package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.model.ai.ConversationState;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AIAssistantService {

    private final Set<ConversationFlow> conversationFlows;
    private final ConversationState conversationState = new ConversationState();

    @Inject
    public AIAssistantService(Set<ConversationFlow> conversationFlows) {
        this.conversationFlows = conversationFlows;
    }

    public AssistantResponse processQuery(String query) {
        if (conversationState.isAwaitingInfo()) {
            return handleOngoingConversation(query);
        }
        return handleNewCommand(query);
    }

    private AssistantResponse handleOngoingConversation(String userInput) {
        ConversationFlow handler = conversationState.getCurrentFlowHandler();
        if (handler == null) {
            conversationState.reset();
            return new AssistantResponse("Algo correu mal, vamos recomeçar.");
        }

        String normalizedInput = StringUtil.normalize(userInput);
        if (Intent.DENY.getKeywords().stream().anyMatch(normalizedInput::contains)) {
            conversationState.reset();
            return new AssistantResponse("Ok, ação cancelada.");
        }

        String processedInput = userInput;
        if (Intent.CONFIRM.getKeywords().stream().anyMatch(normalizedInput::contains)) {
            processedInput = "sim";
        }

        AssistantResponse response = handler.process(processedInput, conversationState.getCollectedData());

        if (response.getAction() != Action.AWAITING_INFO) {
            conversationState.reset();
        }

        return response;
    }

    private AssistantResponse handleNewCommand(String query) {
        Intent intent = getIntent(query);

        switch (intent) {
            case DENY:
                return new AssistantResponse("Entendido.");
            case GREETING:
                return new AssistantResponse("Olá! Em que posso ajudar?");
            case CHANGE_THEME:
                return handleChangeTheme(query);
            case NAVIGATE_TO:
                return handleNavigation(query);
            case GUIDE_ADD_LOTE:
                return new AssistantResponse("Claro, vou mostrar-lhe como adicionar um lote.", Action.GUIDE_NAVIGATE_TO_ADD_LOTE, null);
            // CORRIGIDO: Agora usa a nova intenção e ação
            case GUIDE_ADD_PRODUCT:
                return new AssistantResponse("Com certeza. Vou mostrar-lhe como adicionar um novo produto.", Action.GUIDE_NAVIGATE_TO_ADD_PRODUCT, null);
        }

        Optional<ConversationFlow> handler = findHandlerFor(intent);
        if (handler.isPresent()) {
            conversationState.startConversation(handler.get());
            return handleOngoingConversation(query);
        }

        return new AssistantResponse("Desculpe, não consegui entender o seu pedido. Pode tentar reformular?");
    }

    private Intent getIntent(String query) {
        String normalizedQuery = StringUtil.normalize(query);

        if (Intent.DENY.getKeywords().stream().anyMatch(normalizedQuery::contains)) return Intent.DENY;
        if (Intent.CONFIRM.getKeywords().stream().anyMatch(normalizedQuery::contains)) return Intent.CONFIRM;
        if (Intent.GREETING.getKeywords().stream().anyMatch(normalizedQuery::contains)) return Intent.GREETING;

        return Arrays.stream(Intent.values())
                .filter(intent -> intent.getScore(normalizedQuery) > 0)
                .max(Comparator.comparingInt(intent -> intent.getScore(normalizedQuery)))
                .orElse(Intent.UNKNOWN);
    }

    private Optional<ConversationFlow> findHandlerFor(Intent intent) {
        return conversationFlows.stream()
                .filter(flow -> flow.canHandle(intent))
                .findFirst();
    }

    private AssistantResponse handleChangeTheme(String query) {
        String normalized = StringUtil.normalize(query);
        if (normalized.contains("claro")) return new AssistantResponse("Mudando para o tema claro!", Action.UI_CHANGE_THEME, Map.of("theme", "light"));
        if (normalized.contains("escuro")) return new AssistantResponse("Mudando para o tema escuro!", Action.UI_CHANGE_THEME, Map.of("theme", "dark"));
        return new AssistantResponse("Não entendi qual tema prefere. Tente 'tema claro' ou 'tema escuro'.");
    }

    private AssistantResponse handleNavigation(String query) {
        String normalized = StringUtil.normalize(query);
        String destination = null;
        if (normalized.contains("venda")) destination = "Vendas";
        else if (normalized.contains("cliente")) destination = "Clientes";
        else if (normalized.contains("produto") || normalized.contains("estoque")) destination = "Produtos & Estoque";
        else if (normalized.contains("relatorio")) destination = "Relatórios";
        else if (normalized.contains("categoria")) destination = "Categorias";
        else if (normalized.contains("alerta")) destination = "Alertas de Estoque";
        else if (normalized.contains("movimento")) destination = "Histórico de Movimentos";
        else if (normalized.contains("usuario") || normalized.contains("utilizador")) destination = "Gestão de Usuários";
        else if (normalized.contains("auditoria") || normalized.contains("log")) destination = "Logs de Auditoria";

        if (destination != null) return new AssistantResponse("A navegar para " + destination + "...", Action.UI_NAVIGATE, Map.of("destination", destination));
        return new AssistantResponse("Não percebi para onde quer ir. Tente 'ir para vendas', por exemplo.");
    }
}