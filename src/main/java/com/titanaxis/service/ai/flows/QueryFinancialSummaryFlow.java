package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
                comparacao = (receitaMesAtual > 0) ? I18n.getString("flow.querySummary.noPreviousData") : "";
            } else {
                double percentual = ((receitaMesAtual - receitaMesAnterior) / receitaMesAnterior) * 100;
                String trend = percentual >= 0 ? I18n.getString("flow.querySummary.increase") : I18n.getString("flow.querySummary.decrease");
                comparacao = I18n.getString("flow.querySummary.comparison", Math.abs(percentual), trend);
            }

            String responseText = I18n.getString("flow.querySummary.summary",
                    currentMonth.format(DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"))),
                    currencyFormat.format(receitaMesAtual),
                    comparacao,
                    currencyFormat.format(ticketMedio)
            );

            return new AssistantResponse(responseText);

        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.querySummary.error.generic"));
        }
    }
}