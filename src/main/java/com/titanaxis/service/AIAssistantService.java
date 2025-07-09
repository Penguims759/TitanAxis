package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.model.ai.ConversationContext;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AIAssistantService {

    private final Set<ConversationFlow> conversationFlows;
    private final ConversationContext context = new ConversationContext();

    @Inject
    public AIAssistantService(Set<ConversationFlow> conversationFlows) {
        this.conversationFlows = conversationFlows;
    }

    public AssistantResponse processQuery(String query) {
        String normalizedQuery = StringUtil.normalize(query);

        if (isCancelCommand(normalizedQuery)) {
            context.fullReset();
            return new AssistantResponse("Ok, ação cancelada e contexto limpo.");
        }

        if (context.isAwaitingInfo()) {
            return handleOngoingConversation(normalizedQuery, query);
        }

        return handleNewCommand(normalizedQuery, query);
    }

    private boolean isCancelCommand(String normalizedQuery) {
        return Intent.DENY.getKeywords().stream().anyMatch(normalizedQuery::contains);
    }

    private AssistantResponse handleOngoingConversation(String normalizedQuery, String originalQuery) {
        ConversationFlow handler = context.getCurrentFlow();

        AssistantResponse response = handler.process(originalQuery, context.getCollectedData());

        // Se o fluxo terminou (não está mais à espera de informação)
        if (!context.isAwaitingInfo()) {
            // Verificamos se o fluxo encontrou uma entidade e a guardamos no contexto
            Object foundEntity = context.getCollectedData().get("foundEntity");
            if (foundEntity != null) {
                context.setLastEntity(foundEntity);
                context.setLastIntent( (Intent) context.getCollectedData().get("intent") );
            }
            context.resetFlow(); // Limpa o fluxo, mas mantém o contexto
        }

        return response;
    }

    private AssistantResponse handleNewCommand(String normalizedQuery, String originalQuery) {
        Intent intent = getIntent(normalizedQuery);

        // Comandos que limpam o contexto
        if (intent == Intent.GREETING || intent == Intent.UNKNOWN) {
            context.fullReset();
        }

        switch (intent) {
            case GREETING: return new AssistantResponse("Olá! Em que posso ajudar?");
            case CHANGE_THEME: return handleChangeTheme(normalizedQuery);
            case NAVIGATE_TO: return handleNavigation(normalizedQuery);
            case GUIDE_ADD_LOTE: return new AssistantResponse("Claro, vou mostrar-lhe como adicionar um lote.", Action.GUIDE_NAVIGATE_TO_ADD_LOTE, null);
            case GUIDE_ADD_PRODUCT: return new AssistantResponse("Com certeza. Vou mostrar-lhe como adicionar um novo produto.", Action.GUIDE_NAVIGATE_TO_ADD_PRODUCT, null);
        }

        Optional<ConversationFlow> handlerOpt = findHandlerFor(intent);
        if (handlerOpt.isPresent()) {
            context.startFlow(handlerOpt.get());
            context.getCollectedData().put("intent", intent); // Guarda a intenção atual

            // Se a query tem uma entidade explícita (ex: "stock de Caneta Azul")
            Optional<String> entityFromQuery = intent.extractEntity(normalizedQuery);
            if(entityFromQuery.isPresent()){
                context.getCollectedData().put("entity", entityFromQuery.get());
                context.setLastEntity(null); // Limpa contexto antigo pois um novo foi especificado
            } else {
                // Se não, tenta usar o contexto da conversa anterior
                context.getLastEntity().ifPresent(lastEntity -> {
                    if (lastEntity instanceof Produto && isProductQuery(intent)) {
                        context.getCollectedData().put("entity", ((Produto) lastEntity).getNome());
                    } else if (lastEntity instanceof Cliente && isClientQuery(intent)) {
                        context.getCollectedData().put("entity", ((Cliente) lastEntity).getNome());
                    }
                });
            }

            return handleOngoingConversation(normalizedQuery, originalQuery);
        }

        return new AssistantResponse("Desculpe, não consegui entender o seu pedido. Pode tentar reformular?");
    }

    private Intent getIntent(String normalizedQuery) {
        // Intenções gerais têm prioridade
        if (Intent.GREETING.getKeywords().stream().anyMatch(normalizedQuery::contains)) return Intent.GREETING;
        if (Intent.CONFIRM.getKeywords().stream().anyMatch(normalizedQuery::contains)) return Intent.CONFIRM;

        return Arrays.stream(Intent.values())
                .filter(intent -> intent != Intent.UNKNOWN && intent.getScore(normalizedQuery) > 0)
                .max(Comparator.comparingInt(intent -> intent.getScore(normalizedQuery)))
                .orElse(Intent.UNKNOWN);
    }

    private Optional<ConversationFlow> findHandlerFor(Intent intent) {
        return conversationFlows.stream()
                .filter(flow -> flow.canHandle(intent))
                .findFirst();
    }

    private boolean isProductQuery(Intent intent) {
        return intent == Intent.QUERY_STOCK || intent == Intent.QUERY_PRODUCT_LOTS || intent == Intent.QUERY_MOVEMENT_HISTORY || intent == Intent.MANAGE_STOCK || intent == Intent.ADJUST_STOCK;
    }

    private boolean isClientQuery(Intent intent) {
        return intent == Intent.QUERY_CLIENT_HISTORY || intent == Intent.START_SALE || intent == Intent.QUERY_CLIENT_DETAILS;
    }

    private AssistantResponse handleChangeTheme(String query) {
        if (query.contains("claro")) return new AssistantResponse("Mudando para o tema claro!", Action.UI_CHANGE_THEME, Map.of("theme", "light"));
        if (query.contains("escuro")) return new AssistantResponse("Mudando para o tema escuro!", Action.UI_CHANGE_THEME, Map.of("theme", "dark"));
        return new AssistantResponse("Não entendi qual tema prefere. Tente 'tema claro' ou 'tema escuro'.");
    }

    private AssistantResponse handleNavigation(String query) {
        String destination = null;
        if (query.contains("venda")) destination = "Vendas";
        else if (query.contains("cliente")) destination = "Clientes";
        else if (query.contains("produto") || query.contains("estoque")) destination = "Produtos & Estoque";
        else if (query.contains("relatorio")) destination = "Relatórios";
        else if (query.contains("categoria")) destination = "Categorias";
        else if (query.contains("alerta")) destination = "Alertas de Estoque";
        else if (query.contains("movimento")) destination = "Histórico de Movimentos";
        else if (query.contains("usuario") || query.contains("utilizador")) destination = "Gestão de Usuários";
        else if (query.contains("auditoria") || query.contains("log")) destination = "Logs de Auditoria";

        if (destination != null) return new AssistantResponse("A navegar para " + destination + "...", Action.UI_NAVIGATE, Map.of("destination", destination));
        return new AssistantResponse("Não percebi para onde quer ir. Tente 'ir para vendas', por exemplo.");
    }
}