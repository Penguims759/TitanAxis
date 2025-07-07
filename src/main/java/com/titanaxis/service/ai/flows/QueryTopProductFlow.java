package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.Map;

public class QueryTopProductFlow implements ConversationFlow {

    private final AnalyticsService analyticsService;

    @Inject
    public QueryTopProductFlow(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_TOP_PRODUCT;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        try {
            String topProduct = analyticsService.getTopSellingProduct();
            return new AssistantResponse("O produto mais vendido até agora é: " + topProduct);
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar os dados de vendas.");
        }
    }
}