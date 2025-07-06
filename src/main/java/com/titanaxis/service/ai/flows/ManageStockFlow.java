// src/main/java/com/titanaxis/service/ai/flows/ManageStockFlow.java
package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AIAssistantService.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;
import java.util.Map;

public class ManageStockFlow implements ConversationFlow {

    private enum State {
        START, AWAITING_PRODUCT_NAME, AWAITING_LOT_NUMBER, AWAITING_QUANTITY, CONFIRMATION
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.MANAGE_STOCK;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        State currentState = (State) data.getOrDefault("state", State.START);

        if (currentState != State.START && !userInput.isEmpty()) {
            switch (currentState) {
                case AWAITING_PRODUCT_NAME:
                    data.put("productName", userInput);
                    break;
                case AWAITING_LOT_NUMBER:
                    data.put("lotNumber", userInput);
                    break;
                case AWAITING_QUANTITY:
                    if (StringUtil.isNumeric(userInput)) {
                        data.put("quantity", Integer.parseInt(userInput));
                    } else {
                        return new AssistantResponse("A quantidade deve ser um número.");
                    }
                    break;
                case CONFIRMATION:
                    if (userInput.equalsIgnoreCase("sim")) {
                        data.put("isFinal", true);
                        return new AssistantResponse("Ok, a atualizar o stock...", Action.DIRECT_ADD_STOCK, data);
                    } else {
                        data.put("isFinal", true);
                        return new AssistantResponse("Ok, ação cancelada.");
                    }
            }
        }

        if (!data.containsKey("productName")) {
            data.put("state", State.AWAITING_PRODUCT_NAME);
            return new AssistantResponse("Ok. Qual o nome do produto?");
        }
        if (!data.containsKey("lotNumber")) {
            data.put("state", State.AWAITING_LOT_NUMBER);
            return new AssistantResponse("Certo. Qual o número do lote para '" + data.get("productName") + "'?");
        }
        if (!data.containsKey("quantity")) {
            data.put("state", State.AWAITING_QUANTITY);
            return new AssistantResponse("E qual a quantidade a adicionar ao lote '" + data.get("lotNumber") + "'?");
        }

        data.put("state", State.CONFIRMATION);
        return new AssistantResponse(String.format("Você confirma a adição de %d unidades ao lote %s do produto %s? (sim/não)",
                (Integer) data.get("quantity"), data.get("lotNumber"), data.get("productName")));
    }
}