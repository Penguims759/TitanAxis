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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AIAssistantService {
    private final Set<ConversationFlow> conversationFlows;
    private final ConversationContext context = new ConversationContext();
    private final NLPIntentService nlpIntentService;

    @Inject
    public AIAssistantService(Set<ConversationFlow> conversationFlows, NLPIntentService nlpIntentService) {
        this.conversationFlows = conversationFlows;
        this.nlpIntentService = nlpIntentService;
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
        return handleNewCommand(normalizedQuery, query);
    }

    private boolean isCancelCommand(String normalizedQuery) {
        return nlpIntentService.getIntent(normalizedQuery) == Intent.DENY;
    }

    private AssistantResponse handleOngoingConversation(String originalQuery) {
        ConversationFlow handler = context.getCurrentFlow();
        if (isCancelCommand(StringUtil.normalize(originalQuery))) {
            context.fullReset();
            return new AssistantResponse("Ok, ação cancelada.");
        }
        AssistantResponse response = handler.process(originalQuery, context.getCollectedData());
        if (response.getAction() != Action.AWAITING_INFO) {
            Optional.ofNullable(context.getCollectedData().get("foundEntity")).ifPresent(entity -> {
                context.setLastEntity(entity);
                context.setLastIntent((Intent) context.getCollectedData().get("intent"));
            });
            context.resetFlow();
        }
        return response;
    }

    private AssistantResponse handleNewCommand(String normalizedQuery, String originalQuery) {
        Intent intent = nlpIntentService.getIntent(normalizedQuery);

        if (intent == Intent.GREETING || intent == Intent.UNKNOWN) {
            context.fullReset();
        }

        switch (intent) {
            case GREETING:
                return new AssistantResponse("Olá! Em que posso ajudar?");
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

    private AssistantResponse startConversationFlow(Intent intent, String originalQuery) {
        Optional<ConversationFlow> handlerOpt = findHandlerFor(intent);

        if (handlerOpt.isPresent()) {
            context.startFlow(handlerOpt.get());
            context.getCollectedData().put("intent", intent);

            String extractedEntity = StringUtil.extractValueAfter(originalQuery, new String[]{"de", "do", "da", "para", "para o", "para a"});

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
            return handleOngoingConversation(originalQuery);
        }

        return new AssistantResponse("Não tenho a certeza de como processar esse pedido: " + intent.name());
    }

    private Optional<ConversationFlow> findHandlerFor(Intent intent) {
        return conversationFlows.stream().filter(flow -> flow.canHandle(intent)).findFirst();
    }

    private boolean isProductQuery(Intent intent) {
        return intent.name().startsWith("QUERY_PRODUCT") || intent.name().contains("STOCK") || intent == Intent.UPDATE_PRODUCT;
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
        String destination = StringUtil.extractValueAfter(query, new String[]{"para", "para a", "para o"});
        if (destination == null) {
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