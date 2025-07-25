package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.util.Map;
import java.util.Optional;

public class QueryStockFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final ProdutoRepository produtoRepository;

    @Inject
    public QueryStockFlow(TransactionService transactionService, ProdutoRepository produtoRepository) {
        this.transactionService = transactionService;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_STOCK;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        String productName = (String) data.get("entity");

        if (productName == null) {
            return new AssistantResponse(I18n.getString("flow.queryStock.askProductName"), Action.AWAITING_INFO, null);
        }

        try {
            Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(
                    em -> produtoRepository.findByNome(productName, em)
            );

            if (produtoOpt.isPresent()) {
                Produto produto = produtoOpt.get();
                data.put("foundEntity", produto);
                int totalStock = produto.getQuantidadeTotal();
                return new AssistantResponse(I18n.getString("flow.queryStock.currentStock", produto.getNome(), totalStock));
            } else {
                return new AssistantResponse(I18n.getString("flow.generic.error.entityNotFound", productName));
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.generic.error.persistence"));
        }
    }
}