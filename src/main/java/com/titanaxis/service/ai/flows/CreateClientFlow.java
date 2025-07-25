package com.titanaxis.service.ai.flows;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.util.I18n;

import java.util.Map;

public class CreateClientFlow extends AbstractConversationFlow {

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_CLIENT;
    }

    @Override
    protected void defineSteps() {
        steps.put("nome", new Step(I18n.getString("flow.createClient.askName")));
        steps.put("contato", new Step(data -> I18n.getString("flow.createClient.askContact", data.get("nome"))));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String nome = (String) conversationData.get("nome");
        String contato = (String) conversationData.get("contato");

        Cliente novoCliente = new Cliente(nome, contato, "");
        conversationData.put("foundEntity", novoCliente);

        return new AssistantResponse(
                I18n.getString("flow.createClient.creating", nome),
                Action.DIRECT_CREATE_CLIENT,
                Map.of("nome", nome, "contato", contato)
        );
    }
}