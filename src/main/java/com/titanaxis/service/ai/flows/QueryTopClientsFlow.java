package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.Map;

public class QueryTopClientsFlow implements ConversationFlow {

    private final AnalyticsService analyticsService;

    @Inject
    public QueryTopClientsFlow(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_TOP_CLIENTS;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        try {
            // O número '3' pode ser parametrizado se quisermos que o utilizador diga "top 5 clientes", por exemplo.
            String topClients = analyticsService.getTopBuyingClients(3);
            return new AssistantResponse(topClients);
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar os dados de vendas dos clientes.");
        }
    }
}