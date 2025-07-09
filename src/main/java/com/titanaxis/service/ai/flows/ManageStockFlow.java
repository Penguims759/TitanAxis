// Caminho: penguims759/titanaxis/Penguims759-TitanAxis-d11978d74c8d39dd19a6d1a7bb798e37ccb09060/src/main/java/com/titanaxis/service/ai/flows/ManageStockFlow.java
package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class ManageStockFlow extends AbstractConversationFlow {

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.MANAGE_STOCK;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        if (conversationData.get("entity") != null && !conversationData.containsKey("productName")) {
            conversationData.put("productName", conversationData.get("entity"));
        }
        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("productName", new Step("Ok. Qual o nome do produto?"));
        steps.put("lotNumber", new Step(data -> "Certo. Qual o número do lote para '" + data.get("productName") + "'?"));
        steps.put("quantity", new Step(
                data -> "E qual a quantidade a adicionar ao lote '" + data.get("lotNumber") + "'?",
                StringUtil::isNumeric,
                "A quantidade deve ser um número."
        ));
        steps.put("confirmation", new Step(
                data -> String.format("Você confirma a adição de %s unidades ao lote %s do produto %s? (sim/não)",
                        data.get("quantity"), data.get("lotNumber"), data.get("productName")),
                input -> input.equalsIgnoreCase("sim") || input.equalsIgnoreCase("nao"),
                "Por favor, responda com 'sim' ou 'não'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse("Ok, a atualizar o stock...", Action.DIRECT_ADD_STOCK, conversationData);
        } else {
            return new AssistantResponse("Ok, ação cancelada.");
        }
    }
}