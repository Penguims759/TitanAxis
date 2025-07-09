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
import com.titanaxis.util.StringUtil;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        String clientName = (String) conversationData.get("entity");

        if (clientName == null) {
            if (conversationData.containsKey("askedForClient")) {
                clientName = userInput;
            } else {
                conversationData.put("askedForClient", true);
                return new AssistantResponse("De qual cliente você gostaria de ver o histórico?", Action.AWAITING_INFO, null);
            }
        }

        try {
            final String finalClientName = clientName;
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(
                    em -> clienteRepository.findByNome(finalClientName, em)
            );

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                String history = analyticsService.getClientPurchaseHistory(cliente.getId());
                return new AssistantResponse(history);
            } else {
                return new AssistantResponse("Não consegui encontrar o cliente '" + finalClientName + "'. Por favor, verifique o nome.");
            }

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar a base de dados.");
        }
    }
}