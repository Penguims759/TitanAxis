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

public class QueryClientDetailsFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final ClienteRepository clienteRepository;

    @Inject
    public QueryClientDetailsFlow(TransactionService transactionService, ClienteRepository clienteRepository) {
        this.transactionService = transactionService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_CLIENT_DETAILS;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        String clientName = (String) data.get("entity");

        if (clientName == null) {
            return new AssistantResponse("Qual cliente você gostaria de consultar?", Action.AWAITING_INFO, null);
        }

        try {
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(
                    em -> clienteRepository.findByNome(clientName, em)
            );

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                data.put("foundEntity", cliente);
                String details = String.format(
                        "Aqui estão os detalhes do cliente '%s':\n- Nome: %s\n- Contato: %s\n- Endereço: %s",
                        cliente.getNome(),
                        cliente.getNome(),
                        cliente.getContato() != null ? cliente.getContato() : "Não informado",
                        cliente.getEndereco() != null ? cliente.getEndereco() : "Não informado"
                );
                return new AssistantResponse(details);
            } else {
                return new AssistantResponse("Não consegui encontrar o cliente '" + clientName + "'. Por favor, verifique o nome.");
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar a base de dados. Tente novamente.");
        }
    }
}