package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class AdjustStockFlow extends AbstractConversationFlow {

    private final FlowValidationService validationService;

    @Inject
    public AdjustStockFlow(FlowValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.ADJUST_STOCK || intent == Intent.UPDATE_LOTE;
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
        steps.put("productName", new Step(
                "Ok, vamos alterar um lote. Qual o nome do produto?",
                (input, data) -> validationService.isProdutoValido(input),
                "Produto não encontrado. Por favor, verifique o nome ou diga 'cancelar'."
        ));
        steps.put("lotNumber", new Step(
                data -> "Qual o número do lote para '" + data.get("productName") + "'?",
                (input, data) -> validationService.isLoteValido((String) data.get("productName"), input),
                "Lote não encontrado para este produto. Verifique o número ou diga 'cancelar'."
        ));
        steps.put("quantity", new Step(
                "Qual a nova quantidade total para este lote? (Ex: digite 50 para definir o estoque como 50)",
                StringUtil::isNumeric,
                "A quantidade deve ser um número inteiro."
        ));
        steps.put("confirmation", new Step(
                data -> {
                    int quantity = Integer.parseInt((String) data.get("quantity"));
                    data.put("quantity", quantity);
                    return String.format("Confirma a alteração do estoque do lote %s (produto %s) para %d unidades? (sim/não)",
                            data.get("lotNumber"), data.get("productName"), quantity);
                },
                input -> StringUtil.normalize(input).equals("sim") || StringUtil.normalize(input).equals("nao"),
                "Por favor, responda com 'sim' ou 'não'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse(
                    "Ok, a enviar o ajuste de estoque...",
                    Action.DIRECT_ADJUST_STOCK,
                    new HashMap<>(conversationData));
        } else {
            return new AssistantResponse("Ok, ação cancelada.");
        }
    }
}