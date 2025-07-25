package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.util.I18n;

import java.util.Map;

public class CreateFornecedorFlow extends AbstractConversationFlow {
    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_FORNECEDOR;
    }

    @Override
    protected void defineSteps() {
        steps.put("nome", new Step(I18n.getString("flow.createSupplier.askName")));
        steps.put("contatoNome", new Step(data -> I18n.getString("flow.createSupplier.askContactName", data.get("nome"))));
        steps.put("contatoTelefone", new Step(I18n.getString("flow.createSupplier.askContactPhone")));
        steps.put("contatoEmail", new Step(I18n.getString("flow.createSupplier.askContactEmail")));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        return new AssistantResponse(
                I18n.getString("flow.createSupplier.creating", conversationData.get("nome")),
                Action.DIRECT_CREATE_FORNECEDOR,
                conversationData
        );
    }
}