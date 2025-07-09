// Conteúdo já fornecido e correto na resposta anterior
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
            // Se a entidade não foi fornecida, o fluxo precisa de perguntar.
            return new AssistantResponse("De qual produto você gostaria de ver o stock?", Action.AWAITING_INFO, null);
        }

        try {
            Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(
                    em -> produtoRepository.findByNome(productName, em)
            );

            if (produtoOpt.isPresent()) {
                Produto produto = produtoOpt.get();
                data.put("foundEntity", produto); // Guarda a entidade encontrada para o contexto
                int totalStock = produto.getQuantidadeTotal();
                return new AssistantResponse("O stock atual do produto '" + produto.getNome() + "' é de " + totalStock + " unidades.");
            } else {
                return new AssistantResponse("Não consegui encontrar o produto '" + productName + "'. Por favor, verifique o nome.");
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar a base de dados. Tente novamente.");
        }
    }
}