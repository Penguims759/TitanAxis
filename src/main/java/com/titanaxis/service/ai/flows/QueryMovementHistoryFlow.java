// src/main/java/com/titanaxis/service/ai/flows/QueryMovementHistoryFlow.java
package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.MovimentoRepository;
import com.titanaxis.service.AIAssistantService.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryMovementHistoryFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final MovimentoRepository movimentoRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    public QueryMovementHistoryFlow(TransactionService transactionService, MovimentoRepository movimentoRepository) {
        this.transactionService = transactionService;
        this.movimentoRepository = movimentoRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_MOVEMENT_HISTORY;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        String productName = StringUtil.extractFuzzyValueAfter(userInput, "produto");
        if (productName == null) {
            productName = StringUtil.extractFuzzyValueAfter(userInput, "de"); // Ex: histórico de Cadeira
        }

        if (productName == null) {
            if (userInput.isEmpty() || isInitialCommand(userInput)) {
                return new AssistantResponse("De qual produto você gostaria de ver o histórico de movimentos?");
            }
            productName = userInput;
        }

        try {
            final String finalProductName = productName;
            List<MovimentoEstoque> todosMovimentos = transactionService.executeInTransactionWithResult(
                    em -> movimentoRepository.findAll(em)
            );

            List<MovimentoEstoque> movimentosProduto = todosMovimentos.stream()
                    .filter(mov -> finalProductName.equalsIgnoreCase(StringUtil.normalize(mov.getNomeProduto())))
                    .collect(Collectors.toList());

            data.put("isFinal", true);

            if (movimentosProduto.isEmpty()) {
                return new AssistantResponse("Não encontrei nenhum histórico de movimentos para o produto '" + finalProductName + "'. Verifique se o produto existe e se teve movimentações.");
            }

            String historicoFormatado = movimentosProduto.stream()
                    .map(mov -> String.format(
                            "- %s: %s %d unidades (Lote: %s, Utilizador: %s)",
                            mov.getDataMovimento().format(DATE_TIME_FORMATTER),
                            mov.getTipoMovimento(),
                            mov.getQuantidade(),
                            mov.getNumeroLote(),
                            mov.getNomeUsuario()
                    ))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse("Este é o histórico de movimentos para '" + finalProductName + "':\n" + historicoFormatado);

        } catch (PersistenciaException e) {
            data.put("isFinal", true);
            return new AssistantResponse("Ocorreu um erro ao consultar o histórico. Tente novamente.");
        }
    }

    private boolean isInitialCommand(String userInput) {
        String normalized = StringUtil.normalize(userInput);
        return normalized.contains("historico") && normalized.contains("movimentos");
    }
}