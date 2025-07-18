package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.DashboardData;
import com.titanaxis.model.Usuario;
import com.titanaxis.presenter.DashboardDataWorker;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.dashboard.*;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Map;

public class HomePanel extends JPanel implements DashboardFrame.Refreshable {

    private final AppContext appContext;
    private final DashboardFrame parentFrame;

    // --- Componentes do Dashboard ---
    private KPICardPanel kpiSalesCard, kpiClientsCard, kpiAlertsCard;
    private FinancialSummaryCard financialSummaryCard;
    private QuickActionsPanel quickActionsPanel;
    private SimpleGoalsCard goalsCard;
    private AssistantInsightsPanel assistantInsightsPanel;
    private SalesChartPanel salesChartPanel;
    private CategoryPerformancePanel categoryPerformancePanel;

    private String selectedChartPeriod = "7D";
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public HomePanel(AppContext appContext, DashboardFrame parentFrame) {
        this.appContext = appContext;
        this.parentFrame = parentFrame;
        rebuildUI();
        refreshData();
    }

    public void rebuildUI() {
        removeAll();
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Linha 0: Cabeçalho ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        add(createHeaderPanel(), gbc);

        // --- Linha 1 ---
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0.75; // Linha de cima fica com 40% da altura
        gbc.fill = GridBagConstraints.BOTH;

        // Coluna 0: Relatório do Assistente
        gbc.gridx = 0;
        gbc.weightx = 0; // Não estica
        gbc.insets = new Insets(0, 0, 15, 15);
        add(createAssistantReportPanel(), gbc);

        // Coluna 1: Gráfico de Barras (com o wrapper)
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Ocupa todo o resto
        add(createStableChartPanel(), gbc);

        // Coluna 2: Ações, Metas e Comparações
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0; // Não estica
        gbc.gridheight = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(createRightColumn(), gbc);

        // --- Linha 2 ---
        gbc.gridy = 2;
        gbc.gridheight = 1;
        gbc.weighty = 0.25; // Linha de baixo (gráfico de linhas) fica com 60% da altura

        // Coluna 0 e 1: Novo Painel de Performance de Categorias
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 15);
        categoryPerformancePanel = new CategoryPerformancePanel();
        add(categoryPerformancePanel, gbc);

