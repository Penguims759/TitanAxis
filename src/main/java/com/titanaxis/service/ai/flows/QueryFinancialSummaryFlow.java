package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Map;

public class QueryFinancialSummaryFlow implements ConversationFlow {

    private final AnalyticsService analyticsService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Inject
    public QueryFinancialSummaryFlow(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_FINANCIAL_SUMMARY;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        try {
            YearMonth currentMonth = YearMonth.now();
            YearMonth previousMonth = currentMonth.minusMonths(1);

            double receitaMesAtual = analyticsService.getVendas(currentMonth.atDay(1), currentMonth.atEndOfMonth());
            double receitaMesAnterior = analyticsService.getVendas(previousMonth.atDay(1), previousMonth.atEndOfMonth());
            double ticketMedio = analyticsService.getTicketMedio(currentMonth.atDay(1), currentMonth.atEndOfMonth());

            String comparacao;
            if (receitaMesAnterior == 0) {
                comparacao = (receitaMesAtual > 0) ? " (não há dados do mês anterior para comparar)" : "";
            } else {
                double percentual = ((receitaMesAtual - receitaMesAnterior) / receitaMesAnterior) * 100;
                comparacao = String.format(" (%.1f%% %s em relação ao mês anterior)",
                        Math.abs(percentual),
                        percentual >= 0 ? "de aumento" : "de queda");
            }

            String responseText = String.format(
                    "Resumo do mês atual (%s):\n- Receita Total: %s%s\n- Ticket Médio: %s",
                    currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"))),
                    currencyFormat.format(receitaMesAtual),
                    comparacao,
                    currencyFormat.format(ticketMedio)
            );

            return new AssistantResponse(responseText);

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao consultar os dados financeiros. Por favor, tente novamente.");
        }
    }
}