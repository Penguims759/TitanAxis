package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.I18n;
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
                I18n.getString("flow.manageStock.askProductName"),
                (input, data) -> validationService.isProdutoValido(input),
                I18n.getString("flow.manageStock.validation.productNotFound")
        ));
        steps.put("lotNumber", new Step(data -> I18n.getString("flow.manageStock.askLotNumber", data.get("productName"))));
        steps.put("quantity", new Step(
                data -> I18n.getString("flow.manageStock.askQuantity", data.get("lotNumber")),
                StringUtil::isNumeric,
                I18n.getString("flow.validation.invalidNumber")
        ));
        steps.put("confirmation", new Step(
                data -> I18n.getString("flow.manageStock.askConfirmation", data.get("quantity"), data.get("lotNumber"), data.get("productName")),
                input -> StringUtil.normalize(input).equals("sim") || StringUtil.normalize(input).equals("nao"),
                I18n.getString("flow.validation.confirmYesNo")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse(
                    I18n.getString("flow.manageStock.updating"),
                    Action.DIRECT_ADD_STOCK,
                    new HashMap<>(conversationData));
        } else {
            return new AssistantResponse(I18n.getString("flow.generic.actionCanceled"));
        }
    }
}