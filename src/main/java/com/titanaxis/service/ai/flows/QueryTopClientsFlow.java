package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryTopClientsFlow implements ConversationFlow {

    private final AnalyticsService analyticsService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

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
            LocalDate startOfMonth = YearMonth.now().atDay(1);
            LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

            Map<String, Double> topClientsData = analyticsService.getTopClientes(startOfMonth, endOfMonth, 3);

            if (topClientsData.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.queryTopClients.noData"));
            }

            String topClientsText = topClientsData.entrySet().stream()
                    .map(entry -> I18n.getString("flow.queryTopClients.clientLine", entry.getKey(), currencyFormat.format(entry.getValue())))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse(I18n.getString("flow.queryTopClients.header") + "\n" + topClientsText);

        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.queryTopClients.error.generic"));
        }
    }
}