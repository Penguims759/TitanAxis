package com.titanaxis.view.panels; // <<< PACOTE CORRETO

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.auditoria.Habito;
import com.titanaxis.service.AlertaService;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
import com.titanaxis.view.DashboardFrame;
// IMPORTAÇÕES CORRIGIDAS PARA O SUBPACOTE 'dashboard'
import com.titanaxis.view.panels.dashboard.ActivityCardPanel;
import com.titanaxis.view.panels.dashboard.FinancialSummaryCard;
import com.titanaxis.view.panels.dashboard.KPICardPanel;
import com.titanaxis.view.panels.dashboard.PerformanceRankingsCard;
import com.titanaxis.view.panels.dashboard.QuickActionsPanel;
import com.titanaxis.view.panels.dashboard.SalesChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomePanel extends JPanel implements DashboardFrame.Refreshable {

    private final AppContext appContext;
    private final UIPersonalizationService personalizationService;
    private static final Logger logger = AppLogger.getLogger();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // --- Componentes da UI ---
    private KPICardPanel vendasHojeCard;
    private KPICardPanel novosClientesCard;
    private KPICardPanel alertasCard;
    private FinancialSummaryCard financialSummaryCard;
    private PerformanceRankingsCard performanceRankingsCard;
    private SalesChartPanel salesChart;
    private ActivityCardPanel activityPanel;

    private String selectedChartPeriod = "7D";

    public HomePanel(AppContext appContext) {
        this.appContext = appContext;
        this.personalizationService = new UIPersonalizationService(appContext.getAuthService().getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("default"));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        rebuildUI();
    }

    public void rebuildUI() {
        this.removeAll();

        if (isCardVisible("kpi_cards")) add(createKpiPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        JPanel leftColumn = createLeftColumn();
        JPanel rightColumn = createRightColumn();

        if (leftColumn != null) {
            leftColumn.setPreferredSize(new Dimension(320, 1));
            centerPanel.add(leftColumn, BorderLayout.WEST);
        }
        if (rightColumn != null) centerPanel.add(rightColumn, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        if (isCardVisible("quick_actions")) add(createQuickActionsPanel(), BorderLayout.SOUTH);

        revalidate();
        repaint();
        refreshData();
    }

    @Override
    public void refreshData() {
        logger.info(I18n.getString("home.log.refreshing"));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setLoadingState(true);

        SwingWorker<DashboardData, Exception> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                AnalyticsService analytics = appContext.getAnalyticsService();
                AlertaService alertas = appContext.getAlertaService();
                LocalDate today = LocalDate.now();
                YearMonth currentMonth = YearMonth.from(today);
                YearMonth previousMonth = currentMonth.minusMonths(1);

                return new DashboardData(
                        analytics.getVendas(today, today),
                        analytics.getNovosClientes(currentMonth.atDay(1), currentMonth.atEndOfMonth()),
                        alertas.getProdutosComEstoqueBaixo().size(),
                        analytics.getVendas(currentMonth.atDay(1), currentMonth.atEndOfMonth()),
                        analytics.getVendas(previousMonth.atDay(1), previousMonth.atEndOfMonth()),
                        analytics.getTicketMedio(currentMonth.atDay(1), currentMonth.atEndOfMonth()),
                        analytics.getTopProdutos(currentMonth.atDay(1), currentMonth.atEndOfMonth(), 3),
                        analytics.getTopClientes(currentMonth.atDay(1), currentMonth.atEndOfMonth(), 3),
                        analytics.getVendasAgrupadas(selectedChartPeriod),
                        analytics.getRecentActivity(7)
                );
            }

            @Override
            protected void done() {
                setLoadingState(false);
                try {
                    DashboardData data = get();
                    logger.info(I18n.getString("home.log.dataLoaded"));
                    updateUICards(data);
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, I18n.getString("home.log.dataError"), e.getCause());
                    handleDataLoadError(e.getCause());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private JPanel createLeftColumn() {
        boolean financialVisible = isCardVisible("financial_summary");
        boolean rankingsVisible = isCardVisible("performance_rankings");

        if (!financialVisible && !rankingsVisible) return null;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (financialVisible) {
            financialSummaryCard = new FinancialSummaryCard();
            panel.add(financialSummaryCard);
        }
        if (financialVisible && rankingsVisible) panel.add(Box.createVerticalStrut(15));
        if (rankingsVisible) {
            performanceRankingsCard = new PerformanceRankingsCard();
            panel.add(performanceRankingsCard);
        }

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createRightColumn() {
        boolean chartVisible = isCardVisible("sales_chart");
        boolean activityVisible = isCardVisible("recent_activity");

        if (!chartVisible && !activityVisible) return null;

        JPanel panel = new JPanel(new BorderLayout(15, 0));

        if (chartVisible) {
            salesChart = new SalesChartPanel(this::onChartPeriodChange);
            JPanel chartWrapperPanel = new JPanel(new BorderLayout());
            chartWrapperPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("home.chart.title")));
            chartWrapperPanel.add(salesChart, BorderLayout.CENTER);
            panel.add(chartWrapperPanel, BorderLayout.CENTER);
        }

        if (activityVisible) {
            activityPanel = new ActivityCardPanel();
            activityPanel.setPreferredSize(new Dimension(250, 0));
            panel.add(activityPanel, BorderLayout.EAST);
        }

        return panel;
    }

    private JPanel createKpiPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 15, 15));
        vendasHojeCard = new KPICardPanel(I18n.getString("home.kpi.salesToday"), I18n.getString("home.kpi.salesToday.tooltip"));
        novosClientesCard = new KPICardPanel(I18n.getString("home.kpi.newClients"), I18n.getString("home.kpi.newClients.tooltip"));
        alertasCard = new KPICardPanel(I18n.getString("home.kpi.stockAlerts"), I18n.getString("home.kpi.stockAlerts.tooltip"));

        panel.add(vendasHojeCard);
        panel.add(novosClientesCard);
        panel.add(alertasCard);

        vendasHojeCard.addMouseListener(createNavigationMouseAdapter(I18n.getString("dashboard.tab.history")));
        novosClientesCard.addMouseListener(createNavigationMouseAdapter(I18n.getString("dashboard.tab.clients")));
        alertasCard.addMouseListener(createNavigationMouseAdapter(I18n.getString("dashboard.tab.stockAlerts")));

        return panel;
    }

    private QuickActionsPanel createQuickActionsPanel() {
        QuickActionsPanel panel = new QuickActionsPanel();
        DashboardFrame parentFrame = (DashboardFrame) SwingUtilities.getWindowAncestor(this);

        if (parentFrame != null) {
            panel.newSaleButton.addActionListener(e -> parentFrame.navigateTo(I18n.getString("dashboard.tab.newSale")));
            panel.newProductButton.addActionListener(e -> {
                parentFrame.navigateTo(I18n.getString("dashboard.tab.productsAndStock"));
                parentFrame.navigateTo(I18n.getString("dashboard.tab.productsAndBatches"));
            });
            panel.newClientButton.addActionListener(e -> {
                parentFrame.navigateTo(I18n.getString("dashboard.tab.registrations"));
                parentFrame.navigateTo(I18n.getString("dashboard.tab.clients"));
            });
        }
        return panel;
    }

    private MouseAdapter createNavigationMouseAdapter(String destination) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DashboardFrame parentFrame = (DashboardFrame) SwingUtilities.getWindowAncestor(HomePanel.this);
                if (parentFrame != null) {
                    parentFrame.navigateTo(destination);
                }
            }
        };
    }

    private void onChartPeriodChange(String newPeriod) {
        this.selectedChartPeriod = newPeriod;
        refreshChartData();
    }

    private boolean isCardVisible(String cardKey) {
        return Boolean.parseBoolean(personalizationService.getPreference("dashboard.card." + cardKey, "true"));
    }

    private void refreshChartData() {
        if(salesChart == null) return;
        salesChart.setData(Collections.emptyMap(), selectedChartPeriod);

        SwingWorker<Map<?, Double>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<?, Double> doInBackground() throws Exception {
                return appContext.getAnalyticsService().getVendasAgrupadas(selectedChartPeriod);
            }
            @Override
            protected void done() {
                try {
                    Map<?, Double> chartData = get();
                    salesChart.setData(chartData, selectedChartPeriod);
                } catch (Exception e) {
                    salesChart.setData(null, selectedChartPeriod);
                }
            }
        };
        worker.execute();
    }

    private void setLoadingState(boolean isLoading) {
        String status = isLoading ? I18n.getString("general.loading") : I18n.getString("general.notAvailable");
        if (vendasHojeCard != null) vendasHojeCard.setValue(status);
        if (novosClientesCard != null) novosClientesCard.setValue(status);
        if (alertasCard != null) alertasCard.setValue(status);
        if (financialSummaryCard != null) financialSummaryCard.setRevenue(status);
    }

    private void updateUICards(DashboardData data) {
        if (vendasHojeCard != null) vendasHojeCard.setValue(currencyFormat.format(data.vendasHoje));
        if (novosClientesCard != null) novosClientesCard.setValue(String.valueOf(data.novosClientes));
        if (alertasCard != null) {
            alertasCard.setValue(String.valueOf(data.numAlertas));
            alertasCard.setValueColor(data.numAlertas > 0 ? Color.ORANGE.darker() : new Color(34, 139, 34));
        }
        if (financialSummaryCard != null) {
            financialSummaryCard.setRevenue(currencyFormat.format(data.receitaMes));
            financialSummaryCard.setAvgTicket(currencyFormat.format(data.ticketMedio));
            double comparisonPercentage = (data.receitaMesAnterior == 0) ? (data.receitaMes > 0 ? 100.0 : 0.0) : ((data.receitaMes - data.receitaMesAnterior) / data.receitaMesAnterior) * 100.0;
            financialSummaryCard.setComparison(comparisonPercentage);
        }
        if (performanceRankingsCard != null) {
            performanceRankingsCard.setTopProducts(data.topProducts);
            performanceRankingsCard.setTopClients(data.topClients);
        }
        if (salesChart != null) salesChart.setData(data.chartData, selectedChartPeriod);
        if (activityPanel != null) activityPanel.setActivities(data.activityData);
    }

    private void handleDataLoadError(Throwable error) {
        String errorMsg = I18n.getString("error.title");
        Color errorColor = Color.RED.darker();
        if (vendasHojeCard != null) {
            vendasHojeCard.setValue(errorMsg);
            vendasHojeCard.setValueColor(errorColor);
        }
        if (novosClientesCard != null) {
            novosClientesCard.setValue(errorMsg);
            novosClientesCard.setValueColor(errorColor);
        }
        if (alertasCard != null) {
            alertasCard.setValue(errorMsg);
            alertasCard.setValueColor(errorColor);
        }
        if (financialSummaryCard != null) financialSummaryCard.setRevenue(errorMsg);
        if (salesChart != null) salesChart.setData(null, selectedChartPeriod);
        if (activityPanel != null) activityPanel.setActivities(List.of());
    }

    private static class DashboardData {
        final double vendasHoje, receitaMes, receitaMesAnterior, ticketMedio;
        final long novosClientes;
        final int numAlertas;
        final Map<String, Integer> topProducts;
        final Map<String, Double> topClients;
        final Map<?, Double> chartData;
        final List<Object[]> activityData;

        DashboardData(double vh, long nc, int na, double rm, double rma, double tm, Map<String, Integer> tp, Map<String, Double> tc, Map<?, Double> cd, List<Object[]> ad) {
            this.vendasHoje = vh; this.novosClientes = nc; this.numAlertas = na; this.receitaMes = rm;
            this.receitaMesAnterior = rma; this.ticketMedio = tm; this.topProducts = tp;
            this.topClients = tc; this.chartData = cd; this.activityData = ad;
        }
    }
}