package com.titanaxis.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.titanaxis.app.AppContext;
import com.titanaxis.model.*;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.auditoria.Habito;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.UIGuide;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.dialogs.DashboardCustomizationDialog;
import com.titanaxis.view.panels.*;
import com.titanaxis.view.panels.dashboard.HomePanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardFrame extends JFrame {
    private final AppContext appContext;
    private final AuthService authService;
    private final UIPersonalizationService personalizationService;
    private static final Logger logger = AppLogger.getLogger();

    // Painéis de Abas
    private JTabbedPane mainTabbedPane;
    private JTabbedPane produtosEstoqueTabbedPane;
    private JTabbedPane cadastrosTabbedPane;
    private JTabbedPane adminTabbedPane;
    private JTabbedPane vendasTabbedPane;
    private JTabbedPane financeiroTabbedPane;

    // Painéis Individuais
    private HomePanel homePanel;
    private ProdutoPanel produtoPanel;
    private ClientePanel clientePanel;
    private CategoriaPanel categoriaPanel;
    private FornecedorPanel fornecedorPanel;
    private AlertaPanel alertaPanel;
    private MovimentosPanel movimentosPanel;
    private RelatorioPanel relatorioPanel;
    private UsuarioPanel usuarioPanel;
    private AuditoriaPanel auditoriaPanel;
    private AIAssistantPanel aiAssistantPanel;
    private VendaPanel vendaPanel;
    private HistoricoVendasPanel historicoVendasPanel;
    private FinanceiroPanel financeiroPanel;
    private int aiAssistantTabIndex = -1;

    public DashboardFrame(AppContext appContext) {
        super("Dashboard - TitanAxis");
        this.appContext = appContext;
        this.authService = appContext.getAuthService();
        this.personalizationService = new UIPersonalizationService(
                authService.getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("default")
        );

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSaida();
            }
        });

        setupMenuBar();
        setupNestedTabs();

        mainTabbedPane.addChangeListener(e -> {
            if (aiAssistantTabIndex != -1 && mainTabbedPane.getSelectedIndex() == aiAssistantTabIndex) {
                mainTabbedPane.setForegroundAt(aiAssistantTabIndex, UIManager.getColor("Button.foreground"));
            }
        });

        SwingUtilities.invokeLater(() -> {
            setTheme(personalizationService.getPreference("theme", "dark"));
            showProactiveInsights();
        });
    }

    private void setupMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu menuArquivo = new JMenu("Arquivo");
        final JMenu menuView = new JMenu("Visualização");
        final JMenu menuTema = new JMenu("Alterar Tema");
        final ButtonGroup themeGroup = new ButtonGroup();

        final JMenuItem customizeDashboardItem = new JMenuItem("Personalizar Dashboard");
        customizeDashboardItem.addActionListener(e -> openCustomizationDialog());
        menuView.add(customizeDashboardItem);

        final JRadioButtonMenuItem lightThemeItem = new JRadioButtonMenuItem("Tema Claro");
        lightThemeItem.addActionListener(e -> setTheme("light"));

        final JRadioButtonMenuItem darkThemeItem = new JRadioButtonMenuItem("Tema Escuro");
        darkThemeItem.setSelected(true);
        darkThemeItem.addActionListener(e -> setTheme("dark"));

        themeGroup.add(lightThemeItem);
        themeGroup.add(darkThemeItem);
        menuTema.add(lightThemeItem);
        menuTema.add(darkThemeItem);

        final JMenuItem logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        logoutMenuItem.addActionListener(e -> fazerLogout());

        final JMenuItem sairMenuItem = new JMenuItem("Sair");
        sairMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        sairMenuItem.addActionListener(e -> confirmarSaida());

        menuArquivo.add(menuTema);
        menuArquivo.addSeparator();
        menuArquivo.add(logoutMenuItem);
        menuArquivo.add(sairMenuItem);

        menuBar.add(menuArquivo);
        menuBar.add(menuView);
        setJMenuBar(menuBar);
    }

    private void openCustomizationDialog() {
        DashboardCustomizationDialog dialog = new DashboardCustomizationDialog(this, personalizationService, this::rebuildAndShowHomePanel);
        dialog.setVisible(true);
    }

    private void rebuildAndShowHomePanel() {
        int homeTabIndex = mainTabbedPane.indexOfTab("Início");
        if (homeTabIndex != -1) {
            homePanel = new HomePanel(appContext);
            mainTabbedPane.setComponentAt(homeTabIndex, homePanel);
            mainTabbedPane.revalidate();
            mainTabbedPane.repaint();
            logger.info("Painel de início reconstruído com sucesso.");
        }
    }

    private void setupNestedTabs() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        homePanel = new HomePanel(appContext);
        mainTabbedPane.addTab("Início", homePanel);
        mainTabbedPane.addChangeListener(createRefreshListener(homePanel));

        if (authService.isGerente()) {
            aiAssistantPanel = new AIAssistantPanel(appContext);
            mainTabbedPane.addTab("Assistente", aiAssistantPanel);
            aiAssistantTabIndex = mainTabbedPane.getTabCount() - 1;
            mainTabbedPane.addChangeListener(createRefreshListener(aiAssistantPanel));
        }

        vendasTabbedPane = new JTabbedPane();
        vendaPanel = new VendaPanel(appContext);
        historicoVendasPanel = new HistoricoVendasPanel(appContext);
        vendasTabbedPane.addTab("Nova Venda", vendaPanel);
        vendasTabbedPane.addTab("Histórico", historicoVendasPanel);
        mainTabbedPane.addTab("Vendas", vendasTabbedPane);
        vendasTabbedPane.addChangeListener(createRefreshListener(null));

        if (authService.isGerente()) {
            financeiroPanel = new FinanceiroPanel(appContext);
            mainTabbedPane.addTab("Financeiro", financeiroPanel);
            mainTabbedPane.addChangeListener(createRefreshListener(financeiroPanel));

            produtosEstoqueTabbedPane = new JTabbedPane();
            produtoPanel = new ProdutoPanel(appContext);
            categoriaPanel = new CategoriaPanel(appContext);
            alertaPanel = new AlertaPanel(appContext);
            movimentosPanel = new MovimentosPanel(this, appContext);
            produtosEstoqueTabbedPane.addTab("Gestão de Produtos e Lotes", produtoPanel);
            produtosEstoqueTabbedPane.addTab("Categorias", categoriaPanel);
            produtosEstoqueTabbedPane.addTab("Alertas de Estoque", alertaPanel);
            produtosEstoqueTabbedPane.addTab("Histórico de Movimentos", movimentosPanel);
            mainTabbedPane.addTab("Produtos & Estoque", produtosEstoqueTabbedPane);
            produtosEstoqueTabbedPane.addChangeListener(createRefreshListener(null));

            cadastrosTabbedPane = new JTabbedPane();
            clientePanel = new ClientePanel(appContext);
            fornecedorPanel = new FornecedorPanel(appContext);
            cadastrosTabbedPane.addTab("Clientes", clientePanel);
            cadastrosTabbedPane.addTab("Fornecedores", fornecedorPanel);
            mainTabbedPane.addTab("Cadastros", cadastrosTabbedPane);
            cadastrosTabbedPane.addChangeListener(createRefreshListener(null));

            relatorioPanel = new RelatorioPanel(appContext);
            mainTabbedPane.addTab("Relatórios", relatorioPanel);
            mainTabbedPane.addChangeListener(createRefreshListener(relatorioPanel));
        }

        if (authService.isAdmin()) {
            adminTabbedPane = new JTabbedPane();
            usuarioPanel = new UsuarioPanel(appContext);
            auditoriaPanel = new AuditoriaPanel(appContext);
            adminTabbedPane.addTab("Gestão de Usuários", usuarioPanel);
            adminTabbedPane.addTab("Logs de Auditoria", auditoriaPanel);
            mainTabbedPane.addTab("Administração", adminTabbedPane);
            adminTabbedPane.addChangeListener(createRefreshListener(null));
        }

        add(mainTabbedPane);
    }

    public void navigateTo(String destination) {
        for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            if (mainTabbedPane.getTitleAt(i).equalsIgnoreCase(destination)) {
                mainTabbedPane.setSelectedIndex(i);
                return;
            }
        }

        if (navigateToSubTab(vendasTabbedPane, destination)) return;
        if (navigateToSubTab(produtosEstoqueTabbedPane, destination)) return;
        if (navigateToSubTab(cadastrosTabbedPane, destination)) return;
        if (navigateToSubTab(adminTabbedPane, destination)) return;
        if (navigateToSubTab(financeiroTabbedPane, destination)) return;
    }

    private boolean navigateToSubTab(JTabbedPane parentTabPane, String subTabName) {
        if (parentTabPane != null) {
            for (int i = 0; i < parentTabPane.getTabCount(); i++) {
                if (parentTabPane.getTitleAt(i).equalsIgnoreCase(subTabName)) {
                    mainTabbedPane.setSelectedComponent(parentTabPane);
                    parentTabPane.setSelectedIndex(i);
                    return true;
                }
            }
        }
        return false;
    }

    public void executeAction(Action action, Map<String, Object> params) {
        try {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            switch (action) {
                case UI_NAVIGATE:
                    navigateTo((String) params.get("destination"));
                    break;
                case GUIDE_NAVIGATE_TO_ADD_LOTE:
                    navigateTo("Gestão de Produtos e Lotes");
                    if (produtoPanel != null) {
                        produtoPanel.selectFirstProduct();
                        UIGuide.highlightComponent(produtoPanel.getAddLoteButton());
                    }
                    break;
                case GUIDE_NAVIGATE_TO_ADD_PRODUCT:
                    navigateTo("Gestão de Produtos e Lotes");
                    if (produtoPanel != null) UIGuide.highlightComponent(produtoPanel.getNovoProdutoButton());
                    break;
                case DIRECT_CREATE_PRODUCT:
                    Produto novoProduto = new Produto((String) params.get("nome"), "", (Double) params.get("preco"), (Categoria) params.get("categoria"));
                    appContext.getProdutoService().salvarProduto(novoProduto, ator);
                    UIMessageUtil.showInfoMessage(this, "Produto '" + novoProduto.getNome() + "' criado com sucesso!", "Ação Concluída");
                    if (produtoPanel != null) produtoPanel.refreshData();
                    break;
                case DIRECT_CREATE_CLIENT:
                    Cliente novoCliente = new Cliente((String) params.get("nome"), (String) params.get("contato"), "");
                    appContext.getClienteService().salvar(novoCliente, ator);
                    UIMessageUtil.showInfoMessage(this, "Cliente '" + novoCliente.getNome() + "' foi criado com sucesso!", "Ação Concluída");
                    if (clientePanel != null) clientePanel.refreshData();
                    break;
                case DIRECT_CREATE_FORNECEDOR:
                    Fornecedor novoFornecedor = new Fornecedor();
                    novoFornecedor.setNome((String) params.get("nome"));
                    novoFornecedor.setContatoNome((String) params.get("contatoNome"));
                    novoFornecedor.setContatoTelefone((String) params.get("contatoTelefone"));
                    novoFornecedor.setContatoEmail((String) params.get("contatoEmail"));
                    appContext.getFornecedorService().salvar(novoFornecedor, ator);
                    UIMessageUtil.showInfoMessage(this, "Fornecedor '" + novoFornecedor.getNome() + "' criado com sucesso!", "Ação Concluída");
                    if (fornecedorPanel != null) fornecedorPanel.refreshData();
                    break;
                case DIRECT_ADJUST_STOCK:
                    String prodName = (String) params.get("productName");
                    String lotNumber = (String) params.get("lotNumber");
                    Object quantityObj = params.get("quantity");
                    if (quantityObj instanceof String) {
                        int newQuantity = Integer.parseInt((String) quantityObj);
                        appContext.getProdutoService().ajustarEstoqueLote(prodName, lotNumber, newQuantity, ator);
                    } else if (quantityObj instanceof Number) {
                        int newQuantity = ((Number) quantityObj).intValue();
                        appContext.getProdutoService().ajustarEstoqueLote(prodName, lotNumber, newQuantity, ator);
                    }
                    UIMessageUtil.showInfoMessage(this, "Estoque do lote " + lotNumber + " ajustado com sucesso!", "Ação Concluída");
                    if (produtoPanel != null) produtoPanel.refreshData();
                    if (movimentosPanel != null) movimentosPanel.refreshData();
                    break;

                case UI_CHANGE_THEME:
                    setTheme((String) params.get("theme"));
                    break;
                case START_SALE_FOR_CLIENT:
                    navigateTo("Nova Venda");
                    vendaPanel.refreshData();
                    Cliente cliente = (Cliente) params.get("cliente");
                    if (cliente != null) vendaPanel.selecionarCliente(cliente);
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro detalhado ao executar a ação '" + action + "': ", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Causa desconhecida.";
            if (e.getCause() != null) errorMessage += "\nCausa Raiz: " + e.getCause().getMessage();
            UIMessageUtil.showErrorMessage(this, "Erro ao executar a ação: " + errorMessage, "Erro na Ação do Assistente");
        }
    }

    private ChangeListener createRefreshListener(Component panel) {
        return e -> {
            Object source = e.getSource();
            Component selected = null;
            if (source instanceof JTabbedPane) {
                selected = ((JTabbedPane) source).getSelectedComponent();
            } else if (panel != null) {
                selected = panel;
            }

            if (selected instanceof Refreshable) {
                ((Refreshable) selected).refreshData();
            }
        };
    }

    private void showProactiveInsights() {
        if (aiAssistantPanel == null) return;

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                List<String> combinedInsights = new ArrayList<>(appContext.getAnalyticsService().getSystemInsightsSummary());
                int userId = authService.getUsuarioLogadoId();
                if (userId != 0) {
                    List<Habito> habits = appContext.getUserHabitService().findHabitsForToday(userId);
                    habits.stream()
                            .map(Habito::getSugestao)
                            .filter(Objects::nonNull)
                            .forEach(combinedInsights::add);
                }
                return combinedInsights;
            }

            @Override
            protected void done() {
                try {
                    List<String> insights = get();
                    String userName = authService.getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("");
                    String greeting = getGreetingByTimeOfDay() + " " + userName + "!";

                    if (insights.isEmpty()) {
                        aiAssistantPanel.appendMessage(greeting + " Como posso ajudar hoje?", false);
                    } else {
                        mainTabbedPane.setForegroundAt(aiAssistantTabIndex, Color.CYAN);
                        String insightsMessage = greeting + " Notei alguns pontos que podem precisar da sua atenção:\n" + String.join("\n", insights);
                        aiAssistantPanel.appendMessage(insightsMessage, false);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Falha ao obter insights proativos.", e);
                    aiAssistantPanel.appendMessage(getGreetingByTimeOfDay() + " Tive um problema ao carregar os insights, mas estou pronto para ajudar.", false);
                }
            }
        }.execute();
    }

    private String getGreetingByTimeOfDay() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.NOON)) {
            return "Bom dia,";
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            return "Boa tarde,";
        } else {
            return "Boa noite,";
        }
    }

    private void setTheme(String themeName) {
        try {
            UIManager.setLookAndFeel("light".equals(themeName) ? new FlatLightLaf() : new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(this);
            personalizationService.savePreference("theme", themeName);
            logger.info("Tema alterado para: " + themeName);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Falha ao mudar o tema.", ex);
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro ao alterar o tema.", "Erro de Tema");
        }
    }

    private void fazerLogout() {
        if (UIMessageUtil.showConfirmDialog(this, "Tem certeza que deseja sair da sua conta?", "Logout")) {
            authService.logout();
            new LoginFrame(appContext).setVisible(true);
            this.dispose();
        }
    }

    private void confirmarSaida() {
        if (UIMessageUtil.showConfirmDialog(this, "Tem certeza que deseja fechar a aplicação?", "Sair do Sistema")) {
            authService.logout();
            logger.info("Aplicação encerrada pelo usuário.");
            System.exit(0);
        }
    }

    public interface Refreshable {
        void refreshData();
    }
}