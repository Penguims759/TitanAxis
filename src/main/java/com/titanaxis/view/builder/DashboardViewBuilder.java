package com.titanaxis.view.builder;

import com.titanaxis.app.AppContext;
import com.titanaxis.presenter.*;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.I18n;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.panels.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class DashboardViewBuilder {
    private final DashboardFrame frame;
    private final AppContext appContext;
    private final AuthService authService;

    public DashboardViewBuilder(DashboardFrame frame, AppContext appContext) {
        this.frame = frame;
        this.appContext = appContext;
        this.authService = appContext.getAuthService();
    }

    public JTabbedPane buildMainTabbedPane() {
        JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        mainTabbedPane.addChangeListener(createRefreshListener());

        // Aba Home (sempre visível)
        HomePanel homePanel = new HomePanel(appContext, frame);
        frame.setHomePanel(homePanel);
        mainTabbedPane.addTab(I18n.getString("dashboard.tab.home"), homePanel);

        // --- ALTERAÇÃO FINAL: "Produtos" e "Vendas" são agora visíveis para todos, nesta ordem. ---
        mainTabbedPane.addTab(I18n.getString("dashboard.tab.productsAndStock"), buildProdutosEstoqueTabbedPane());
        mainTabbedPane.addTab(I18n.getString("dashboard.tab.sales"), buildVendasTabbedPane());

        // Abas para Gerentes e Admins continuam a ter a sua lógica de permissão
        if (authService.isGerente()) {
            FinanceiroPanel financeiroPanel = new FinanceiroPanel(appContext);
            frame.setFinanceiroPanel(financeiroPanel);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.financial"), financeiroPanel);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.registrations"), buildCadastrosTabbedPane());
            RelatorioPanel relatorioPanel = new RelatorioPanel(appContext);
            frame.setRelatorioPanel(relatorioPanel);
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.reports"), relatorioPanel);
        }

        // Abas apenas para Admins
        if (authService.isAdmin()) {
            mainTabbedPane.addTab(I18n.getString("dashboard.tab.administration"), buildAdminTabbedPane());
        }

        return mainTabbedPane;
    }

    private JTabbedPane buildVendasTabbedPane() {
        JTabbedPane vendasTabbedPane = new JTabbedPane();
        vendasTabbedPane.addChangeListener(createRefreshListener());

        VendaPanel vendaPanel = new VendaPanel(appContext);
        frame.setVendaPanel(vendaPanel);
        vendasTabbedPane.addTab(I18n.getString("dashboard.tab.newSale"), vendaPanel);

        HistoricoVendasPanel historicoVendasPanel = new HistoricoVendasPanel();
        historicoVendasPanel.setAppContext(appContext);
        new HistoricoVendasPresenter(historicoVendasPanel, appContext.getVendaService());
        frame.setHistoricoVendasPanel(historicoVendasPanel);
        vendasTabbedPane.addTab(I18n.getString("dashboard.tab.history"), historicoVendasPanel);

        frame.setVendasTabbedPane(vendasTabbedPane);
        return vendasTabbedPane;
    }

    private JTabbedPane buildProdutosEstoqueTabbedPane() {
        JTabbedPane produtosEstoqueTabbedPane = new JTabbedPane();
        produtosEstoqueTabbedPane.addChangeListener(createRefreshListener());

        ProdutoPanel produtoPanel = new ProdutoPanel();
        produtoPanel.setAppContext(appContext);
        new ProdutoPresenter(produtoPanel, appContext.getProdutoService(), authService);
        frame.setProdutoPanel(produtoPanel);
        produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.productsAndBatches"), produtoPanel);

        // Apenas Gerentes e Admins podem ver estas sub-abas
        if (authService.isGerente()) {
            CategoriaPanel categoriaPanel = new CategoriaPanel();
            new CategoriaPresenter(categoriaPanel, appContext.getCategoriaService(), authService);
            frame.setCategoriaPanel(categoriaPanel);
            produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.categories"), categoriaPanel);

            AlertaPanel alertaPanel = new AlertaPanel(appContext);
            frame.setAlertaPanel(alertaPanel);
            produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.stockAlerts"), alertaPanel);

            MovimentosPanel movimentosPanel = new MovimentosPanel(frame);
            movimentosPanel.setAppContext(appContext);
            new MovimentoPresenter(movimentosPanel, appContext.getMovimentoService());
            frame.setMovimentosPanel(movimentosPanel);
            produtosEstoqueTabbedPane.addTab(I18n.getString("dashboard.tab.movementHistory"), movimentosPanel);
        }

        frame.setProdutosEstoqueTabbedPane(produtosEstoqueTabbedPane);
        return produtosEstoqueTabbedPane;
    }

    private JTabbedPane buildCadastrosTabbedPane() {
        JTabbedPane cadastrosTabbedPane = new JTabbedPane();
        cadastrosTabbedPane.addChangeListener(createRefreshListener());

        ClientePanel clientePanel = new ClientePanel();
        new ClientePresenter(clientePanel, appContext.getClienteService(), authService);
        frame.setClientePanel(clientePanel);
        cadastrosTabbedPane.addTab(I18n.getString("dashboard.tab.clients"), clientePanel);

        FornecedorPanel fornecedorPanel = new FornecedorPanel();
        new FornecedorPresenter(fornecedorPanel, appContext.getFornecedorService(), authService);
        frame.setFornecedorPanel(fornecedorPanel);
        cadastrosTabbedPane.addTab(I18n.getString("dashboard.tab.suppliers"), fornecedorPanel);

        frame.setCadastrosTabbedPane(cadastrosTabbedPane);
        return cadastrosTabbedPane;
    }

    private JTabbedPane buildAdminTabbedPane() {
        JTabbedPane adminTabbedPane = new JTabbedPane();
        adminTabbedPane.addChangeListener(createRefreshListener());

        UsuarioPanel usuarioPanel = new UsuarioPanel();
        new UsuarioPresenter(usuarioPanel, authService);
        frame.setUsuarioPanel(usuarioPanel);
        adminTabbedPane.addTab(I18n.getString("dashboard.tab.userManagement"), usuarioPanel);

        AuditoriaPanel auditoriaPanel = new AuditoriaPanel(appContext);
        frame.setAuditoriaPanel(auditoriaPanel);
        adminTabbedPane.addTab(I18n.getString("dashboard.tab.auditLogs"), auditoriaPanel);

        frame.setAdminTabbedPane(adminTabbedPane);
        return adminTabbedPane;
    }

    private ChangeListener createRefreshListener() {
        return e -> {
            if (e.getSource() instanceof JTabbedPane) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                Component selectedComponent = sourceTabbedPane.getSelectedComponent();

                if (selectedComponent instanceof JTabbedPane) {
                    JTabbedPane nestedTabbedPane = (JTabbedPane) selectedComponent;
                    selectedComponent = nestedTabbedPane.getSelectedComponent();
                }

                if (selectedComponent instanceof DashboardFrame.Refreshable) {
                    ((DashboardFrame.Refreshable) selectedComponent).refreshData();
                }
            }
        };
    }
}