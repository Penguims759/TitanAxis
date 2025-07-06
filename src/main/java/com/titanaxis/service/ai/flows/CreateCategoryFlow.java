// src/main/java/com/titanaxis/service/ai/flows/CreateCategoryFlow.java
package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AIAssistantService.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import java.util.Map;

public class CreateCategoryFlow implements ConversationFlow {

    private enum State {
        START, AWAITING_NAME
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_CATEGORY;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        State currentState = (State) data.getOrDefault("state", State.START);

        if (currentState != State.START && !userInput.isEmpty()) {
            data.put("nome", userInput);
        }

        if (!data.containsKey("nome")) {
            data.put("state", State.AWAITING_NAME);
            return new AssistantResponse("Claro, vamos criar uma nova categoria. Qual é o nome?");
        }

        data.put("isFinal", true);
        return new AssistantResponse("Ok, a criar a categoria '" + data.get("nome") + "'...", Action.DIRECT_CREATE_CATEGORY, data);
    }
}