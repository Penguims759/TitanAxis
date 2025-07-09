// Caminho: penguims759/titanaxis/Penguims759-TitanAxis-d11978d74c8d39dd19a6d1a7bb798e37ccb09060/src/main/java/com/titanaxis/service/ai/flows/QueryClientHistoryFlow.java
package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.Map;
import java.util.Optional;

public class QueryClientHistoryFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final ClienteRepository clienteRepository;
    private final AnalyticsService analyticsService;

    @Inject
    public QueryClientHistoryFlow(TransactionService transactionService, ClienteRepository clienteRepository, AnalyticsService analyticsService) {
        this.transactionService = transactionService;
        this.clienteRepository = clienteRepository;
        this.analyticsService = analyticsService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_CLIENT_HISTORY;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        String clientName = (String) data.get("entity");
        if (clientName == null) {
            return new AssistantResponse("De qual cliente você gostaria de ver o histórico?", Action.AWAITING_INFO, null);
        }

        try {
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(em -> clienteRepository.findByNome(clientName, em));
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                data.put("foundEntity", cliente);
                String history = analyticsService.getClientPurchaseHistory(cliente.getId());
                return new AssistantResponse(history);
            } else {
                return new AssistantResponse("Não consegui encontrar o cliente '" + clientName + "'. Por favor, verifique o nome.");
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar a base de dados.");
        }
    }
}