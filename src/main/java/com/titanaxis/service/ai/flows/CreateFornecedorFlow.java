// src/main/java/com/titanaxis/service/ai/flows/CreateFornecedorFlow.java
package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import java.util.Map;

public class CreateFornecedorFlow extends AbstractConversationFlow {
    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_FORNECEDOR;
    }

    @Override
    protected void defineSteps() {
        steps.put("nome", new Step("Ok, vamos cadastrar um novo fornecedor. Qual o nome dele?"));
        steps.put("contatoNome", new Step(data -> "Qual o nome da pessoa de contato no fornecedor '" + data.get("nome") + "'?"));
        steps.put("contatoTelefone", new Step("E o telefone de contato?"));
        steps.put("contatoEmail", new Step("Para finalizar, qual o email de contato?"));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        return new AssistantResponse(
                "Entendido. A criar o fornecedor '" + conversationData.get("nome") + "'...",
                Action.DIRECT_CREATE_FORNECEDOR,
                conversationData
        );
    }
}