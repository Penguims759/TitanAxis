package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AlertaService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryLowStockFlow implements ConversationFlow {

    private final AlertaService alertaService;

    @Inject
    public QueryLowStockFlow(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_LOW_STOCK;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        try {
            List<Produto> produtos = alertaService.getProdutosComEstoqueBaixo();
            if (produtos.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.queryLowStock.noProducts"));
            }

            String productList = produtos.stream()
                    .map(p -> I18n.getString("flow.queryLowStock.productLine", p.getNome(), p.getQuantidadeTotal()))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse(I18n.getString("flow.queryLowStock.header") + "\n" + productList);

        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.queryLowStock.error.generic"));
        }
    }
}