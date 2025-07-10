package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class QuerySalesComparisonFlow implements ConversationFlow {

    private final AnalyticsService analyticsService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Inject
    public QuerySalesComparisonFlow(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_SALES_COMPARISON;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        String normalizedInput = StringUtil.normalize(userInput);

        Optional<DateRange> period1Opt = getDateRange(normalizedInput, false);
        Optional<DateRange> period2Opt = getDateRange(normalizedInput, true);

        if (period1Opt.isEmpty() || period2Opt.isEmpty()) {
            return new AssistantResponse("Não consegui entender os períodos que você deseja comparar. Tente algo como 'compare as vendas de hoje com as de ontem'.");
        }

        DateRange period1 = period1Opt.get();
        DateRange period2 = period2Opt.get();

        try {
            double sales1 = analyticsService.getVendas(period1.start, period1.end);
            double sales2 = analyticsService.getVendas(period2.start, period2.end);

            String comparison;
            if (sales2 == 0) {
                comparison = sales1 > 0 ? " (não há vendas no período anterior para comparar)" : "";
            } else {
                double percentage = ((sales1 - sales2) / sales2) * 100;
                comparison = String.format(" (%.1f%% %s que %s)",
                        Math.abs(percentage),
                        percentage >= 0 ? "a mais" : "a menos",
                        period2.name);
            }

            String responseText = String.format("Vendas para '%s': %s.\nVendas para '%s': %s.\nResultado: %s vendeu %s%s.",
                    period1.name, currencyFormat.format(sales1),
                    period2.name, currencyFormat.format(sales2),
                    period1.name, currencyFormat.format(sales1), comparison
            );

            return new AssistantResponse(responseText);

        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao buscar os dados de vendas para a comparação.");
        }
    }

    private Optional<DateRange> getDateRange(String text, boolean findSecondPeriod) {
        String[] keywords = {"hoje", "ontem", "esta semana", "semana passada", "este mes", "mes passado"};
        String foundKeyword = null;

        int searchIndex = 0;
        if (findSecondPeriod) {
            Optional<DateRange> firstPeriod = getDateRange(text, false);
            if (firstPeriod.isPresent()) {
                searchIndex = text.indexOf(firstPeriod.get().keyword) + firstPeriod.get().keyword.length();
            }
        }

        for (String keyword : keywords) {
            if (text.indexOf(keyword, searchIndex) != -1) {
                foundKeyword = keyword;
                break;
            }
        }

        if (foundKeyword == null) return Optional.empty();

        LocalDate today = LocalDate.now();
        return switch (foundKeyword) {
            case "hoje" -> Optional.of(new DateRange("hoje", today, today, "hoje"));
            case "ontem" -> Optional.of(new DateRange("ontem", today.minusDays(1), today.minusDays(1), "ontem"));
            case "esta semana" -> Optional.of(new DateRange("esta semana", today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)), "esta semana"));
            case "semana passada" -> {
                LocalDate start = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate end = start.plusDays(6);
                yield Optional.of(new DateRange("semana passada", start, end, "semana passada"));
            }
            case "este mes" -> Optional.of(new DateRange("este mês", today.withDayOfMonth(1), today.with(TemporalAdjusters.lastDayOfMonth()), "este mês"));
            case "mes passado" -> {
                LocalDate start = today.minusMonths(1).withDayOfMonth(1);
                LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
                yield Optional.of(new DateRange("mês passado", start, end, "mês passado"));
            }
            default -> Optional.empty();
        };
    }

    private record DateRange(String name, LocalDate start, LocalDate end, String keyword) {}
}