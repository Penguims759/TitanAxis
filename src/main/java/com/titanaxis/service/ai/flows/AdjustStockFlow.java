package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class AdjustStockFlow extends AbstractConversationFlow {

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.ADJUST_STOCK;
    }

    @Override
    protected void defineSteps() {
        steps.put("productName", new Step("Ok, vamos ajustar um estoque. Qual o nome do produto?"));
        steps.put("lotNumber", new Step(data -> "Qual o número do lote para '" + data.get("productName") + "'?"));
        steps.put("quantity", new Step(
                "Qual a nova quantidade total para este lote? (Ex: digite 50 para definir o estoque como 50)",
                StringUtil::isNumeric,
                "A quantidade deve ser um número inteiro."
        ));
        steps.put("confirmation", new Step(
                data -> String.format("Confirma a alteração do estoque do lote %s (produto %s) para %s unidades? (sim/não)",
                        data.get("lotNumber"), data.get("productName"), data.get("quantity")),
                input -> input.equalsIgnoreCase("sim") || input.equalsIgnoreCase("nao"),
                "Por favor, responda com 'sim' ou 'não'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse(
                    "Ok, a enviar o ajuste de estoque...",
                    Action.DIRECT_ADJUST_STOCK,
                    conversationData);
        } else {
            return new AssistantResponse("Ok, ação cancelada.");
        }
    }
}