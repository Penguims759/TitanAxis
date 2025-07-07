package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import java.util.Map;

public class CreateClientFlow implements ConversationFlow {

    private enum State {
        START, AWAITING_NAME, AWAITING_CONTACT
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_CLIENT;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        State currentState = (State) data.getOrDefault("state", State.START);

        if (currentState != State.START && !userInput.isEmpty()) {
            if (!data.containsKey("nome")) {
                data.put("nome", userInput);
            } else {
                data.put("contato", userInput);
            }
        }

        if (!data.containsKey("nome")) {
            data.put("state", State.AWAITING_NAME);
            return new AssistantResponse("Vamos criar um novo cliente. Qual é o nome dele?");
        }
        if (!data.containsKey("contato")) {
            data.put("state", State.AWAITING_CONTACT);
            return new AssistantResponse("Ok, o nome é '" + data.get("nome") + "'. Qual o contato (email/telefone)?");
        }

        data.put("isFinal", true);
        return new AssistantResponse("Entendido! A criar o cliente '" + data.get("nome") + "'.", Action.DIRECT_CREATE_CLIENT, data);
    }
}