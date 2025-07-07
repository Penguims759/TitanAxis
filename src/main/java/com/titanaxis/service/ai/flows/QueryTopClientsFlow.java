package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.text.NumberFormat;
import java.util.stream.Collectors;

public class QueryTopClientsFlow implements ConversationFlow {

    private final AnalyticsService analyticsService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "PT"));

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
            // Define o período como o mês atual
            LocalDate startOfMonth = YearMonth.now().atDay(1);
            LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

            Map<String, Double> topClientsData = analyticsService.getTopClientes(startOfMonth, endOfMonth, 3);

            if (topClientsData.isEmpty()) {
                return new AssistantResponse("Ainda não há dados de vendas suficientes este mês para gerar um ranking de clientes.");
            }

            String topClientsText = topClientsData.entrySet().stream()
                    .map(entry -> String.format("- %s (%s)", entry.getKey(), currencyFormat.format(entry.getValue())))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse("Os melhores clientes deste mês são:\n" + topClientsText);

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar os dados de vendas dos clientes.");
        }
    }
}