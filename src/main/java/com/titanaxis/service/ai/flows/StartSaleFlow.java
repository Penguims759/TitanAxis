package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.StringUtil;
import java.util.Map;
import java.util.Optional;

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
                "Para qual cliente é a venda? (Opcional, pode deixar em branco)",
                (input, data) -> isClientNameValidOrEmpty(input),
                "Cliente não encontrado. Verifique o nome ou deixe em branco para continuar."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String clientName = (String) conversationData.get("clientName");
        if (clientName == null || clientName.trim().isEmpty() || isClientless(clientName)) {
            return new AssistantResponse("Ok, a abrir o painel de vendas.", Action.UI_NAVIGATE, Map.of("destination", "Vendas"));
        }
        return validateAndFinish(clientName, conversationData);
    }

    private AssistantResponse validateAndFinish(String clientName, Map<String, Object> data) {
        if (validationService.isClienteValido(clientName)) {
            data.put("cliente", new Cliente(clientName, "", "")); // Simples DTO por agora
            return new AssistantResponse(
                    "Ok, a iniciar a venda para o cliente " + clientName,
                    Action.START_SALE_FOR_CLIENT,
                    data
            );
        } else {
            return new AssistantResponse("Cliente '" + clientName + "' não encontrado. Gostaria de o criar primeiro?");
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