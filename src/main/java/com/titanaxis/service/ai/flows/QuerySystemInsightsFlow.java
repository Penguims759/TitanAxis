package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                return new AssistantResponse("Analisei o sistema e não encontrei nenhum ponto de atenção imediato. Está tudo em ordem!");
            }

            String responseText = "Claro, aqui estão os insights e recomendações que encontrei:\n" +
                    String.join("\n", insights);

            return new AssistantResponse(responseText);

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao tentar gerar os insights. Por favor, tente novamente.");
        }
    }
}