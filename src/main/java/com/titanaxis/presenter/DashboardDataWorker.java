package com.titanaxis.presenter;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.DashboardData;
import com.titanaxis.model.auditoria.Habito;
import com.titanaxis.service.AlertaService;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.UserHabitService;
import com.titanaxis.view.panels.HomePanel;

import javax.swing.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardDataWorker extends SwingWorker<DashboardData, Void> {

    private final HomePanel homePanel;
    private final AppContext appContext;
    private final String chartPeriod;

    public DashboardDataWorker(HomePanel homePanel, AppContext appContext, String chartPeriod) {
        this.homePanel = homePanel;
        this.appContext = appContext;
        this.chartPeriod = chartPeriod;
    }

    @Override
    protected DashboardData doInBackground() throws Exception {
        AnalyticsService analytics = appContext.getAnalyticsService();
        AlertaService alertas = appContext.getAlertaService();
        UserHabitService habitService = appContext.getUserHabitService();
        int userId = appContext.getAuthService().getUsuarioLogadoId();

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        double vendasHoje = analytics.getVendas(today, today);
        double receitaMes = analytics.getVendas(currentMonth.atDay(1), currentMonth.atEndOfMonth());
        int novosClientes = (int) analytics.getNovosClientes(currentMonth.atDay(1), currentMonth.atEndOfMonth());
        int numAlertas = alertas.getProdutosComEstoqueBaixo().size() + alertas.getLotesProximosDoVencimento().size();

        Map<?, Double> salesChartData = analytics.getVendasAgrupadas(chartPeriod);

        double receitaMesAnterior = analytics.getVendas(previousMonth.atDay(1), previousMonth.atEndOfMonth());
        double ticketMedio = analytics.getTicketMedio(currentMonth.atDay(1), currentMonth.atEndOfMonth());
        double ticketMedioMesAnterior = analytics.getTicketMedio(previousMonth.atDay(1), previousMonth.atEndOfMonth());

        Map<String, Integer> topProducts = analytics.getTopProdutos(currentMonth.atDay(1), currentMonth.atEndOfMonth(), 5);
        Map<String, Double> topClients = analytics.getTopClientes(currentMonth.atDay(1), currentMonth.atEndOfMonth(), 5);

        List<String> assistantInsights = new ArrayList<>(analytics.getSystemInsightsSummary());
        if (userId != 0) {
            habitService.findHabitsForToday(userId).stream()
                    .map(Habito::getSugestao)
                    .forEach(suggestion -> {
                        if (suggestion != null) {
                            assistantInsights.add("[HÁBITO] " + suggestion);
                        }
                    });
        }

        return new DashboardData(vendasHoje, receitaMes, novosClientes, numAlertas, salesChartData, receitaMesAnterior,
                ticketMedio, ticketMedioMesAnterior, topProducts, topClients, assistantInsights);
    }

    @Override
    protected void done() {
        try {
            DashboardData data = get();
            homePanel.updateUI(data);
        } catch (Exception e) {
            e.printStackTrace();
            homePanel.showErrorState(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } finally {
            homePanel.setLoadingState(false);
        }
    }
}