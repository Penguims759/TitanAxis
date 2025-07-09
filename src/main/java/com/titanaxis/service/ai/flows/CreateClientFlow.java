// Caminho: penguims759/titanaxis/Penguims759-TitanAxis-d11978d74c8d39dd19a6d1a7bb798e37ccb09060/src/main/java/com/titanaxis/service/ai/flows/CreateClientFlow.java
package com.titanaxis.service.ai.flows;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import java.util.Map;

public class CreateClientFlow extends AbstractConversationFlow {

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_CLIENT;
    }

    @Override
    protected void defineSteps() {
        steps.put("nome", new Step("Vamos criar um novo cliente. Qual é o nome dele?"));
        steps.put("contato", new Step(data -> "Ok, o nome é '" + data.get("nome") + "'. Qual o contato (email/telefone)?"));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String nome = (String) conversationData.get("nome");
        String contato = (String) conversationData.get("contato");

        Cliente novoCliente = new Cliente(nome, contato, "");
        // Coloca a entidade criada nos dados para ser guardada no contexto pelo serviço
        conversationData.put("foundEntity", novoCliente);

        return new AssistantResponse(
                "Entendido! A criar o cliente '" + nome + "'.",
                Action.DIRECT_CREATE_CLIENT,
                Map.of("nome", nome, "contato", contato)
        );
    }
}