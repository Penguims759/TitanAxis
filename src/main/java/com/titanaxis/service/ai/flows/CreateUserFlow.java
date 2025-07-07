package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.Map;

public class CreateUserFlow implements ConversationFlow {

    private final AuthService authService;

    private enum State {
        START, AWAITING_USERNAME, AWAITING_PASSWORD, AWAITING_LEVEL
    }

    @Inject
    public CreateUserFlow(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_USER;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        if (!authService.isAdmin()) {
            data.put("isFinal", true);
            return new AssistantResponse("Desculpe, apenas administradores podem criar novos utilizadores.");
        }

        State currentState = (State) data.getOrDefault("state", State.START);

        if (currentState != State.START && !userInput.isEmpty()) {
            switch (currentState) {
                case AWAITING_USERNAME:
                    data.put("username", userInput);
                    break;
                case AWAITING_PASSWORD:
                    data.put("password", userInput);
                    break;
                case AWAITING_LEVEL:
                    try {
                        data.put("level", NivelAcesso.valueOf(userInput.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return new AssistantResponse("Nível inválido. Use 'padrao', 'gerente' ou 'admin'.");
                    }
                    break;
            }
        }

        if (!data.containsKey("username")) {
            data.put("state", State.AWAITING_USERNAME);
            return new AssistantResponse("Certo. Qual o nome do novo utilizador?");
        }
        if (!data.containsKey("password")) {
            data.put("state", State.AWAITING_PASSWORD);
            return new AssistantResponse("Qual será a senha para '" + data.get("username") + "'?");
        }
        if (!data.containsKey("level")) {
            data.put("state", State.AWAITING_LEVEL);
            return new AssistantResponse("E qual o nível de acesso? (padrao, gerente, ou admin)");
        }

        data.put("isFinal", true);
        return new AssistantResponse("A criar o utilizador '" + data.get("username") + "'...", Action.DIRECT_CREATE_USER, data);
    }
}