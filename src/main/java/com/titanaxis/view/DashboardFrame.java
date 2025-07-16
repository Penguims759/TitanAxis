package com.titanaxis.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.titanaxis.app.AppContext;
import com.titanaxis.app.MainApp;
import com.titanaxis.model.*;
import com.titanaxis.model.ai.Action;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIGuide;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.dialogs.CommandBarDialog;
import com.titanaxis.view.dialogs.DashboardCustomizationDialog;
import com.titanaxis.view.panels.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardFrame extends JFrame {
    private final AppContext appContext;
    private final AuthService authService;
    private final UIPersonalizationService personalizationService;
    private static final Logger logger = AppLogger.getLogger();

    private JTabbedPane mainTabbedPane;
    private JTabbedPane produtosEstoqueTabbedPane;
    private JTabbedPane cadastrosTabbedPane;
    private JTabbedPane adminTabbedPane;
    private JTabbedPane vendasTabbedPane;
    private JTabbedPane financeiroTabbedPane;

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
    private VendaPanel vendaPanel;
    private HistoricoVendasPanel historicoVendasPanel;
    private FinanceiroPanel financeiroPanel;

    public DashboardFrame(AppContext appContext) {
        super(I18n.getString("dashboard.title"));
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
        setupCommandBarShortcut();
        SwingUtilities.invokeLater(() -> setTheme(personalizationService.getPreference("theme", "dark")));
    }

    private void setupCommandBarShortcut() {
        JRootPane rootPane = getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK), "openCommandBar");
        actionMap.put("openCommandBar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CommandBarDialog commandBar = new CommandBarDialog(DashboardFrame.this, appContext);
                commandBar.setVisible(true);
            }
        });
    }

    private void setupMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu menuArquivo = new JMenu(I18n.getString("dashboard.menu.file"));
        final JMenu menuView = new JMenu(I18n.getString("dashboard.menu.view"));
        final JMenu menuTema = new JMenu(I18n.getString("dashboard.menu.changeTheme"));
        final JMenu menuIdioma = new JMenu(I18n.getString("dashboard.menu.language"));
        final ButtonGroup themeGroup = new ButtonGroup();
        final ButtonGroup languageGroup = new ButtonGroup();

        final JMenuItem customizeDashboardItem = new JMenuItem(I18n.getString("dashboard.menu.customize"));
        customizeDashboardItem.addActionListener(e -> openCustomizationDialog());
        menuView.add(customizeDashboardItem);

        final JRadioButtonMenuItem lightThemeItem = new JRadioButtonMenuItem(I18n.getString("dashboard.menu.lightTheme"));
        lightThemeItem.addActionListener(e -> setTheme("light"));
        final JRadioButtonMenuItem darkThemeItem = new JRadioButtonMenuItem(I18n.getString("dashboard.menu.darkTheme"));
        darkThemeItem.addActionListener(e -> setTheme("dark"));
        themeGroup.add(lightThemeItem);
        themeGroup.add(darkThemeItem);
        menuTema.add(lightThemeItem);
        menuTema.add(darkThemeItem);

        JRadioButtonMenuItem ptBrItem = new JRadioButtonMenuItem("Português (Brasil)");
        ptBrItem.addActionListener(e -> switchLanguage("pt", "BR"));
        JRadioButtonMenuItem enUsItem = new JRadioButtonMenuItem("English (US)");
        enUsItem.addActionListener(e -> switchLanguage("en", "US"));
        languageGroup.add(ptBrItem);
        languageGroup.add(enUsItem);
        menuIdioma.add(ptBrItem);
        menuIdioma.add(enUsItem);

        if ("pt".equals(I18n.getCurrentLocale().getLanguage())) {
            ptBrItem.setSelected(true);
        } else {
            enUsItem.setSelected(true);
        }

        if ("light".equals(personalizationService.getPreference("theme", "dark"))) {
            lightThemeItem.setSelected(true);
        } else {
            darkThemeItem.setSelected(true);
        }

        final JMenuItem logoutMenuItem = new JMenuItem(I18n.getString("dashboard.menu.logout"));
        logoutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        logoutMenuItem.addActionListener(e -> fazerLogout());

        final JMenuItem sairMenuItem = new JMenuItem(I18n.getString("dashboard.menu.exit"));
        sairMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        sairMenuItem.addActionListener(e -> confirmarSaida());

        menuArquivo.add(menuIdioma);
        menuArquivo.add(menuTema);
        menuArquivo.addSeparator();
        menuArquivo.add(logoutMenuItem);
        menuArquivo.add(sairMenuItem);

        menuBar.add(menuArquivo);
        menuBar.add(menuView);
        setJMenuBar(menuBar);
    }

    private void switchLanguage(String language, String country) {
        try {
            Locale newLocale = new Locale(language, country);
            personalizationService.savePreference("locale", newLocale.toLanguageTag());
            new UIPersonalizationService("default_user").savePreference("locale", newLocale.toLanguageTag());
            I18n.setLocale(newLocale);
            JOptionPane.showMessageDialog(this,
                    I18n.getString("dashboard.language.restartMessage"),
                    I18n.getString("dashboard.language.restartTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar = new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!currentJar.getName().endsWith(".jar")) {
                System.exit(0);
                return;
            }
            final ArrayList<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());
            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        } catch (java.net.URISyntaxException | IOException e) {
            logger.log(Level.SEVERE, "Falha ao tentar reiniciar a aplicação.", e);
            UIMessageUtil.showErrorMessage(this, "Não foi possível reiniciar a aplicação automaticamente. Por favor, reinicie manualmente.", "Erro de Reinicialização");
            System.exit(0);
        }
    }

    private void openCustomizationDialog() {
        DashboardCustomizationDialog dialog = new DashboardCustomizationDialog(this, personalizationService, this::rebuildAndShowHomePanel);
        dialog.setVisible(true);
    }

    private void rebuildAndShowHomePanel() {
        int homeTabIndex = mainTabbedPane.indexOfTab(I18n.getString("dashboard.tab.home"));
        if (homeTabIndex != -1) {
            homePanel = new HomePanel(appContext, this); // Recria o painel
            mainTabbedPane.setComponentAt(homeTabIndex, homePanel);
            mainTabbedPane.revalidate();
            mainTabbedPane.repaint();
        }
    }

    private void setupNestedTabs() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        homePanel = new HomePanel(appContext, this);
        mainTabbedPane.addTab(I18n.getString("dashboard.tab.home"), homePanel);
        mainTabbedPane.addChangeListener(createRefreshListener(homePanel));

        vendasTabbedPane = new JTabbedPane();
        vendaPanel = new VendaPanel(appContext);
        historicoVendasPanel = new HistoricoVendasPanel(appContext);
        vendasTabbedPane.addTab(I18n.getString("dashboard.tab.newSale"), vendaPanel);
        vendasTabbedPane.addTab(I18n.getString("dashboard.tab.history"), historicoVendasPanel);
        mainTabbedPane.addTab(I18n.getString("dashboard.tab.sales"), vendasTabbedPane);
        vendasTabbedPane.addChangeListener(createRefreshListener(null));

        if (authService.isGerente()) {
            financeiroPanel = new FinanceiroPanel(appContext);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.financial"), financeiroPanel);
            mainTabbedPane.addChangeListener(createRefreshListener(financeiroPanel));

            produtosEstoqueTabbedPane = new JTabbedPane();
            produtoPanel = new ProdutoPanel(appContext);
            categoriaPanel = new CategoriaPanel(appContext);
            alertaPanel = new AlertaPanel(appContext);
            movimentosPanel = new MovimentosPanel(this, appContext);
            produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.productsAndBatches"), produtoPanel);
            produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.categories"), categoriaPanel);
            produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.stockAlerts"), alertaPanel);
            produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.movementHistory"), movimentosPanel);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.productsAndStock"), produtosEstoqueTabbedPane);
            produtosEstoqueTabbedPane.addChangeListener(createRefreshListener(null));

            cadastrosTabbedPane = new JTabbedPane();
            clientePanel = new ClientePanel(appContext);
            fornecedorPanel = new FornecedorPanel(appContext);
            cadastrosTabbedPane.addTab(I18n.getString("dashboard.tab.clients"), clientePanel);
            cadastrosTabbedPane.addTab(I18n.getString("dashboard.tab.suppliers"), fornecedorPanel);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.registrations"), cadastrosTabbedPane);
            cadastrosTabbedPane.addChangeListener(createRefreshListener(null));

            relatorioPanel = new RelatorioPanel(appContext);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.reports"), relatorioPanel);
            mainTabbedPane.addChangeListener(createRefreshListener(relatorioPanel));
        }

        if (authService.isAdmin()) {
            adminTabbedPane = new JTabbedPane();
            usuarioPanel = new UsuarioPanel(appContext);
            auditoriaPanel = new AuditoriaPanel(appContext);
            adminTabbedPane.addTab(I18n.getString("dashboard.tab.userManagement"), usuarioPanel);
            adminTabbedPane.addTab(I18n.getString("dashboard.tab.auditLogs"), auditoriaPanel);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.administration"), adminTabbedPane);
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
                    navigateTo(I18n.getString("dashboard.tab.productsAndBatches"));
                    if (produtoPanel != null) {
                        produtoPanel.selectFirstProduct();
                        UIGuide.highlightComponent(produtoPanel.getAddLoteButton());
                    }
                    break;
                case GUIDE_NAVIGATE_TO_ADD_PRODUCT:
                    navigateTo(I18n.getString("dashboard.tab.productsAndBatches"));
                    if (produtoPanel != null) UIGuide.highlightComponent(produtoPanel.getNovoProdutoButton());
                    break;
                case DIRECT_CREATE_PRODUCT:
                    Produto novoProduto = new Produto((String) params.get("nome"), "", (Double) params.get("preco"), (Categoria) params.get("categoria"));
                    appContext.getProdutoService().salvarProduto(novoProduto, ator);
                    UIMessageUtil.showInfoMessage(this, I18n.getString("dashboard.action.productCreated", novoProduto.getNome()), I18n.getString("dashboard.action.actionComplete"));
                    if (produtoPanel != null) produtoPanel.refreshData();
                    break;
                case DIRECT_CREATE_CLIENT:
                    Cliente novoCliente = new Cliente((String) params.get("nome"), (String) params.get("contato"), "");
                    appContext.getClienteService().salvar(novoCliente, ator);
                    UIMessageUtil.showInfoMessage(this, I18n.getString("dashboard.action.clientCreated", novoCliente.getNome()), I18n.getString("dashboard.action.actionComplete"));
                    if (clientePanel != null) clientePanel.refreshData();
                    break;
                case DIRECT_CREATE_FORNECEDOR:
                    Fornecedor novoFornecedor = new Fornecedor();
                    novoFornecedor.setNome((String) params.get("nome"));
                    novoFornecedor.setContatoNome((String) params.get("contatoNome"));
                    novoFornecedor.setContatoTelefone((String) params.get("contatoTelefone"));
                    novoFornecedor.setContatoEmail((String) params.get("contatoEmail"));
                    appContext.getFornecedorService().salvar(novoFornecedor, ator);
                    UIMessageUtil.showInfoMessage(this, I18n.getString("dashboard.action.supplierCreated", novoFornecedor.getNome()), I18n.getString("dashboard.action.actionComplete"));
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
                    UIMessageUtil.showInfoMessage(this, I18n.getString("dashboard.action.stockAdjusted", lotNumber), I18n.getString("dashboard.action.actionComplete"));
                    if (produtoPanel != null) produtoPanel.refreshData();
                    if (movimentosPanel != null) movimentosPanel.refreshData();
                    break;

                case UI_CHANGE_THEME:
                    setTheme((String) params.get("theme"));
                    break;
                case START_SALE_FOR_CLIENT:
                    navigateTo(I18n.getString("dashboard.tab.newSale"));
                    vendaPanel.refreshData();
                    Cliente cliente = (Cliente) params.get("cliente");
                    if (cliente != null) vendaPanel.selecionarCliente(cliente);
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro detalhado ao executar a ação '" + action + "': ", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : I18n.getString("error.unknownCause");
            if (e.getCause() != null) errorMessage += I18n.getString("error.rootCause", e.getCause().getMessage());
            UIMessageUtil.showErrorMessage(this, I18n.getString("dashboard.action.errorExecuting", errorMessage), I18n.getString("dashboard.action.errorTitle"));
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

    private void setTheme(String themeName) {
        try {
            UIManager.setLookAndFeel("light".equals(themeName) ? new FlatLightLaf() : new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(this);
            personalizationService.savePreference("theme", themeName);
            logger.info("Tema alterado para: " + themeName);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Falha ao mudar o tema.", ex);
            UIMessageUtil.showErrorMessage(this, I18n.getString("dashboard.theme.error"), I18n.getString("dashboard.theme.errorTitle"));
        }
    }

    private void fazerLogout() {
        if (UIMessageUtil.showConfirmDialog(this, I18n.getString("dashboard.logout.confirm"), I18n.getString("dashboard.logout.title"))) {
            authService.logout();
            new LoginFrame(appContext).setVisible(true);
            this.dispose();
        }
    }

    private void confirmarSaida() {
        if (UIMessageUtil.showConfirmDialog(this, I18n.getString("dashboard.exit.confirm"), I18n.getString("dashboard.exit.title"))) {
            authService.logout();
            logger.info("Aplicação encerrada pelo usuário.");
            System.exit(0);
        }
    }

    public interface Refreshable {
        void refreshData();
    }
}