        revalidate();
        repaint();
    }

    private JComponent createHeaderPanel() {
        JPanel header = new JPanel(new GridLayout(1, 4, 10, 0));
        header.setOpaque(false);

        // Painel de saudação
        JPanel greetingPanel = new JPanel(new BorderLayout());
        greetingPanel.setOpaque(false);
        greetingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(70, 130, 180)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel textContainer = new JPanel();
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.setOpaque(false);

        String username = appContext.getAuthService().getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("");
        JLabel mainGreetingLabel = new JLabel("<html>" + getGreetingByTimeOfDay() + " " + username + "!</html>");
        mainGreetingLabel.setFont(new Font("Arial", Font.BOLD, 28));
        textContainer.add(mainGreetingLabel);

        JLabel subGreetingLabel = new JLabel("- O assistente");
        subGreetingLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        textContainer.add(subGreetingLabel);

        greetingPanel.add(textContainer, BorderLayout.CENTER);

        // KPIs
        kpiSalesCard = new KPICardPanel(I18n.getString("home.kpi.salesToday"), I18n.getString("home.kpi.salesToday.tooltip"));
        kpiClientsCard = new KPICardPanel(I18n.getString("home.kpi.newClients"), I18n.getString("home.kpi.newClients.tooltip"));
        kpiAlertsCard = new KPICardPanel(I18n.getString("home.kpi.stockAlerts"), I18n.getString("home.kpi.stockAlerts.tooltip"));

        header.add(greetingPanel);
        header.add(kpiSalesCard);
        header.add(kpiClientsCard);
        header.add(kpiAlertsCard);

        return header;
    }

    private JComponent createAssistantReportPanel() {
        assistantInsightsPanel = new AssistantInsightsPanel();
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(assistantInsightsPanel, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(350, 0));
        return wrapper;
    }

    private JComponent createStableChartPanel() {
        JPanel chartWrapperPanel = new JPanel(new BorderLayout());
        chartWrapperPanel.setBorder(BorderFactory.createTitledBorder("Evolução de Vendas"));

        salesChartPanel = new SalesChartPanel(this::onPeriodChange);
        chartWrapperPanel.add(salesChartPanel, BorderLayout.CENTER);

        return chartWrapperPanel;
    }


    private JComponent createRightColumn() {
        JPanel column = new JPanel(new BorderLayout(0, 15));
        column.setOpaque(false);

        quickActionsPanel = new QuickActionsPanel(parentFrame::navigateTo);
        financialSummaryCard = new FinancialSummaryCard();

        JPanel topOfRightColumn = new JPanel();
        topOfRightColumn.setOpaque(false);
        topOfRightColumn.setLayout(new BoxLayout(topOfRightColumn, BoxLayout.Y_AXIS));
        topOfRightColumn.add(quickActionsPanel);
        topOfRightColumn.add(Box.createVerticalStrut(15));
        topOfRightColumn.add(financialSummaryCard);

        goalsCard = new SimpleGoalsCard(appContext);

        column.add(topOfRightColumn, BorderLayout.NORTH);
        column.add(goalsCard, BorderLayout.CENTER);

        return column;
    }

    private void onPeriodChange(String newPeriod) {
        this.selectedChartPeriod = newPeriod;
        salesChartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new ChartDataWorker(appContext.getAnalyticsService(), newPeriod).execute();
    }

    @Override
    public void refreshData() {
        setLoadingState(true);
        new DashboardDataWorker(this, appContext, selectedChartPeriod, parentFrame).execute();
    }

    public void setLoadingState(boolean isLoading) {
        setCursor(isLoading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        String status = isLoading ? I18n.getString("general.loading") : "---";

        if(isLoading) {
            if (kpiSalesCard != null) kpiSalesCard.setValue(status);
            if (kpiClientsCard != null) kpiClientsCard.setValue(status);
            if (kpiAlertsCard != null) kpiAlertsCard.setValue(status);

            if (financialSummaryCard != null) {
                financialSummaryCard.setRevenue(status);
                financialSummaryCard.setAvgTicket(status);
                financialSummaryCard.setComparison(Double.NaN);
            }
            if (salesChartPanel != null) salesChartPanel.setData(null, selectedChartPeriod);
            if (assistantInsightsPanel != null) assistantInsightsPanel.setInsights(null);
            if (categoryPerformancePanel != null) categoryPerformancePanel.setData(null, null);
        }
    }

    public void showErrorState(String errorMessage) {
        UIMessageUtil.showErrorMessage(this, "Erro ao carregar dados do dashboard: " + errorMessage, "Erro de Carregamento");
    }

    private class ChartDataWorker extends SwingWorker<Map<?, Double>, Void> {
        private final AnalyticsService analyticsService;
        private final String period;

        public ChartDataWorker(AnalyticsService analyticsService, String period) {
            this.analyticsService = analyticsService;
            this.period = period;
        }

        @Override
        protected Map<?, Double> doInBackground() throws Exception {
            return analyticsService.getVendasAgrupadas(period);
        }

        @Override
        protected void done() {
            try {
                Map<?, Double> data = get();
                updateChartData(data);
            } catch (Exception e) {
                showErrorState(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            } finally {
                salesChartPanel.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    public void updateChartData(Map<?, Double> chartData) {
        if (salesChartPanel != null) {
            salesChartPanel.setData(chartData, selectedChartPeriod);
        }
    }


    public void updateUI(DashboardData data) {
        kpiSalesCard.setValue(currencyFormat.format(data.vendasHoje));
        kpiClientsCard.setValue(String.valueOf(data.novosClientes));
        kpiAlertsCard.setValue(String.valueOf(data.numAlertas));
        kpiAlertsCard.setValueColor(data.numAlertas > 0 ? Color.RED : UIManager.getColor("Label.foreground"));

        salesChartPanel.setData(data.salesChartData, selectedChartPeriod);

        financialSummaryCard.setRevenue(currencyFormat.format(data.receitaMes));
        financialSummaryCard.setAvgTicket(currencyFormat.format(data.ticketMedio));
        double comparison = (data.receitaMesAnterior == 0 && data.receitaMes > 0) ? 100.0 :
                (data.receitaMesAnterior == 0) ? Double.NaN :
                        ((data.receitaMes - data.receitaMesAnterior) / data.receitaMesAnterior) * 100;
        financialSummaryCard.setComparison(comparison);

        if (assistantInsightsPanel != null) {
            assistantInsightsPanel.setInsights(data.assistantInsights);
        }

        if (categoryPerformancePanel != null) {
            categoryPerformancePanel.setData(data.categorySalesEvolution, data.categoryTrends);
        }

        if(goalsCard != null) goalsCard.refreshData();

        revalidate();
        repaint();
    }

    private String getGreetingByTimeOfDay() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.NOON)) return I18n.getString("dashboard.greeting.morning");
        if (now.isBefore(LocalTime.of(18, 0))) return I18n.getString("dashboard.greeting.afternoon");
        return I18n.getString("dashboard.greeting.evening");
    }
}