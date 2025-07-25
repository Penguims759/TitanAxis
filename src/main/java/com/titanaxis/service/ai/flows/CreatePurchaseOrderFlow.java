package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.I18n;
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
                I18n.getString("flow.createOrder.askSupplier"),
                (input, data) -> validationService.isFornecedorValido(input),
                I18n.getString("flow.createOrder.validation.supplierNotFound")
        ));
        steps.put("produto", new Step(
                I18n.getString("flow.createOrder.askProduct"),
                (input, data) -> validationService.isProdutoValido(input),
                I18n.getString("flow.manageStock.validation.productNotFound")
        ));
        steps.put("quantidade", new Step(
                I18n.getString("flow.createOrder.askQuantity"),
                StringUtil::isNumeric,
                I18n.getString("flow.validation.invalidNumber")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        return new AssistantResponse(I18n.getString("flow.createOrder.success",
                conversationData.get("quantidade"),
                conversationData.get("produto"),
                conversationData.get("fornecedor")
        ));
    }
}