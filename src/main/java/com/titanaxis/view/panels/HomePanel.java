package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.AlertaService;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.dashboard.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomePanel extends JPanel {

    private final AppContext appContext;
    private final UIPersonalizationService personalizationService;
    private static final Logger logger = AppLogger.getLogger();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "PT"));

    private KPICardPanel vendasHojeCard;
    private KPICardPanel novosClientesCard;
    private KPICardPanel alertasCard;
    private FinancialSummaryCard financialSummaryCard;
    private PerformanceRankingsCard performanceRankingsCard;
    private SalesChartPanel salesChart;
    private ActivityCardPanel activityPanel;
    private QuickActionsPanel quickActionsPanel;

    private final JScrollPane scrollPane;

    public HomePanel(AppContext appContext) {
        this.appContext = appContext;
        this.personalizationService = new UIPersonalizationService(appContext.getAuthService().getUsuarioLogado().map(com.titanaxis.model.Usuario::getNomeUsuario).orElse("default"));

        setLayout(new BorderLayout());

        scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        rebuildUI();
    }

    public void rebuildUI() {
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        int gridY = 0;

        if (Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.kpi_cards", "true"))) {
            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.gridwidth = 3;
            mainContentPanel.add(createKpiPanel(), gbc);
        }

        if (Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.financial_summary", "true"))) {
            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.gridwidth = 3;
            financialSummaryCard = new FinancialSummaryCard();
            mainContentPanel.add(financialSummaryCard, gbc);
        }

        if (Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.performance_rankings", "true"))) {
            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.gridwidth = 3;
            performanceRankingsCard = new PerformanceRankingsCard();
            mainContentPanel.add(performanceRankingsCard, gbc);
        }

        if (Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.sales_chart", "true")) ||
                Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.recent_activity", "true"))) {

            JPanel centerPanel = new JPanel(new GridBagLayout());
            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.gridwidth = 3;
            gbc.weighty = 1.0;
            mainContentPanel.add(centerPanel, gbc);

            GridBagConstraints gbcCenter = new GridBagConstraints();
            gbcCenter.fill = GridBagConstraints.BOTH;
            gbcCenter.weighty = 1.0;

            if (Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.sales_chart", "true"))) {
                gbcCenter.gridx = 0;
                gbcCenter.weightx = 0.65;
                salesChart = new SalesChartPanel();
                centerPanel.add(salesChart, gbcCenter);
            }
            if (Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.recent_activity", "true"))) {
                gbcCenter.gridx = 1;
                gbcCenter.weightx = 0.35;
                gbcCenter.insets = new Insets(0, (salesChart != null && salesChart.isVisible() ? 20 : 0), 0, 0);
                activityPanel = new ActivityCardPanel();
                centerPanel.add(activityPanel, gbcCenter);
            }
        }

        if (Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.quick_actions", "true"))) {
            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.gridwidth = 3;
            gbc.weighty = 0;
            mainContentPanel.add(createQuickActionsPanel(), gbc);
        }

        scrollPane.setViewportView(mainContentPanel);
        scrollPane.revalidate();
        scrollPane.repaint();

        refreshData();
    }

    private JPanel createKpiPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 15, 15));
        vendasHojeCard = new KPICardPanel("VENDAS DO DIA", "Ver detalhes das vendas de hoje");
        novosClientesCard = new KPICardPanel("NOVOS CLIENTES (MÊS)", "Ir para a gestão de clientes");
        alertasCard = new KPICardPanel("ALERTAS DE STOCK", "Ir para o painel de alertas de stock");
        panel.add(vendasHojeCard);
        panel.add(novosClientesCard);
        panel.add(alertasCard);

        alertasCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DashboardFrame parentFrame = (DashboardFrame) SwingUtilities.getWindowAncestor(HomePanel.this);
                if (parentFrame != null) {
                    parentFrame.navigateTo("Produtos & Estoque");
                    parentFrame.navigateToProductSubTab("Alertas de Estoque");
                }
            }
        });
        novosClientesCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DashboardFrame parentFrame = (DashboardFrame) SwingUtilities.getWindowAncestor(HomePanel.this);
                if (parentFrame != null) parentFrame.navigateTo("Clientes");
            }
        });
        return panel;
    }

    private QuickActionsPanel createQuickActionsPanel() {
        QuickActionsPanel panel = new QuickActionsPanel();
        DashboardFrame parentFrame = (DashboardFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            panel.newSaleButton.addActionListener(e -> parentFrame.navigateTo("Vendas"));
            panel.newProductButton.addActionListener(e -> parentFrame.navigateTo("Produtos & Estoque"));
            panel.newClientButton.addActionListener(e -> parentFrame.navigateTo("Clientes"));
        }
        return panel;
    }

    public void refreshData() {
        logger.info("A atualizar dados do HomePanel...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    AnalyticsService analytics = appContext.getAnalyticsService();
                    AlertaService alertas = appContext.getAlertaService();
                    LocalDate today = LocalDate.now();
                    YearMonth currentMonth = YearMonth.from(today);
                    YearMonth previousMonth = currentMonth.minusMonths(1);

                    final double vendasHoje = analytics.getVendas(today, today);
                    final long novosClientes = analytics.getNovosClientes(currentMonth.atDay(1), currentMonth.atEndOfMonth());
                    final int numAlertas = alertas.getProdutosComEstoqueBaixo().size();

                    final double receitaMes = analytics.getVendas(currentMonth.atDay(1), currentMonth.atEndOfMonth());
                    final double receitaMesAnterior = analytics.getVendas(previousMonth.atDay(1), previousMonth.atEndOfMonth());
                    final double ticketMedio = analytics.getTicketMedio(currentMonth.atDay(1), currentMonth.atEndOfMonth());
                    final double comparisonPercentage = (receitaMesAnterior == 0) ? Double.NaN : ((receitaMes - receitaMesAnterior) / receitaMesAnterior) * 100.0;

                    final Map<String, Integer> topProducts = analytics.getTopProdutos(currentMonth.atDay(1), currentMonth.atEndOfMonth(), 3);
                    final Map<String, Double> topClients = analytics.getTopClientes(currentMonth.atDay(1), currentMonth.atEndOfMonth(), 3);

                    final Map<LocalDate, Double> chartData = analytics.getVendasUltimos7Dias();
                    final List<Object[]> activityData = analytics.getRecentActivity(7);

                    SwingUtilities.invokeLater(() -> {
                        if (vendasHojeCard != null) vendasHojeCard.setValue(currencyFormat.format(vendasHoje));
                        if (novosClientesCard != null) novosClientesCard.setValue(String.valueOf(novosClientes));
                        if (alertasCard != null) {
                            alertasCard.setValue(String.valueOf(numAlertas));
                            if(numAlertas > 0) alertasCard.setValueColor(Color.ORANGE.darker()); else alertasCard.setValueColor(new Color(34, 139, 34));
                        }
                        if (financialSummaryCard != null) {
                            financialSummaryCard.setRevenue(currencyFormat.format(receitaMes));
                            financialSummaryCard.setAvgTicket(currencyFormat.format(ticketMedio));
                            financialSummaryCard.setComparison(comparisonPercentage);
                        }
                        if (performanceRankingsCard != null) {
                            performanceRankingsCard.setTopProducts(topProducts);
                            performanceRankingsCard.setTopClients(topClients);
                        }
                        if (salesChart != null) salesChart.setData(chartData);
                        if (activityPanel != null) activityPanel.setActivities(activityData);
                    });

                } catch (PersistenciaException e) {
                    logger.log(Level.SEVERE, "Erro ao carregar dados para o dashboard.", e);
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
            }
        };
        worker.execute();
    }
}