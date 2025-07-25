package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class UpdateProductFlow extends AbstractConversationFlow {

    private final FlowValidationService validationService;

    @Inject
    public UpdateProductFlow(FlowValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.UPDATE_PRODUCT;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        if (conversationData.get("entity") != null && !conversationData.containsKey("productName")) {
            String productNameFromContext = (String) conversationData.get("entity");
            if(validationService.isProdutoValido(productNameFromContext)){
                conversationData.put("productName", productNameFromContext);
            }
        }

        String flowStep = (String) conversationData.get("flow");
        if ("PRICE_UPDATE".equals(flowStep)) {
            return handlePriceUpdate(userInput, conversationData);
        } else if ("STATUS_UPDATE".equals(flowStep)) {
            return handleStatusUpdate(userInput, conversationData);
        } else if ("CONFIRM_UPDATE".equals(flowStep)) {
            if (userInput.equalsIgnoreCase("sim")) {
                return new AssistantResponse(I18n.getString("flow.updateProduct.updating"), Action.DIRECT_UPDATE_PRODUCT, conversationData);
            } else {
                conversationData.clear();
                return new AssistantResponse(I18n.getString("flow.generic.actionCanceled"));
            }
        }

        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("productName", new Step(
                I18n.getString("flow.updateProduct.askProductName"),
                (input, data) -> validationService.isProdutoValido(input),
                I18n.getString("flow.manageStock.validation.productNotFound")
        ));

        steps.put("updateType", new Step(
                data -> I18n.getString("flow.updateProduct.askUpdateType", data.get("productName")),
                input -> StringUtil.normalize(input).contains("preco") || StringUtil.normalize(input).contains("status"),
                I18n.getString("flow.updateProduct.validation.invalidType")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String updateType = StringUtil.normalize((String) conversationData.get("updateType"));
        if(updateType.contains("preco")){
            conversationData.put("flow", "PRICE_UPDATE");
            return new AssistantResponse(I18n.getString("flow.updateProduct.askNewPrice"), Action.AWAITING_INFO, null);
        } else {
            conversationData.put("flow", "STATUS_UPDATE");
            return new AssistantResponse(I18n.getString("flow.updateProduct.askNewStatus"), Action.AWAITING_INFO, null);
        }
    }

    private AssistantResponse handlePriceUpdate(String userInput, Map<String, Object> data) {
        if (StringUtil.isNumeric(userInput.replace(",", "."))) {
            data.put("newPrice", Double.parseDouble(userInput.replace(",", ".")));
            String confirmationMessage = I18n.getString("flow.updateProduct.confirmPrice", data.get("productName"), data.get("newPrice"));
            data.put("flow", "CONFIRM_UPDATE");
            return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
        }
        return new AssistantResponse(I18n.getString("flow.validation.invalidNumber"), Action.AWAITING_INFO, null);
    }

    private AssistantResponse handleStatusUpdate(String userInput, Map<String, Object> data) {
        String normalizedInput = StringUtil.normalize(userInput);
        if (normalizedInput.contains("ativar")) data.put("active", true);
        else if (normalizedInput.contains("inativar")) data.put("active", false);
        else {
            return new AssistantResponse(I18n.getString("flow.updateProduct.validation.invalidStatus"), Action.AWAITING_INFO, null);
        }
        String statusText = (Boolean) data.get("active") ? I18n.getString("flow.updateProduct.status.activation") : I18n.getString("flow.updateProduct.status.deactivation");
        String confirmationMessage = I18n.getString("flow.updateProduct.confirmStatus", statusText, data.get("productName"));
        data.put("flow", "CONFIRM_UPDATE");
        return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
    }
}