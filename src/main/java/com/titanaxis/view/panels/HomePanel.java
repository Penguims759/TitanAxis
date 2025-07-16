package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.DashboardData;
import com.titanaxis.model.Usuario;
import com.titanaxis.presenter.DashboardDataWorker;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.dashboard.*;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.Locale;

public class HomePanel extends JPanel implements DashboardFrame.Refreshable {

    private final AppContext appContext;
    private final DashboardFrame parentFrame;

    // --- Componentes do Dashboard ---
    private FinancialSummaryCard comparisonPanel;
    private MetasPanel goalsPanel;
    private JPanel kpiPanel; // Declarado aqui para ser acessível em toda a classe
    private AIAssistantPanel aiAssistantPanel;
    private JTextArea assistantReportArea;
    private SalesChartPanel salesChartPanel;
    private JPanel chartDisplayPanel;
    private PerformanceRankingsCard topProductsCard;
    private PerformanceRankingsCard topClientsCard;

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

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(0, 0, 15, 0);
        add(createHeaderPanel(), gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.weightx = 0.25; gbc.insets = new Insets(0, 0, 15, 15);
        add(createCommandColumn(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.45;
        add(createMainChartColumn(), gbc);

        gbc.gridx = 2; gbc.weightx = 0.30; gbc.insets = new Insets(0, 0, 15, 0);
        add(createObjectivesColumn(), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.weighty = 0.5;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(createInteractiveAnalysisPanel(), gbc);

        revalidate(); repaint();
    }

    private JComponent createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        String username = appContext.getAuthService().getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("");
        JLabel greetingLabel = new JLabel(getGreetingByTimeOfDay() + " " + username + "!");
        greetingLabel.setFont(new Font("Arial", Font.BOLD, 22));
        header.add(greetingLabel, BorderLayout.CENTER);
        header.add(new QuickActionsPanel(parentFrame::navigateTo), BorderLayout.EAST);
        return header;
    }

    private JComponent createCommandColumn() {
        JPanel column = new JPanel(new BorderLayout(0, 15));
        aiAssistantPanel = new AIAssistantPanel(appContext);

        JPanel assistantReportPanel = new JPanel(new BorderLayout());
        assistantReportPanel.setBorder(BorderFactory.createTitledBorder("Relatório do Assistente"));
        assistantReportArea = new JTextArea("A carregar insights...");
        assistantReportArea.setEditable(false);
        assistantReportArea.setLineWrap(true);
        assistantReportArea.setWrapStyleWord(true);
        assistantReportPanel.add(new JScrollPane(assistantReportArea));

        column.add(aiAssistantPanel, BorderLayout.CENTER);
        column.add(assistantReportPanel, BorderLayout.SOUTH);
        assistantReportPanel.setPreferredSize(new Dimension(0, (int)(aiAssistantPanel.getPreferredSize().getHeight() * 0.4)));
        return column;
    }

    private JComponent createMainChartColumn() {
        salesChartPanel = new SalesChartPanel(this::onChartPeriodChange);
        salesChartPanel.setBorder(BorderFactory.createTitledBorder("Evolução de Vendas"));
        return salesChartPanel;
    }

    private JComponent createObjectivesColumn() {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        comparisonPanel = new FinancialSummaryCard();
        comparisonPanel.setBorder(BorderFactory.createTitledBorder("Comparações Mês a Mês"));
        goalsPanel = new MetasPanel(appContext);
        goalsPanel.setBorder(BorderFactory.createTitledBorder("Metas de Venda"));

        // --- CORREÇÃO AQUI: Inicializa o kpiPanel como membro da classe ---
        kpiPanel = new JPanel();
        kpiPanel.setBorder(BorderFactory.createTitledBorder("Resumo do Dia"));
        kpiPanel.setLayout(new GridLayout(3,1,0,5));

        column.add(comparisonPanel);
        column.add(Box.createVerticalStrut(15));
        column.add(goalsPanel);
        column.add(Box.createVerticalStrut(15));
        column.add(kpiPanel);
        column.add(Box.createVerticalGlue());

        return column;
    }

    private JComponent createInteractiveAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Análise Detalhada Interativa"));

        DefaultListModel<String> analysisListModel = new DefaultListModel<>();
        analysisListModel.addElement("Top 5 Produtos");
        analysisListModel.addElement("Top 5 Clientes");
        JList<String> analysisList = new JList<>(analysisListModel);
        analysisList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        analysisList.setSelectedIndex(0);
        analysisList.setPreferredSize(new Dimension(180, 0));

        chartDisplayPanel = new JPanel(new CardLayout());
        topProductsCard = new PerformanceRankingsCard();
        topClientsCard = new PerformanceRankingsCard();
        chartDisplayPanel.add(topProductsCard, "Top 5 Produtos");
        chartDisplayPanel.add(topClientsCard, "Top 5 Clientes");

        analysisList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = analysisList.getSelectedValue();
                CardLayout cl = (CardLayout)(chartDisplayPanel.getLayout());
                cl.show(chartDisplayPanel, selected);
            }
        });

        panel.add(new JScrollPane(analysisList), BorderLayout.WEST);
        panel.add(chartDisplayPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 240));

        return panel;
    }

    private void onChartPeriodChange(String newPeriod) {
        this.selectedChartPeriod = newPeriod;
        refreshData();
    }

    @Override
    public void refreshData() {
        setLoadingState(true);
        new DashboardDataWorker(this, appContext, selectedChartPeriod).execute();
    }

    public void setLoadingState(boolean isLoading) {
        setCursor(isLoading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        String status = isLoading ? I18n.getString("general.loading") : "---";

        if (kpiPanel != null) {
            kpiPanel.removeAll();
            kpiPanel.add(new JLabel(status));
            kpiPanel.revalidate();
            kpiPanel.repaint();
        }
        if (comparisonPanel != null) {
            comparisonPanel.setRevenue(status);
            comparisonPanel.setAvgTicket(status);
            comparisonPanel.setComparison(Double.NaN);
        }
        if (salesChartPanel != null) salesChartPanel.setData(null, selectedChartPeriod);
        if (assistantReportArea != null) assistantReportArea.setText(I18n.getString("general.loading"));
        if (topProductsCard != null) topProductsCard.setTopProducts(null);
        if (topClientsCard != null) topClientsCard.setTopClients(null);
    }

    public void showErrorState(String errorMessage) {
        UIMessageUtil.showErrorMessage(this, "Erro ao carregar dados do dashboard: " + errorMessage, "Erro de Carregamento");
    }

    public void updateUI(DashboardData data) {
        kpiPanel.removeAll();
        kpiPanel.add(new JLabel("Vendas Hoje: " + currencyFormat.format(data.vendasHoje)));
        kpiPanel.add(new JLabel("Novos Clientes: " + data.novosClientes));
        kpiPanel.add(new JLabel("Alertas Ativos: " + data.numAlertas));

        if(salesChartPanel != null) salesChartPanel.setData(data.salesChartData, selectedChartPeriod);

        if (comparisonPanel != null) {
            comparisonPanel.setRevenue(currencyFormat.format(data.receitaMes));
            comparisonPanel.setAvgTicket(currencyFormat.format(data.ticketMedio));
            double comparison = (data.receitaMesAnterior == 0 && data.receitaMes > 0) ? 100.0 :
                    (data.receitaMesAnterior == 0) ? Double.NaN :
                            ((data.receitaMes - data.receitaMesAnterior) / data.receitaMesAnterior) * 100;
            comparisonPanel.setComparison(comparison);
        }

        if (assistantReportArea != null) {
            if (data.assistantInsights.isEmpty()) {
                assistantReportArea.setText("Nenhum insight ou alerta importante no momento.");
            } else {
                assistantReportArea.setText(String.join("\n", data.assistantInsights));
            }
        }

        if(topProductsCard != null) topProductsCard.setTopProducts(data.topProducts);
        if(topClientsCard != null) topClientsCard.setTopClients(data.topClients);
        if(goalsPanel != null) goalsPanel.refreshData();

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