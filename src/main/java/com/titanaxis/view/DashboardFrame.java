package com.titanaxis.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.titanaxis.app.AppContext;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.ai.Action;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.UIGuide;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.panels.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private JTabbedPane adminTabbedPane;
    private ProdutoPanel produtoPanel;
    private ClientePanel clientePanel;
    private CategoriaPanel categoriaPanel;
    private AlertaPanel alertaPanel;
    private MovimentosPanel movimentosPanel;
    private RelatorioPanel relatorioPanel;
    private UsuarioPanel usuarioPanel;
    private AuditoriaPanel auditoriaPanel;
    private AIAssistantPanel aiAssistantPanel;
    private VendaPanel vendaPanel;


    public DashboardFrame(AppContext appContext) {
        super("Dashboard - TitanAxis");
        this.appContext = appContext;
        this.authService = appContext.getAuthService();
        this.personalizationService = new UIPersonalizationService(
                authService.getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("default")
        );

        setSize(1200, 800);
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

        SwingUtilities.invokeLater(() -> {
            setTheme(personalizationService.getPreference("theme", "dark"));
            showProactiveInsights();
        });
    }

    private void setupMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu menuArquivo = new JMenu("Arquivo");
        final JMenu menuTema = new JMenu("Alterar Tema");
        final ButtonGroup themeGroup = new ButtonGroup();

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
        logoutMenuItem.addActionListener(e -> fazerLogout());

        final JMenuItem sairMenuItem = new JMenuItem("Sair");
        sairMenuItem.addActionListener(e -> confirmarSaida());

        menuArquivo.add(menuTema);
        menuArquivo.addSeparator();
        menuArquivo.add(logoutMenuItem);
        menuArquivo.add(sairMenuItem);
        menuBar.add(menuArquivo);
        setJMenuBar(menuBar);
    }

    private void setupNestedTabs() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        if (authService.isGerente()) {
            aiAssistantPanel = new AIAssistantPanel(appContext);
            mainTabbedPane.addTab("Assistente IA", aiAssistantPanel);
            mainTabbedPane.addChangeListener(createRefreshListener(aiAssistantPanel));
        }

        vendaPanel = new VendaPanel(appContext);
        mainTabbedPane.addTab("Vendas", vendaPanel);
        mainTabbedPane.addChangeListener(createRefreshListener(vendaPanel));


        if (authService.isGerente()) {
            produtosEstoqueTabbedPane = new JTabbedPane();
            produtoPanel = new ProdutoPanel(appContext);
            categoriaPanel = new CategoriaPanel(appContext);
            alertaPanel = new AlertaPanel(appContext);
            movimentosPanel = new MovimentosPanel(appContext);

            produtosEstoqueTabbedPane.addTab("Gestão de Produtos e Lotes", produtoPanel);
            produtosEstoqueTabbedPane.addTab("Categorias", categoriaPanel);
            produtosEstoqueTabbedPane.addTab("Alertas de Estoque", alertaPanel);
            produtosEstoqueTabbedPane.addTab("Histórico de Movimentos", movimentosPanel);
            mainTabbedPane.addTab("Produtos & Estoque", produtosEstoqueTabbedPane);

            produtosEstoqueTabbedPane.addChangeListener(e -> {
                Component selected = produtosEstoqueTabbedPane.getSelectedComponent();
                if (selected instanceof ProdutoPanel) ((ProdutoPanel) selected).refreshData();
                else if (selected instanceof CategoriaPanel) ((CategoriaPanel) selected).refreshData();
                else if (selected instanceof AlertaPanel) ((AlertaPanel) selected).refreshData();
                else if (selected instanceof MovimentosPanel) ((MovimentosPanel) selected).refreshData();
            });

            clientePanel = new ClientePanel(appContext);
            mainTabbedPane.addTab("Clientes", clientePanel);
            mainTabbedPane.addChangeListener(createRefreshListener(clientePanel));

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

            adminTabbedPane.addChangeListener(e -> {
                Component selected = adminTabbedPane.getSelectedComponent();
                if (selected instanceof UsuarioPanel) ((UsuarioPanel) selected).refreshData();
                else if (selected instanceof AuditoriaPanel) ((AuditoriaPanel) selected).refreshData();
            });
        }

        add(mainTabbedPane);
    }

    public void navigateTo(String destination) {
        Map<String, Component> mainDestinations = Map.of(
                "Vendas", vendaPanel,
                "Clientes", clientePanel,
                "Produtos & Estoque", produtosEstoqueTabbedPane,
                "Relatórios", relatorioPanel,
                "Administração", adminTabbedPane,
                "Assistente IA", aiAssistantPanel
        );

        Map<String, Component> productSubDestinations = Map.of(
                "Categorias", categoriaPanel,
                "Alertas de Estoque", alertaPanel,
                "Histórico de Movimentos", movimentosPanel
        );

        Map<String, Component> adminSubDestinations = Map.of(
                "Gestão de Usuários", usuarioPanel,
                "Logs de Auditoria", auditoriaPanel
        );

        if (mainDestinations.containsKey(destination)) {
            mainTabbedPane.setSelectedComponent(mainDestinations.get(destination));
        } else if (productSubDestinations.containsKey(destination)) {
            mainTabbedPane.setSelectedComponent(produtosEstoqueTabbedPane);
            produtosEstoqueTabbedPane.setSelectedComponent(productSubDestinations.get(destination));
        } else if (adminSubDestinations.containsKey(destination)) {
            mainTabbedPane.setSelectedComponent(adminTabbedPane);
            adminTabbedPane.setSelectedComponent(adminSubDestinations.get(destination));
        }
    }

    public void executeAction(Action action, Map<String, Object> params) {
        try {
            Usuario ator = authService.getUsuarioLogado().orElse(null);

            switch (action) {
                case UI_NAVIGATE:
                    String destination = (String) params.get("destination");
                    navigateTo(destination);
                    break;

                case DIRECT_CREATE_PRODUCT:
                    String nomeProduto = (String) params.get("nome");
                    Double preco = (Double) params.get("preco");
                    Categoria categoria = (Categoria) params.get("categoria");
                    Produto novoProduto = new Produto(nomeProduto, "", preco, categoria);

                    appContext.getProdutoService().salvarProduto(novoProduto, ator);
                    UIMessageUtil.showInfoMessage(this, "Produto '" + nomeProduto + "' criado com sucesso!", "Ação Concluída");
                    if (produtoPanel != null) produtoPanel.refreshData();
                    break;

                case DIRECT_CREATE_CATEGORY:
                    String nomeCategoria = (String) params.get("nome");
                    appContext.getCategoriaService().salvar(new Categoria(nomeCategoria), ator);
                    UIMessageUtil.showInfoMessage(this, "Categoria '" + nomeCategoria + "' criada com sucesso!", "Ação Concluída");
                    if (categoriaPanel != null) categoriaPanel.refreshData();
                    break;

                case DIRECT_CREATE_USER:
                    String username = (String) params.get("username");
                    String password = (String) params.get("password");
                    NivelAcesso level = (NivelAcesso) params.get("level");
                    appContext.getAuthService().cadastrarUsuario(username, password, level, ator);
                    UIMessageUtil.showInfoMessage(this, "Utilizador '" + username + "' criado com sucesso!", "Ação Concluída");
                    if (usuarioPanel != null) usuarioPanel.refreshData();
                    break;

                case DIRECT_ADD_STOCK:
                    String productNameStock = (String) params.get("productName");
                    String lotNumber = (String) params.get("lotNumber");
                    int quantity = (Integer) params.get("quantity");
                    appContext.getProdutoService().adicionarEstoqueLote(productNameStock, lotNumber, quantity, ator);
                    UIMessageUtil.showInfoMessage(this, "Estoque do lote " + lotNumber + " atualizado com sucesso!", "Ação Concluída");
                    if (produtoPanel != null) produtoPanel.refreshData();
                    break;

                case DIRECT_UPDATE_PRODUCT:
                    String productNameUpdate = (String) params.get("productName");
                    Produto produto = appContext.getProdutoService().listarProdutos(true).stream()
                            .filter(p -> p.getNome().equalsIgnoreCase(productNameUpdate)).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado para atualização."));

                    if (params.containsKey("newPrice")) {
                        produto.setPreco((Double) params.get("newPrice"));
                        appContext.getProdutoService().salvarProduto(produto, ator);
                        UIMessageUtil.showInfoMessage(this, "Preço do produto " + productNameUpdate + " atualizado!", "Ação Concluída");
                    }
                    if (params.containsKey("active")) {
                        appContext.getProdutoService().alterarStatusProduto(produto.getId(), (Boolean) params.get("active"), ator);
                        UIMessageUtil.showInfoMessage(this, "Status do produto " + productNameUpdate + " atualizado!", "Ação Concluída");
                    }
                    if (produtoPanel != null) produtoPanel.refreshData();
                    break;

                case GUIDE_NAVIGATE_TO_ADD_LOTE:
                    mainTabbedPane.setSelectedComponent(produtosEstoqueTabbedPane);
                    produtosEstoqueTabbedPane.setSelectedComponent(produtoPanel);
                    produtoPanel.selectFirstProduct();
                    UIGuide.highlightComponent(produtoPanel.getAddLoteButton());
                    break;

                // CORRIGIDO: Lógica para a nova ação
                case GUIDE_NAVIGATE_TO_ADD_PRODUCT:
                    mainTabbedPane.setSelectedComponent(produtosEstoqueTabbedPane);
                    produtosEstoqueTabbedPane.setSelectedComponent(produtoPanel);
                    UIGuide.highlightComponent(produtoPanel.getNovoProdutoButton());
                    break;

                case DIRECT_CREATE_CLIENT:
                    String nome = (String) params.get("nome");
                    String contato = (String) params.get("contato");
                    Cliente novoCliente = new Cliente(nome, contato, "");
                    appContext.getClienteService().salvar(novoCliente, ator);
                    UIMessageUtil.showInfoMessage(this, "Cliente '" + nome + "' foi criado com sucesso!", "Ação Concluída");
                    if (clientePanel != null) clientePanel.refreshData();
                    break;

                case UI_CHANGE_THEME:
                    setTheme((String) params.get("theme"));
                    break;

                case START_SALE_FOR_CLIENT:
                    mainTabbedPane.setSelectedComponent(vendaPanel);
                    vendaPanel.refreshData();
                    Cliente cliente = (Cliente) params.get("cliente");
                    if (cliente != null) {
                        vendaPanel.selecionarCliente(cliente);
                    }
                    break;

                case DISPLAY_COMPLEX_RESPONSE:
                    String text = (String) params.get("text");
                    aiAssistantPanel.appendMessage(text, false);
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro detalhado ao executar a ação '" + action + "': ", e);
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "\nCausa Raiz: " + e.getCause().getMessage();
            }
            UIMessageUtil.showErrorMessage(this, "Erro ao executar a ação: " + errorMessage, "Erro na Ação do Assistente");
        }
    }

    private ChangeListener createRefreshListener(Component panel) {
        return e -> {
            if (mainTabbedPane.getSelectedComponent() == panel) {
                try {
                    panel.getClass().getMethod("refreshData").invoke(panel);
                } catch (Exception ex) {
                    // O método não existe ou falhou, não faz nada. Silencioso.
                }
            }
        };
    }

    private void showProactiveInsights() {
        if (aiAssistantPanel != null) {
            String insights = appContext.getAnalyticsService().getProactiveInsightsSummary();
            if (insights != null && !insights.isEmpty()) {
                mainTabbedPane.setSelectedComponent(aiAssistantPanel);
                aiAssistantPanel.appendMessage("Olá! Tenho alguns insights para você hoje:\n" + insights, false);
            }
        }
    }

    private void setTheme(String themeName) {
        try {
            if ("light".equals(themeName)) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            }
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
}