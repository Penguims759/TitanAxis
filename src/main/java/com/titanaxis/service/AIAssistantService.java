// src/main/java/com/titanaxis/service/AIAssistantService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.model.ai.ConversationContext;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.service.ai.NLPIntentService;
import com.titanaxis.util.StringUtil;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIAssistantService {
    private final Set<ConversationFlow> conversationFlows;
    private final ConversationContext context = new ConversationContext();
    private final NLPIntentService nlpIntentService;

    // Padrões para remover partes comuns dos comandos e isolar a entidade
    private static final Pattern QUERY_STOCK_PATTERN = Pattern.compile("^(qual o estoque do produto|ver o stock de|estoque de|qual o estoque de)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUERY_PRODUCT_LOTS_PATTERN = Pattern.compile("^(quais os lotes da|ver lotes do produto|lotes do produto)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAVIGATION_PATTERN = Pattern.compile("^(ir para|navegar para|abrir o painel de|me leve para)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXECUTE_SALE_PATTERN = Pattern.compile("vender\\s+(\\d+)\\s+(?:unidades de|de)?\\s*'?([^']*)'?\\s*(?:do lote\\s*'([^']*)')?\\s*(?:para o cliente\\s*'([^']*)')?", Pattern.CASE_INSENSITIVE);


    @Inject
    public AIAssistantService(Set<ConversationFlow> conversationFlows, NLPIntentService nlpIntentService) {
        this.conversationFlows = conversationFlows;
        this.nlpIntentService = nlpIntentService;
    }

    public ConversationContext getContext() {
        return context;
    }

    public AssistantResponse processQuery(String query) {
        String normalizedQuery = StringUtil.normalize(query);

        if (isCancelCommand(normalizedQuery)) {
            context.fullReset();
            return new AssistantResponse("Ok, ação cancelada e contexto limpo.");
        }

        if (context.isAwaitingInfo()) {
            return handleOngoingConversation(query);
        }

        return handleNewCommand(query); // Passar a query original
    }

    private boolean isCancelCommand(String normalizedQuery) {
        return nlpIntentService.getIntent(normalizedQuery) == Intent.DENY;
    }

    private AssistantResponse handleOngoingConversation(String originalQuery) {
        Optional<Action> proactiveActionOpt = context.getPendingProactiveAction();
        if (proactiveActionOpt.isPresent()) {
            return handleProactiveConfirmation(originalQuery, proactiveActionOpt.get());
        }

        ConversationFlow handler = context.getCurrentFlow();
        AssistantResponse response = handler.process(originalQuery, context.getCollectedData());

        if (response.getAction() != Action.AWAITING_INFO && response.getAction() != null && !response.getAction().name().startsWith("PROACTIVE_")) {
            Optional.ofNullable(context.getCollectedData().get("foundEntity")).ifPresent(entity -> {
                context.setLastEntity(entity);
                context.setLastIntent((Intent) context.getCollectedData().get("intent"));
            });
            context.resetFlow();
        }
        return response;
    }

    private AssistantResponse handleProactiveConfirmation(String userInput, Action proactiveAction) {
        Intent userIntent = nlpIntentService.getIntent(StringUtil.normalize(userInput));
        Map<String, Object> params = context.getProactiveActionParams();
        context.resetFlow();

        if (userIntent == Intent.CONFIRM) {
            switch (proactiveAction) {
                case PROACTIVE_SUGGEST_ADD_LOTE:
                    return startConversationFlow(Intent.MANAGE_STOCK, (String) params.get("nome"));
                case PROACTIVE_SUGGEST_START_SALE:
                    return startConversationFlow(Intent.START_SALE, (String) params.get("nome"));
                default:
                    return new AssistantResponse("Ok, mas não sei como continuar a partir daqui.");
            }
        } else {
            return new AssistantResponse("Ok, deixamos para a próxima.");
        }
    }


    private AssistantResponse handleNewCommand(String originalQuery) {
        String normalizedQuery = StringUtil.normalize(originalQuery);
        Intent intent = nlpIntentService.getIntent(normalizedQuery);

        if (intent == Intent.GREETING || intent == Intent.UNKNOWN) {
            context.fullReset();
        }

        switch (intent) {
            case GREETING:
                return new AssistantResponse(getGreetingByTimeOfDay() + " Em que posso ajudar?");
            case CHANGE_THEME:
                return handleChangeTheme(normalizedQuery);
            case NAVIGATE_TO:
                return handleNavigation(originalQuery);
            case GUIDE_ADD_LOTE:
                return new AssistantResponse("Claro, vou mostrar-lhe como adicionar um lote.", Action.GUIDE_NAVIGATE_TO_ADD_LOTE, null);
            case GUIDE_ADD_PRODUCT:
                return new AssistantResponse("Com certeza. Vou mostrar-lhe como adicionar um novo produto.", Action.GUIDE_NAVIGATE_TO_ADD_PRODUCT, null);
            case UNKNOWN:
                return new AssistantResponse("Desculpe, não consegui entender o seu pedido. Pode tentar reformular?");
            default:
                return startConversationFlow(intent, originalQuery);
        }
    }

    private String getGreetingByTimeOfDay() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.NOON)) {
            return "Bom dia!";
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            return "Boa tarde!";
        } else {
            return "Boa noite!";
        }
    }

    private Map<String, Object> extractSaleEntities(String query) {
        Map<String, Object> entities = new HashMap<>();
        Matcher matcher = EXECUTE_SALE_PATTERN.matcher(query);
        if (matcher.find()) {
            try {
                entities.put("quantidade", Integer.parseInt(matcher.group(1)));
                entities.put("produto", matcher.group(2).trim());
                if (matcher.group(3) != null) entities.put("lote", matcher.group(3).trim());
                if (matcher.group(4) != null) entities.put("cliente", matcher.group(4).trim());
            } catch (NumberFormatException e) {
                // ignora se a quantidade não for um número
            }
        }
        return entities;
    }

    private String cleanAndExtractEntity(String query, Intent intent) {
        String cleanedQuery = query.replaceAll("[?.,!]$", "").trim();
        switch(intent) {
            case QUERY_STOCK:
                return QUERY_STOCK_PATTERN.matcher(cleanedQuery).replaceAll("");
            case QUERY_PRODUCT_LOTS:
                return QUERY_PRODUCT_LOTS_PATTERN.matcher(cleanedQuery).replaceAll("");
            case NAVIGATE_TO:
                return NAVIGATION_PATTERN.matcher(cleanedQuery).replaceAll("");
            default:
                return StringUtil.extractValueAfter(cleanedQuery, new String[]{"de", "do", "da", "para", "para o", "para a"});
        }
    }


    private AssistantResponse startConversationFlow(Intent intent, String originalQuery) {
        Optional<ConversationFlow> handlerOpt = findHandlerFor(intent);

        if (handlerOpt.isPresent()) {
            ConversationFlow handler = handlerOpt.get();
            context.startFlow(handler);
            context.getCollectedData().put("intent", intent);

            if (intent == Intent.EXECUTE_FULL_SALE) {
                Map<String, Object> saleEntities = extractSaleEntities(originalQuery);
                context.getCollectedData().putAll(saleEntities);
            } else {
                String extractedEntity = cleanAndExtractEntity(originalQuery, intent);
                if (extractedEntity != null && !extractedEntity.isEmpty()) {
                    context.getCollectedData().put("entity", extractedEntity);
                    context.setLastEntity(null);
                } else {
                    context.getLastEntity().ifPresent(lastEntity -> {
                        if (lastEntity instanceof Produto && isProductQuery(intent)) {
                            context.getCollectedData().put("entity", ((Produto) lastEntity).getNome());
                        } else if (lastEntity instanceof Cliente && isClientQuery(intent)) {
                            context.getCollectedData().put("entity", ((Cliente) lastEntity).getNome());
                        }
                    });
                }
            }

            return handler.process(originalQuery, context.getCollectedData());
        }

        return new AssistantResponse("Não tenho a certeza de como processar esse pedido: " + intent.name());
    }

    private Optional<ConversationFlow> findHandlerFor(Intent intent) {
        return conversationFlows.stream().filter(flow -> flow.canHandle(intent)).findFirst();
    }

    private boolean isProductQuery(Intent intent) {
        return intent.name().startsWith("QUERY_PRODUCT") || intent.name().contains("STOCK") || intent == Intent.UPDATE_PRODUCT || intent == Intent.MANAGE_STOCK;
    }

    private boolean isClientQuery(Intent intent) {
        return intent.name().startsWith("QUERY_CLIENT") || intent == Intent.START_SALE;
    }

    private AssistantResponse handleChangeTheme(String query) {
        if (query.contains("claro"))
            return new AssistantResponse("Mudando para o tema claro!", Action.UI_CHANGE_THEME, Map.of("theme", "light"));
        if (query.contains("escuro"))
            return new AssistantResponse("Mudando para o tema escuro!", Action.UI_CHANGE_THEME, Map.of("theme", "dark"));
        return new AssistantResponse("Não entendi qual tema prefere. Tente 'tema claro' ou 'tema escuro'.");
    }

    private AssistantResponse handleNavigation(String query) {
        String destination = cleanAndExtractEntity(query, Intent.NAVIGATE_TO);
        if (destination == null || destination.isEmpty()) {
            return new AssistantResponse("Não percebi para onde quer ir. Tente 'ir para vendas', por exemplo.");
        }

        String targetPanel = null;
        String normalizedDestination = StringUtil.normalize(destination);

        if (normalizedDestination.contains("venda")) targetPanel = "Vendas";
        else if (normalizedDestination.contains("cliente")) targetPanel = "Clientes";
        else if (normalizedDestination.contains("produto") || normalizedDestination.contains("estoque")) targetPanel = "Produtos & Estoque";
        else if (normalizedDestination.contains("relatorio")) targetPanel = "Relatórios";
        else if (normalizedDestination.contains("categoria")) targetPanel = "Categorias";
        else if (normalizedDestination.contains("alerta")) targetPanel = "Alertas de Estoque";
        else if (normalizedDestination.contains("movimento")) targetPanel = "Histórico de Movimentos";
        else if (normalizedDestination.contains("usuario") || normalizedDestination.contains("utilizador")) targetPanel = "Gestão de Usuários";
        else if (normalizedDestination.contains("auditoria") || normalizedDestination.contains("log")) targetPanel = "Logs de Auditoria";
        else if (normalizedDestination.contains("inicio") || normalizedDestination.contains("home")) targetPanel = "Início";
        else if (normalizedDestination.contains("financeiro")) targetPanel = "Financeiro";

        if (targetPanel != null) {
            return new AssistantResponse("A navegar para " + targetPanel + "...", Action.UI_NAVIGATE, Map.of("destination", targetPanel));
        }
        return new AssistantResponse("Não encontrei um painel chamado '" + destination + "'.");
    }
}