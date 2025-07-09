// Caminho: penguims759/titanaxis/Penguims759-TitanAxis-d11978d74c8d39dd19a6d1a7bb798e37ccb09060/src/main/java/com/titanaxis/service/AIAssistantService.java
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

    @Inject public AIAssistantService(Set<ConversationFlow> conversationFlows) { this.conversationFlows = conversationFlows; }

    public AssistantResponse processQuery(String query) {
        String normalizedQuery = StringUtil.normalize(query);
        if (isCancelCommand(normalizedQuery)) {
            context.fullReset();
            return new AssistantResponse("Ok, ação cancelada e contexto limpo.");
        }
        if (context.isAwaitingInfo()) { return handleOngoingConversation(query); }
        return handleNewCommand(normalizedQuery, query);
    }

    private boolean isCancelCommand(String normalizedQuery) { return Intent.DENY.getKeywords().stream().anyMatch(normalizedQuery::contains); }

    private AssistantResponse handleOngoingConversation(String originalQuery) {
        ConversationFlow handler = context.getCurrentFlow();
        AssistantResponse response = handler.process(originalQuery, context.getCollectedData());
        if (!context.isAwaitingInfo()) {
            Optional.ofNullable(context.getCollectedData().get("foundEntity")).ifPresent(entity -> {
                context.setLastEntity(entity);
                context.setLastIntent((Intent) context.getCollectedData().get("intent"));
            });
            context.resetFlow();
        }
        return response;
    }

    private AssistantResponse handleNewCommand(String normalizedQuery, String originalQuery) {
        Intent intent = getIntent(normalizedQuery);
        if (intent == Intent.GREETING || intent == Intent.UNKNOWN) { context.fullReset(); }

        switch (intent) {
            case GREETING: return new AssistantResponse("Olá! Em que posso ajudar?");
            case CHANGE_THEME: return handleChangeTheme(normalizedQuery);
            case NAVIGATE_TO: return handleNavigation(normalizedQuery);
            case GUIDE_ADD_LOTE: return new AssistantResponse("Claro, vou mostrar-lhe como adicionar um lote.", Action.GUIDE_NAVIGATE_TO_ADD_LOTE, null);
            case GUIDE_ADD_PRODUCT: return new AssistantResponse("Com certeza. Vou mostrar-lhe como adicionar um novo produto.", Action.GUIDE_NAVIGATE_TO_ADD_PRODUCT, null);
            case UNKNOWN: return new AssistantResponse("Desculpe, não consegui entender o seu pedido. Pode tentar reformular?");
        }

        Optional<ConversationFlow> handlerOpt = findHandlerFor(intent);
        if (handlerOpt.isPresent()) {
            context.startFlow(handlerOpt.get());
            context.getCollectedData().put("intent", intent);
            intent.extractEntity(normalizedQuery).ifPresentOrElse(
                    entity -> {
                        context.getCollectedData().put("entity", entity);
                        context.setLastEntity(null);
                    },
                    () -> context.getLastEntity().ifPresent(lastEntity -> {
                        if (lastEntity instanceof Produto && isProductQuery(intent)) { context.getCollectedData().put("entity", ((Produto) lastEntity).getNome()); }
                        else if (lastEntity instanceof Cliente && isClientQuery(intent)) { context.getCollectedData().put("entity", ((Cliente) lastEntity).getNome()); }
                    })
            );
            return handleOngoingConversation(originalQuery);
        }
        return new AssistantResponse("Não tenho a certeza de como processar esse pedido.");
    }

    private Intent getIntent(String normalizedQuery) {
        if (Intent.GREETING.getKeywords().stream().anyMatch(normalizedQuery::contains)) return Intent.GREETING;
        if (Intent.CONFIRM.getKeywords().stream().anyMatch(normalizedQuery::contains)) return Intent.CONFIRM;
        return Arrays.stream(Intent.values()).filter(i -> i != Intent.UNKNOWN && i.getScore(normalizedQuery) > 0).max(Comparator.comparingInt(i -> i.getScore(normalizedQuery))).orElse(Intent.UNKNOWN);
    }

    private Optional<ConversationFlow> findHandlerFor(Intent intent) { return conversationFlows.stream().filter(flow -> flow.canHandle(intent)).findFirst(); }
    private boolean isProductQuery(Intent intent) { return intent == Intent.QUERY_STOCK || intent == Intent.QUERY_PRODUCT_LOTS || intent == Intent.QUERY_MOVEMENT_HISTORY || intent == Intent.MANAGE_STOCK || intent == Intent.ADJUST_STOCK || intent == Intent.UPDATE_PRODUCT; }
    private boolean isClientQuery(Intent intent) { return intent == Intent.QUERY_CLIENT_HISTORY || intent == Intent.START_SALE || intent == Intent.QUERY_CLIENT_DETAILS; }
    private AssistantResponse handleChangeTheme(String query) { if (query.contains("claro")) return new AssistantResponse("Mudando para o tema claro!", Action.UI_CHANGE_THEME, Map.of("theme", "light")); if (query.contains("escuro")) return new AssistantResponse("Mudando para o tema escuro!", Action.UI_CHANGE_THEME, Map.of("theme", "dark")); return new AssistantResponse("Não entendi qual tema prefere. Tente 'tema claro' ou 'tema escuro'."); }
    private AssistantResponse handleNavigation(String query) { String d = null; if (query.contains("venda")) d = "Vendas"; else if (query.contains("cliente")) d = "Clientes"; else if (query.contains("produto") || query.contains("estoque")) d = "Produtos & Estoque"; else if (query.contains("relatorio")) d = "Relatórios"; else if (query.contains("categoria")) d = "Categorias"; else if (query.contains("alerta")) d = "Alertas de Estoque"; else if (query.contains("movimento")) d = "Histórico de Movimentos"; else if (query.contains("usuario") || query.contains("utilizador")) d = "Gestão de Usuários"; else if (query.contains("auditoria") || query.contains("log")) d = "Logs de Auditoria"; if (d != null) return new AssistantResponse("A navegar para " + d + "...", Action.UI_NAVIGATE, Map.of("destination", d)); return new AssistantResponse("Não percebi para onde quer ir. Tente 'ir para vendas', por exemplo."); }
}