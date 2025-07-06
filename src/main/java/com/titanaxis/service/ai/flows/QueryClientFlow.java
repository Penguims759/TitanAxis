// src/main/java/com/titanaxis/service/ai/flows/QueryClientFlow.java
package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.service.AIAssistantService.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.util.Map;
import java.util.Optional;

public class QueryClientFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final ClienteRepository clienteRepository;

    @Inject
    public QueryClientFlow(TransactionService transactionService, ClienteRepository clienteRepository) {
        this.transactionService = transactionService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_CLIENT_DETAILS;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        // Tenta extrair o nome do cliente da pergunta inicial
        String clientName = StringUtil.extractFuzzyValueAfter(userInput, "cliente");

        if (clientName == null) {
            if (userInput.isEmpty() || isInitialCommand(userInput)) {
                return new AssistantResponse("Qual cliente você gostaria de consultar?");
            }
            clientName = userInput; // Assume que a resposta do utilizador é o nome do cliente
        }

        try {
            final String finalClientName = clientName;
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(
                    em -> clienteRepository.findByNome(finalClientName, em)
            );

            data.put("isFinal", true); // Esta conversa termina sempre aqui

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                String details = String.format(
                        "Aqui estão os detalhes do cliente '%s':\n- Nome: %s\n- Contato: %s\n- Endereço: %s",
                        cliente.getNome(),
                        cliente.getNome(),
                        cliente.getContato() != null ? cliente.getContato() : "Não informado",
                        cliente.getEndereco() != null ? cliente.getEndereco() : "Não informado"
                );
                return new AssistantResponse(details);
            } else {
                return new AssistantResponse("Não consegui encontrar o cliente '" + finalClientName + "'. Por favor, verifique o nome.");
            }

        } catch (PersistenciaException e) {
            data.put("isFinal", true);
            return new AssistantResponse("Ocorreu um erro ao consultar a base de dados. Tente novamente.");
        }
    }

    private boolean isInitialCommand(String userInput) {
        String normalized = StringUtil.normalize(userInput);
        return normalized.contains("detalhes") && normalized.contains("cliente");
    }
}