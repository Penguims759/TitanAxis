package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class CreatePurchaseOrderFlow extends AbstractConversationFlow {

    private final FlowValidationService validationService;

    @Inject
    public CreatePurchaseOrderFlow(FlowValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_PURCHASE_ORDER;
    }

    @Override
    protected void defineSteps() {
        steps.put("fornecedor", new Step(
                "Claro, vamos criar uma ordem de compra. Para qual fornecedor?",
                (input, data) -> validationService.isFornecedorValido(input),
                "Fornecedor não encontrado. Por favor, verifique o nome."
        ));
        steps.put("produto", new Step(
                "Qual produto você deseja pedir?",
                (input, data) -> validationService.isProdutoValido(input),
                "Produto não encontrado. Por favor, verifique o nome."
        ));
        steps.put("quantidade", new Step(
                "Qual a quantidade a ser pedida?",
                StringUtil::isNumeric,
                "A quantidade deve ser um número."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String responseText = String.format("Ok, registei um rascunho de pedido de compra de %s unidades de '%s' para o fornecedor '%s'.",
                conversationData.get("quantidade"),
                conversationData.get("produto"),
                conversationData.get("fornecedor")
        );
        return new AssistantResponse(responseText);
    }
}