package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class StartSaleFlow extends AbstractConversationFlow {

    private final FlowValidationService validationService;

    @Inject
    public StartSaleFlow(FlowValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.START_SALE;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        String clientName = (String) conversationData.get("entity");

        if (clientName != null) {
            return validateAndFinish(clientName, conversationData);
        }

        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("clientName", new Step(
                I18n.getString("flow.startSale.askClientName"),
                (input, data) -> isClientNameValidOrEmpty(input),
                I18n.getString("flow.startSale.validation.clientNotFound")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String clientName = (String) conversationData.get("clientName");
        if (clientName == null || clientName.trim().isEmpty() || isClientless(clientName)) {
            return new AssistantResponse(I18n.getString("flow.startSale.openingPanel"), Action.UI_NAVIGATE, Map.of("destination", "Vendas"));
        }
        return validateAndFinish(clientName, conversationData);
    }

    private AssistantResponse validateAndFinish(String clientName, Map<String, Object> data) {
        if (validationService.isClienteValido(clientName)) {
            // Criamos um objeto temporário para passar o nome do cliente
            Cliente clienteTemp = new Cliente();
            clienteTemp.setNome(clientName);
            data.put("cliente", clienteTemp);
            data.put("foundEntity", clienteTemp);
            return new AssistantResponse(
                    I18n.getString("flow.startSale.forClient", clientName),
                    Action.START_SALE_FOR_CLIENT,
                    data
            );
        } else {
            return new AssistantResponse(I18n.getString("flow.startSale.askCreateClient", clientName));
        }
    }

    private boolean isClientless(String input) {
        String normalized = StringUtil.normalize(input);
        return normalized.contains("sem cliente") || normalized.equals("nenhum") || normalized.equals("branco");
    }

    private boolean isClientNameValidOrEmpty(String name) {
        if (name == null || name.trim().isEmpty() || isClientless(name)) {
            return true;
        }
        return validationService.isClienteValido(name);
    }
}