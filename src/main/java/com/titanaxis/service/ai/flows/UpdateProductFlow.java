package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class UpdateProductFlow extends AbstractConversationFlow {

    private final TransactionService transactionService;
    private final FlowValidationService validationService;

    @Inject
    public UpdateProductFlow(TransactionService transactionService, FlowValidationService validationService) {
        this.transactionService = transactionService;
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
                return new AssistantResponse("Ok, a enviar a atualização...", Action.DIRECT_UPDATE_PRODUCT, conversationData);
            } else {
                conversationData.clear();
                return new AssistantResponse("Ok, ação cancelada.");
            }
        }

        return super.process(userInput, conversationData);
    }


    @Override
    protected void defineSteps() {
        steps.put("productName", new Step(
                "Qual produto você deseja alterar?",
                (input, data) -> validationService.isProdutoValido(input),
                "Não encontrei este produto. Por favor, verifique o nome."
        ));

        steps.put("updateType", new Step(
                data -> "O que você deseja alterar no produto '" + data.get("productName") + "'? (preço ou status)",
                input -> StringUtil.normalize(input).contains("preco") || StringUtil.normalize(input).contains("status"),
                "Não entendi. Você pode alterar o 'preço' ou o 'status'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String updateType = StringUtil.normalize((String) conversationData.get("updateType"));
        if(updateType.contains("preco")){
            conversationData.put("flow", "PRICE_UPDATE");
            return new AssistantResponse("Qual o novo preço?", Action.AWAITING_INFO, null);
        } else {
            conversationData.put("flow", "STATUS_UPDATE");
            return new AssistantResponse("Deseja 'ativar' ou 'inativar' o produto?", Action.AWAITING_INFO, null);
        }
    }

    private AssistantResponse handlePriceUpdate(String userInput, Map<String, Object> data) {
        if (StringUtil.isNumeric(userInput.replace(",", "."))) {
            data.put("newPrice", Double.parseDouble(userInput.replace(",", ".")));
            String confirmationMessage = String.format("Você confirma a alteração do preço do produto %s para %.2f? (sim/não)", data.get("productName"), data.get("newPrice"));
            data.put("flow", "CONFIRM_UPDATE");
            return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
        }
        return new AssistantResponse("Preço inválido. Por favor, digite um número.", Action.AWAITING_INFO, null);
    }

    private AssistantResponse handleStatusUpdate(String userInput, Map<String, Object> data) {
        String normalizedInput = StringUtil.normalize(userInput);
        if (normalizedInput.contains("ativar")) data.put("active", true);
        else if (normalizedInput.contains("inativar")) data.put("active", false);
        else {
            return new AssistantResponse("Não entendi. Deseja 'ativar' ou 'inativar'?", Action.AWAITING_INFO, null);
        }
        String confirmationMessage = String.format("Você confirma a %s do produto %s? (sim/não)", ((Boolean) data.get("active") ? "ativação" : "inativação"), data.get("productName"));
        data.put("flow", "CONFIRM_UPDATE");
        return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
    }
}