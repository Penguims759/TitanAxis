// Caminho: penguims759/titanaxis/Penguims759-TitanAxis-d11978d74c8d39dd19a6d1a7bb798e37ccb09060/src/main/java/com/titanaxis/service/ai/flows/StartSaleFlow.java
package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.Map;
import java.util.Optional;

public class StartSaleFlow extends AbstractConversationFlow {

    private final TransactionService transactionService;
    private final ClienteRepository clienteRepository;

    @Inject
    public StartSaleFlow(TransactionService transactionService, ClienteRepository clienteRepository) {
        this.transactionService = transactionService;
        this.clienteRepository = clienteRepository;
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
                this::isClientNameValidOrEmpty,
                "Cliente não encontrado. Verifique o nome ou deixe em branco para continuar."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String clientName = (String) conversationData.get("clientName");
        if (clientName == null || clientName.trim().isEmpty()) {
            return new AssistantResponse("Ok, a abrir o painel de vendas.", Action.UI_NAVIGATE, Map.of("destination", "Vendas"));
        }
        return validateAndFinish(clientName, conversationData);
    }

    private AssistantResponse validateAndFinish(String clientName, Map<String, Object> data) {
        try {
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(em ->
                    clienteRepository.findByNome(clientName, em));

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                data.put("cliente", cliente);
                data.put("foundEntity", cliente);
                return new AssistantResponse(
                        "Ok, a iniciar a venda para o cliente " + clientName,
                        Action.START_SALE_FOR_CLIENT,
                        data
                );
            } else {
                return new AssistantResponse("Cliente '" + clientName + "' não encontrado. Gostaria de o criar primeiro?");
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao verificar o cliente na base de dados.");
        }
    }

    private boolean isClientNameValidOrEmpty(String name) {
        if (name == null || name.trim().isEmpty()) {
            return true;
        }
        try {
            return transactionService.executeInTransactionWithResult(em ->
                    clienteRepository.findByNome(name, em)
            ).isPresent();
        } catch (PersistenciaException e) {
            return false;
        }
    }
}