package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
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

public class QueryProductLotsFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final ProdutoRepository produtoRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    public QueryProductLotsFlow(TransactionService transactionService, ProdutoRepository produtoRepository) {
        this.transactionService = transactionService;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_PRODUCT_LOTS;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        String productName = (String) data.get("entity");

        if (productName == null) {
            return new AssistantResponse(I18n.getString("flow.queryLots.askProductName"), Action.AWAITING_INFO, null);
        }

        try {
            Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(
                    em -> produtoRepository.findByNome(productName, em)
            );

            if (produtoOpt.isPresent()) {
                Produto produto = produtoOpt.get();
                data.put("foundEntity", produto);
                List<Lote> lotes = produto.getLotes();

                if (lotes.isEmpty()) {
                    return new AssistantResponse(I18n.getString("flow.queryLots.noLots", produto.getNome()));
                }

                String lotesDetails = lotes.stream()
                        .map(lote -> I18n.getString("flow.queryLots.lotLine",
                                lote.getNumeroLote(),
                                lote.getQuantidade(),
                                lote.getDataValidade() != null ? lote.getDataValidade().format(DATE_FORMATTER) : I18n.getString("general.notAvailable")
                        ))
                        .collect(Collectors.joining("\n"));

                return new AssistantResponse(I18n.getString("flow.queryLots.header", produto.getNome()) + "\n" + lotesDetails);
            } else {
                return new AssistantResponse(I18n.getString("flow.generic.error.entityNotFound", productName));
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.generic.error.persistence"));
        }
    }
}