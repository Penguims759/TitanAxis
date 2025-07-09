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
import com.titanaxis.util.StringUtil;

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
            if (data.containsKey("askedForProduct")) {
                productName = userInput;
            } else {
                data.put("askedForProduct", true);
                return new AssistantResponse("Qual produto você gostaria de consultar o stock?", Action.AWAITING_INFO, null);
            }
        }

        try {
            final String finalProductName = productName;
            Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(
                    em -> produtoRepository.findByNome(finalProductName, em)
            );

            if (produtoOpt.isPresent()) {
                Produto produto = produtoOpt.get();
                // NOVO: Coloca a entidade encontrada nos dados para ser guardada no contexto
                data.put("foundEntity", produto);
                int totalStock = produto.getQuantidadeTotal();
                return new AssistantResponse("O stock atual do produto '" + produto.getNome() + "' é de " + totalStock + " unidades.");
            } else {
                return new AssistantResponse("Não consegui encontrar o produto '" + finalProductName + "'. Por favor, verifique o nome.");
            }

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar a base de dados. Tente novamente.");
        }
    }
}