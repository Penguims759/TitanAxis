package com.titanaxis.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.titanaxis.app.AppContext;
import com.titanaxis.handler.ActionHandler;
import com.titanaxis.model.ai.Action;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.builder.DashboardMenuBarFactory;
import com.titanaxis.view.builder.DashboardViewBuilder;
import com.titanaxis.view.dialogs.CommandBarDialog;
import com.titanaxis.view.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import org.slf4j.Logger;

public class DashboardFrame extends JFrame {
    private final AppContext appContext;
    private final AuthService authService;
    private final UIPersonalizationService personalizationService;
    private final ActionHandler actionHandler;
    private static final Logger logger = AppLogger.getLogger();

    // Referências aos painéis
    private JTabbedPane mainTabbedPane;
    private JTabbedPane produtosEstoqueTabbedPane, cadastrosTabbedPane, adminTabbedPane, vendasTabbedPane, financeiroTabbedPane;
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
    private JPanel overlayPanel;

    public DashboardFrame(AppContext appContext) {
        super(I18n.getString("dashboard.title"));
        this.appContext = appContext;
        this.authService = appContext.getAuthService();
        this.personalizationService = new UIPersonalizationService(authService.getUsuarioLogado().map(u -> u.getNomeUsuario()).orElse("default"));
        this.actionHandler = new ActionHandler(this, appContext);

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

        setupOverlay();
        setupMenuBar();
        setupTabs();
        setupCommandBarShortcut();
        SwingUtilities.invokeLater(() -> setTheme(personalizationService.getPreference("theme", "dark")));
    }

    // Getters para os painéis
    public ProdutoPanel getProdutoPanel() { return produtoPanel; }
    public VendaPanel getVendaPanel() { return vendaPanel; }

    // Setters para o ViewBuilder
    public void setMainTabbedPane(JTabbedPane mainTabbedPane) { this.mainTabbedPane = mainTabbedPane; }
    public void setProdutosEstoqueTabbedPane(JTabbedPane produtosEstoqueTabbedPane) { this.produtosEstoqueTabbedPane = produtosEstoqueTabbedPane; }
    public void setCadastrosTabbedPane(JTabbedPane cadastrosTabbedPane) { this.cadastrosTabbedPane = cadastrosTabbedPane; }
    public void setAdminTabbedPane(JTabbedPane adminTabbedPane) { this.adminTabbedPane = adminTabbedPane; }
    public void setVendasTabbedPane(JTabbedPane vendasTabbedPane) { this.vendasTabbedPane = vendasTabbedPane; }
    public void setFinanceiroTabbedPane(JTabbedPane financeiroTabbedPane) { this.financeiroTabbedPane = financeiroTabbedPane; }
    public void setHomePanel(HomePanel homePanel) { this.homePanel = homePanel; }
    public void setProdutoPanel(ProdutoPanel produtoPanel) { this.produtoPanel = produtoPanel; }
    public void setClientePanel(ClientePanel clientePanel) { this.clientePanel = clientePanel; }
    public void setCategoriaPanel(CategoriaPanel categoriaPanel) { this.categoriaPanel = categoriaPanel; }
    public void setFornecedorPanel(FornecedorPanel fornecedorPanel) { this.fornecedorPanel = fornecedorPanel; }
    public void setAlertaPanel(AlertaPanel alertaPanel) { this.alertaPanel = alertaPanel; }
    public void setMovimentosPanel(MovimentosPanel movimentosPanel) { this.movimentosPanel = movimentosPanel; }
    public void setRelatorioPanel(RelatorioPanel relatorioPanel) { this.relatorioPanel = relatorioPanel; }
    public void setUsuarioPanel(UsuarioPanel usuarioPanel) { this.usuarioPanel = usuarioPanel; }
    public void setAuditoriaPanel(AuditoriaPanel auditoriaPanel) { this.auditoriaPanel = auditoriaPanel; }
    public void setVendaPanel(VendaPanel vendaPanel) { this.vendaPanel = vendaPanel; }
    public void setHistoricoVendasPanel(HistoricoVendasPanel historicoVendasPanel) { this.historicoVendasPanel = historicoVendasPanel; }
    public void setFinanceiroPanel(FinanceiroPanel financeiroPanel) { this.financeiroPanel = financeiroPanel; }

    private void setupOverlay() {
        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 100));
        overlayPanel.setVisible(false);
        setGlassPane(overlayPanel);
    }

    public void setOverlayVisible(boolean visible) {
        overlayPanel.setVisible(visible);
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
                setOverlayVisible(true);
                commandBar.setVisible(true);
            }
        });
    }

    private void setupMenuBar() {
        DashboardMenuBarFactory menuFactory = new DashboardMenuBarFactory(this, appContext);
        setJMenuBar(menuFactory.createMenuBar());
    }

    private void setupTabs() {
        DashboardViewBuilder viewBuilder = new DashboardViewBuilder(this, appContext);
        this.mainTabbedPane = viewBuilder.buildMainTabbedPane();
        add(mainTabbedPane);
    }

    public void rebuildAndShowHomePanel() {
        int homeTabIndex = mainTabbedPane.indexOfTab(I18n.getString("dashboard.tab.home"));
        if (homeTabIndex != -1) {
            homePanel = new HomePanel(appContext, this);
            mainTabbedPane.setComponentAt(homeTabIndex, homePanel);
            mainTabbedPane.revalidate();
            mainTabbedPane.repaint();
        }
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
        actionHandler.execute(action, params);
    }

    public void refreshPanel(String panelTitle) {
        Component panel = findPanelByTitle(mainTabbedPane, panelTitle);
        if (panel instanceof Refreshable) {
            ((Refreshable) panel).refreshData();
        }
    }

    private Component findPanelByTitle(JTabbedPane tabbedPane, String title) {
        if (tabbedPane == null) return null;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equalsIgnoreCase(title)) {
                return tabbedPane.getComponentAt(i);
            }
            if (tabbedPane.getComponentAt(i) instanceof JTabbedPane) {
                Component found = findPanelByTitle((JTabbedPane) tabbedPane.getComponentAt(i), title);
                if (found != null) return found;
            }
        }
        return null;
    }

    public void setTheme(String themeName) {
        try {
            UIManager.setLookAndFeel("light".equals(themeName) ? new FlatLightLaf() : new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(this);
            personalizationService.savePreference("theme", themeName);
            logger.info("Tema alterado para: " + themeName);
        } catch (Exception ex) {
            logger.error("Falha ao mudar o tema.", ex);
            UIMessageUtil.showErrorMessage(this, I18n.getString("dashboard.theme.error"), I18n.getString("dashboard.theme.errorTitle"));
        }
    }

    public void confirmarSaida() {
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