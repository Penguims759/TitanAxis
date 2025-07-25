package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.MovimentoRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class QueryMovementHistoryFlow implements ConversationFlow {
    private final TransactionService transactionService;
    private final MovimentoRepository movimentoRepository;
    private final ProdutoRepository produtoRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    public QueryMovementHistoryFlow(TransactionService transactionService, MovimentoRepository movimentoRepository, ProdutoRepository produtoRepository) {
        this.transactionService = transactionService;
        this.movimentoRepository = movimentoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_MOVEMENT_HISTORY;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        String productName = (String) data.get("entity");
        if (productName == null) {
            return new AssistantResponse(I18n.getString("flow.queryMovement.askProductName"), Action.AWAITING_INFO, null);
        }

        try {
            Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(em -> produtoRepository.findByNome(productName, em));
            if (produtoOpt.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.generic.error.entityNotFound", productName));
            }

            data.put("foundEntity", produtoOpt.get());

            List<MovimentoEstoque> movimentos = transactionService.executeInTransactionWithResult(em -> movimentoRepository.findAll(em));
            List<MovimentoEstoque> movimentosProduto = movimentos.stream().filter(mov -> mov.getProduto() != null && mov.getProduto().getId() == produtoOpt.get().getId()).collect(Collectors.toList());

            if (movimentosProduto.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.queryMovement.noHistory", productName));
            }

            String historicoFormatado = movimentosProduto.stream()
                    .map(mov -> I18n.getString("flow.queryMovement.historyLine",
                            mov.getDataMovimento().format(DATE_TIME_FORMATTER),
                            mov.getTipoMovimento(),
                            mov.getQuantidade(),
                            mov.getNumeroLote(),
                            mov.getNomeUsuario()))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse(I18n.getString("flow.queryMovement.header", productName) + "\n" + historicoFormatado);
        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.queryMovement.error.generic"));
        }
    }
}