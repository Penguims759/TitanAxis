package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AlertaService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

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
                return new AssistantResponse("Boas notícias! Nenhum produto está com o stock baixo no momento.");
            }

            String productList = produtos.stream()
                    .map(p -> String.format("- %s (%d unidades)", p.getNome(), p.getQuantidadeTotal()))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse("Encontrei os seguintes produtos com stock baixo:\n" + productList);

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar os dados de stock.");
        }
    }
}