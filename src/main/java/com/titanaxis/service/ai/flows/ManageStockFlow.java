package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class ManageStockFlow extends AbstractConversationFlow {

    private final FlowValidationService validationService;

    @Inject
    public ManageStockFlow(FlowValidationService validationService) {
        this.validationService = validationService;
    }

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
        steps.put("productName", new Step(
                "Ok. Qual o nome do produto?",
                (input, data) -> validationService.isProdutoValido(input),
                "Produto não encontrado. Por favor, verifique o nome ou diga 'cancelar'."
        ));
        steps.put("lotNumber", new Step(data -> "Certo. Qual o número do lote para '" + data.get("productName") + "'?"));
        steps.put("quantity", new Step(
                data -> "E qual a quantidade a adicionar ao lote '" + data.get("lotNumber") + "'?",
                StringUtil::isNumeric,
                "A quantidade deve ser um número."
        ));
        steps.put("confirmation", new Step(
                data -> String.format("Você confirma a adição de %s unidades ao lote %s do produto %s? (sim/não)",
                        data.get("quantity"), data.get("lotNumber"), data.get("productName")),
                input -> StringUtil.normalize(input).equals("sim") || StringUtil.normalize(input).equals("nao"),
                "Por favor, responda com 'sim' ou 'não'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse(
                    "Ok, a atualizar o stock...",
                    Action.DIRECT_ADD_STOCK,
                    new HashMap<>(conversationData));
        } else {
            return new AssistantResponse("Ok, ação cancelada.");
        }
    }
}