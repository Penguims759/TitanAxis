package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class QueryClientCreditFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final ClienteRepository clienteRepository;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Inject
    public QueryClientCreditFlow(TransactionService transactionService, ClienteRepository clienteRepository) {
        this.transactionService = transactionService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_CLIENT_CREDIT;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        String clientName = (String) conversationData.get("entity");

        if (clientName == null) {
            // Tenta extrair o nome da pergunta inicial se não foi passado como entidade
            clientName = StringUtil.extractValueAfter(userInput, new String[]{"do cliente", "da cliente", "de"});
        }

        if (clientName == null || clientName.isEmpty()) {
            return new AssistantResponse("De qual cliente você gostaria de ver o crédito?");
        }

        // *** INÍCIO DA CORREÇÃO ***
        final String finalClientName = clientName.trim();
        // *** FIM DA CORREÇÃO ***

        try {
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(em ->
                    clienteRepository.findByNome(finalClientName, em)); // Usa a variável final

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                String creditValue = currencyFormat.format(cliente.getCredito());
                return new AssistantResponse(String.format("O cliente '%s' possui %s de crédito.", cliente.getNome(), creditValue));
            } else {
                return new AssistantResponse("Não consegui encontrar o cliente '" + finalClientName + "'. Por favor, verifique o nome.");
            }

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar o crédito do cliente.");
        }
    }
}