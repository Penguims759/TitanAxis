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
                I18n.getString("flow.adjustStock.askProductName"),
                (input, data) -> validationService.isProdutoValido(input),
                I18n.getString("flow.manageStock.validation.productNotFound") // Reutilizando
        ));
        steps.put("lotNumber", new Step(
                data -> I18n.getString("flow.adjustStock.askLotNumber", data.get("productName")),
                (input, data) -> validationService.isLoteValido((String) data.get("productName"), input),
                I18n.getString("flow.adjustStock.validation.lotNotFound")
        ));
        steps.put("quantity", new Step(
                I18n.getString("flow.adjustStock.askQuantity"),
                StringUtil::isNumeric,
                I18n.getString("flow.validation.invalidNumber")
        ));
        steps.put("confirmation", new Step(
                data -> {
                    int quantity = Integer.parseInt((String) data.get("quantity"));
                    data.put("quantity", quantity);
                    return I18n.getString("flow.adjustStock.askConfirmation",
                            data.get("lotNumber"), data.get("productName"), quantity);
                },
                input -> StringUtil.normalize(input).equals("sim") || StringUtil.normalize(input).equals("nao"),
                I18n.getString("flow.validation.confirmYesNo")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse(
                    I18n.getString("flow.adjustStock.updating"),
                    Action.DIRECT_ADJUST_STOCK,
                    new HashMap<>(conversationData));
        } else {
            return new AssistantResponse(I18n.getString("flow.generic.actionCanceled"));
        }
    }
}