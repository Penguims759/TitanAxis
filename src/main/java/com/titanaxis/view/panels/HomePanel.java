package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.AlertaService;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.dashboard.ActivityCardPanel;
import com.titanaxis.view.panels.dashboard.KPICardPanel;
import com.titanaxis.view.panels.dashboard.QuickActionsPanel;
import com.titanaxis.view.panels.dashboard.SalesChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
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

    // CORREÇÃO: Todos os painéis são criados uma única vez e guardados como variáveis de instância.
    private final JPanel kpiPanel;
    private final SalesChartPanel salesChart;
    private final ActivityCardPanel activityPanel;
    private final QuickActionsPanel quickActionsPanel;
    private final JPanel centerContentPanel;

    private final KPICardPanel vendasHojeCard;
    private final KPICardPanel novosClientesCard;
    private final KPICardPanel alertasCard;

    public HomePanel(AppContext appContext) {
        this.appContext = appContext;
        this.personalizationService = new UIPersonalizationService(appContext.getAuthService().getUsuarioLogado().map(com.titanaxis.model.Usuario::getNomeUsuario).orElse("default"));

        setLayout(new BorderLayout());

        // 1. CRIAÇÃO DE TODOS OS COMPONENTES
        // Criação dos KPIs individuais
        vendasHojeCard = new KPICardPanel("VENDAS DO DIA", "Ver detalhes das vendas de hoje");
        novosClientesCard = new KPICardPanel("NOVOS CLIENTES (MÊS)", "Ir para a gestão de clientes");
        alertasCard = new KPICardPanel("ALERTAS DE STOCK", "Ir para o painel de alertas de stock");

        // Criação do painel que agrupa os KPIs
        kpiPanel = new JPanel(new GridLayout(1, 0, 15, 15));
        kpiPanel.add(vendasHojeCard);
        kpiPanel.add(novosClientesCard);
        kpiPanel.add(alertasCard);
        addNavigationListeners();

        // Criação dos outros painéis
        salesChart = new SalesChartPanel();
        activityPanel = new ActivityCardPanel();
        quickActionsPanel = new QuickActionsPanel();
        addQuickActionListeners();

        // 2. MONTAGEM DA ESTRUTURA FIXA
        // O painel `centerContentPanel` vai conter o gráfico e a atividade lado a lado
        centerContentPanel = new JPanel(new GridBagLayout());

        // O `mainContentPanel` organiza todos os outros na vertical
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 10, 0); // Espaçamento vertical
        gbc.weightx = 1.0;

        // Adiciona todos os componentes à estrutura (a sua visibilidade será controlada depois)
        gbc.gridy = 0;
        mainContentPanel.add(kpiPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0; // O painel central ocupa o máximo de espaço vertical
        mainContentPanel.add(centerContentPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        mainContentPanel.add(quickActionsPanel, gbc);

        add(new JScrollPane(mainContentPanel), BorderLayout.CENTER);

        // 3. ATUALIZAÇÃO INICIAL DA VISIBILIDADE E DADOS
        rebuildUI();
    }

    public void rebuildUI() {
        // CORREÇÃO: A lógica agora apenas define a visibilidade dos componentes.
        // Isto é muito mais estável e rápido para o Swing.
        kpiPanel.setVisible(Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.kpi_cards", "true")));
        salesChart.setVisible(Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.sales_chart", "true")));
        activityPanel.setVisible(Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.recent_activity", "true")));
        quickActionsPanel.setVisible(Boolean.parseBoolean(personalizationService.getPreference("dashboard.card.quick_actions", "true")));

        // Reconstrói o painel central para ajustar o espaçamento
        buildCenterContentPanel();

        // Valida o painel principal para garantir que o layout é recalculado
        revalidate();
        repaint();

        // Atualiza os dados dos componentes que estão visíveis
        refreshData();
    }

    private void buildCenterContentPanel() {
        centerContentPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        boolean chartVisible = salesChart.isVisible();
        boolean activityVisible = activityPanel.isVisible();

        if (chartVisible && activityVisible) {
            gbc.gridx = 0; gbc.weightx = 0.65;
            centerContentPanel.add(salesChart, gbc);
            gbc.gridx = 1; gbc.weightx = 0.35; gbc.insets = new Insets(0, 20, 0, 0);
            centerContentPanel.add(activityPanel, gbc);
        } else if (chartVisible) {
            gbc.gridx = 0; gbc.weightx = 1.0;
            centerContentPanel.add(salesChart, gbc);
        } else if (activityVisible) {
            gbc.gridx = 0; gbc.weightx = 1.0;
            centerContentPanel.add(activityPanel, gbc);
        }
    }

    private void addNavigationListeners() {
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
    }

    private void addQuickActionListeners() {
        DashboardFrame parentFrame = (DashboardFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            quickActionsPanel.newSaleButton.addActionListener(e -> parentFrame.navigateTo("Vendas"));
            quickActionsPanel.newProductButton.addActionListener(e -> parentFrame.navigateTo("Produtos & Estoque"));
            quickActionsPanel.newClientButton.addActionListener(e -> parentFrame.navigateTo("Clientes"));
        }
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

                    if (kpiPanel.isVisible()) {
                        final double vendasHoje = analytics.getVendasHoje();
                        final long novosClientes = analytics.getNovosClientesMes();
                        final int numAlertas = alertas.getProdutosComEstoqueBaixo().size();
                        SwingUtilities.invokeLater(() -> {
                            vendasHojeCard.setValue(currencyFormat.format(vendasHoje));
                            novosClientesCard.setValue(String.valueOf(novosClientes));
                            alertasCard.setValue(String.valueOf(numAlertas));
                            if(numAlertas > 0) alertasCard.setValueColor(Color.ORANGE.darker());
                            else alertasCard.setValueColor(new Color(34, 139, 34));
                        });
                    }
                    if (salesChart.isVisible()) {
                        final Map<LocalDate, Double> chartData = analytics.getVendasUltimos7Dias();
                        SwingUtilities.invokeLater(() -> salesChart.setData(chartData));
                    }
                    if (activityPanel.isVisible()) {
                        final List<Object[]> activityData = analytics.getRecentActivity(7);
                        SwingUtilities.invokeLater(() -> activityPanel.setActivities(activityData));
                    }
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