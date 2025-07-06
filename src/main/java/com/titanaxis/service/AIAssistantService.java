// src/main/java/com/titanaxis/service/AIAssistantService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.model.ai.ConversationState;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AIAssistantService {

    private final Set<ConversationFlow> conversationFlows;
    private final ConversationState conversationState = new ConversationState();
    private final AnalyticsService analyticsService;

    public enum Intent {
        UPDATE_PRODUCT(Arrays.asList("alterar produto", "mudar produto", "alterar preco", "alterar status", "inativar produto", "ativar produto")),
        MANAGE_STOCK(Arrays.asList("adicionar stock", "adicionar unidades", "adicionar ao lote", "gerir stock", "gerir estoque")),
        CREATE_PRODUCT(Arrays.asList("criar produto", "novo produto", "cadastrar produto")),
        CREATE_USER(Arrays.asList("criar utilizador", "novo utilizador", "adicionar utilizador", "criar usuario", "novo usuario", "adicionar usuario")),
        CREATE_CATEGORY(Arrays.asList("criar categoria", "nova categoria", "adicionar categoria")),
        CREATE_CLIENT(Arrays.asList("criar cliente", "novo cliente", "adicionar cliente")),

        QUERY_STOCK(Arrays.asList("qual o stock", "qual o estoque", "stock de", "estoque de", "ver stock", "ver estoque")),
        QUERY_CLIENT_DETAILS(Arrays.asList("detalhes do cliente", "mostre o cliente", "ver cliente", "informacoes do cliente")),
        QUERY_PRODUCT_LOTS(Arrays.asList("quais os lotes", "lotes do produto", "ver lotes", "mostrar lotes")),
        QUERY_MOVEMENT_HISTORY(Arrays.asList("historico de movimentos", "ver historico", "mostre o historico")),
        QUERY_TOP_CLIENTS(Arrays.asList("melhores clientes", "top clientes", "ranking de clientes", "cliente que mais compra")),

        // NOVA INTENÇÃO DE NAVEGAÇÃO
        NAVIGATE_TO(Arrays.asList("ir para", "leve-me para", "abrir tela", "abrir ecra", "navegar para", "ver vendas", "ver clientes", "ver produtos", "ver relatorios")),

        GUIDE_ADD_LOTE(Arrays.asList("como adicionar lote", "ajuda lote", "como faco para adicionar lote")),
        CHANGE_THEME(Arrays.asList("mudar tema", "alterar tema", "tema claro", "tema escuro")),
        GREETING(Arrays.asList("ola", "oi", "bom dia", "boa tarde", "boa noite", "boas")),
        CONFIRM(Arrays.asList("sim", "confirmo", "confirma", "pode", "ok", "isso", "exato")),
        DENY(Arrays.asList("não", "nao", "cancela", "cancelar", "sair", "para", "encerrar")),
        UNKNOWN(Arrays.asList());

        private final List<String> keywords;
        Intent(List<String> keywords) { this.keywords = keywords; }
        public List<String> getKeywords() { return keywords; }
    }

    @Inject
    public AIAssistantService(Set<ConversationFlow> conversationFlows, AnalyticsService analyticsService) {
        this.conversationFlows = conversationFlows;
        this.analyticsService = analyticsService;
    }

    public AssistantResponse processQuery(String query) {
        Intent intent = getIntent(query);

        if (intent == Intent.DENY) {
            boolean wasInConversation = conversationState.isAwaitingInfo();
            conversationState.reset();
            return new AssistantResponse(wasInConversation ? "Ok, ação cancelada." : "Ok, tudo bem.");
        }

        if (conversationState.isAwaitingInfo()) {
            return handleConversation(query, intent);
        }

        Optional<ConversationFlow> handler = findHandlerFor(intent);
        if (handler.isPresent()) {
            conversationState.startConversation(handler.get());
            return handleConversation(query, intent);
        }

        try {
            switch (intent) {
                case GREETING:
                    return new AssistantResponse("Olá! Como posso ajudar?");
                case CHANGE_THEME:
                    return handleChangeTheme(query);
                case QUERY_TOP_CLIENTS:
                    return new AssistantResponse(analyticsService.getTopBuyingClients(3));
                case NAVIGATE_TO: // NOVO
                    return handleNavigation(query);
                default:
                    return new AssistantResponse("Desculpe, não entendi o que você quis dizer.");
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao aceder à base de dados para responder à sua pergunta.");
        }
    }

    // NOVO MÉTODO PARA LIDAR COM NAVEGAÇÃO
    private AssistantResponse handleNavigation(String query) {
        String normalizedQuery = StringUtil.normalize(query);
        String destination = null;

        if (normalizedQuery.contains("venda")) destination = "Vendas";
        else if (normalizedQuery.contains("cliente")) destination = "Clientes";
        else if (normalizedQuery.contains("produto")) destination = "Produtos & Estoque";
        else if (normalizedQuery.contains("relatorio")) destination = "Relatórios";
        else if (normalizedQuery.contains("categoria")) destination = "Categorias";
        else if (normalizedQuery.contains("alerta")) destination = "Alertas de Estoque";
        else if (normalizedQuery.contains("movimento")) destination = "Histórico de Movimentos";
        else if (normalizedQuery.contains("usuario") || normalizedQuery.contains("utilizador")) destination = "Gestão de Usuários";
        else if (normalizedQuery.contains("auditoria") || normalizedQuery.contains("log")) destination = "Logs de Auditoria";

        if (destination != null) {
            return new AssistantResponse("A levá-lo para " + destination + "...", Action.UI_NAVIGATE, Map.of("destination", destination));
        }

        return new AssistantResponse("Não percebi para onde você quer ir. Tente 'ir para vendas', por exemplo.");
    }

    // ... (restante dos métodos sem alteração) ...
    private AssistantResponse handleConversation(String userInput, Intent intent) {
        ConversationFlow handler = conversationState.getCurrentFlowHandler();
        if (handler == null) {
            conversationState.reset();
            return new AssistantResponse("Houve um erro, vamos começar de novo.");
        }

        if (intent == Intent.CONFIRM) userInput = "sim";

        AssistantResponse response = handler.process(userInput, conversationState.getCollectedData());

        if (conversationState.getCollectedData().containsKey("isFinal")) {
            conversationState.reset();
        }

        return response;
    }

    private AssistantResponse handleChangeTheme(String query) {
        if (StringUtil.normalize(query).contains("claro")) {
            return new AssistantResponse("Mudando para o tema claro!", Action.UI_CHANGE_THEME, Map.of("theme", "light"));
        } else if (StringUtil.normalize(query).contains("escuro")) {
            return new AssistantResponse("Mudando para o tema escuro!", Action.UI_CHANGE_THEME, Map.of("theme", "dark"));
        }
        return new AssistantResponse("Não entendi qual tema você quer. Tente 'tema claro' ou 'tema escuro'.");
    }

    private Optional<ConversationFlow> findHandlerFor(Intent intent) {
        return conversationFlows.stream()
                .filter(flow -> flow.canHandle(intent))
                .findFirst();
    }

    private Intent getIntent(String query) {
        String normalizedQuery = StringUtil.normalize(query);

        for(String keyword : Intent.DENY.getKeywords()) {
            if (normalizedQuery.contains(keyword)) return Intent.DENY;
        }
        for(String keyword : Intent.CONFIRM.getKeywords()) {
            if (normalizedQuery.contains(keyword)) return Intent.CONFIRM;
        }

        Intent bestIntent = Intent.UNKNOWN;
        int maxScore = 0;

        for (Intent intent : Intent.values()) {
            if (intent.getKeywords().isEmpty() || intent == Intent.CONFIRM || intent == Intent.DENY) continue;

            for (String keyword : intent.getKeywords()) {
                if (normalizedQuery.contains(keyword)) {
                    int score = keyword.length();
                    if (score > maxScore) {
                        maxScore = score;
                        bestIntent = intent;
                    }
                }
            }
        }

        if (bestIntent == Intent.UNKNOWN) {
            for(String keyword : Intent.GREETING.getKeywords()) {
                if (normalizedQuery.contains(keyword)) return Intent.GREETING;
            }
        }

        return bestIntent;
    }
}