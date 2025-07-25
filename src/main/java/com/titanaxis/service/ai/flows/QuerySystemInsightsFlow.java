package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.util.List;
import java.util.Map;

public class QuerySystemInsightsFlow implements ConversationFlow {

    private final AnalyticsService analyticsService;

    @Inject
    public QuerySystemInsightsFlow(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_SYSTEM_INSIGHTS;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        try {
            List<String> insights = analyticsService.getSystemInsightsSummaryText();

            if (insights.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.queryInsights.noInsights"));
            }

            String responseText = I18n.getString("flow.queryInsights.header") + "\n" +
                    String.join("\n", insights);

            return new AssistantResponse(responseText);

        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.queryInsights.error.generic"));
        }
    }
}