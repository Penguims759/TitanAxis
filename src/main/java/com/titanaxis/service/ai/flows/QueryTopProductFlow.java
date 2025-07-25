package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

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
            if (topProduct.equals(I18n.getString("service.analytics.noSalesYet"))) {
                return new AssistantResponse(I18n.getString("flow.queryTopProduct.noSales"));
            }
            return new AssistantResponse(I18n.getString("flow.queryTopProduct.result", topProduct));
        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.queryTopProduct.error.generic"));
        }
    }
}