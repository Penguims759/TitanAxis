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
import com.titanaxis.util.I18n;

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
            return new AssistantResponse(I18n.getString("flow.queryDetails.askClientName"), Action.AWAITING_INFO, null);
        }

        try {
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(
                    em -> clienteRepository.findByNome(clientName, em)
            );

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                data.put("foundEntity", cliente);
                String details = I18n.getString("flow.queryDetails.details",
                        cliente.getNome(),
                        cliente.getNome(),
                        cliente.getContato() != null ? cliente.getContato() : I18n.getString("general.notSpecified"),
                        cliente.getEndereco() != null ? cliente.getEndereco() : I18n.getString("general.notSpecified")
                );
                return new AssistantResponse(details);
            } else {
                return new AssistantResponse(I18n.getString("flow.generic.error.entityNotFound", clientName));
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.generic.error.persistence"));
        }
    }
}