// src/main/java/com/titanaxis/view/DashboardFrame.java
package com.titanaxis.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.titanaxis.app.AppContext;
import com.titanaxis.model.Cliente;
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

    // Referências aos componentes da UI para manipulação pelo assistente
    private JTabbedPane mainTabbedPane;
    private JTabbedPane produtosEstoqueTabbedPane;
    private ProdutoPanel produtoPanel;
    private ClientePanel clientePanel;
    private CategoriaPanel categoriaPanel;
    private AlertaPanel alertaPanel;
    private MovimentosPanel movimentosPanel;
    private RelatorioPanel relatorioPanel;
    private UsuarioPanel usuarioPanel;
    private AuditoriaPanel auditoriaPanel;
    private AIAssistantPanel aiAssistantPanel;
    private VendaPanel vendaPanel; // NOVO


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

        // Inicia as ações pós-carregamento da UI
        SwingUtilities.invokeLater(() -> {
            setTheme(personalizationService.getPreference("theme", "dark"));
            showProactiveInsights();
        });
    }

    private void setupNestedTabs() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        // --- ABA DO ASSISTENTE ---
        if (authService.isGerente()) {
            aiAssistantPanel = new AIAssistantPanel(appContext);
            // ALTERADO: Texto da aba modificado para remover o emoticon
            mainTabbedPane.addTab("Assistente IA", aiAssistantPanel);
            mainTabbedPane.addChangeListener(createRefreshListener(aiAssistantPanel));
        }

        // --- ABA DE VENDAS ---
        vendaPanel = new VendaPanel(appContext); // ALTERADO: Atribuído à variável de instância
        mainTabbedPane.addTab("Vendas", vendaPanel);
        mainTabbedPane.addChangeListener(createRefreshListener(vendaPanel)); // NOVO


        // --- ABAS DE GESTÃO (GERENTE) ---
        if (authService.isGerente()) {
            // Sub-abas de Produtos & Estoque
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

            // Listener para as sub-abas
            produtosEstoqueTabbedPane.addChangeListener(e -> {
                Component selected = produtosEstoqueTabbedPane.getSelectedComponent();
                if (selected instanceof ProdutoPanel) ((ProdutoPanel) selected).refreshData();
                else if (selected instanceof CategoriaPanel) ((CategoriaPanel) selected).refreshData();
                else if (selected instanceof AlertaPanel) ((AlertaPanel) selected).refreshData();
                else if (selected instanceof MovimentosPanel) ((MovimentosPanel) selected).refreshData();
            });

            // Aba de Clientes
            clientePanel = new ClientePanel(appContext);
            mainTabbedPane.addTab("Clientes", clientePanel);
            mainTabbedPane.addChangeListener(createRefreshListener(clientePanel));

            // Aba de Relatórios
            relatorioPanel = new RelatorioPanel(appContext);
            mainTabbedPane.addTab("Relatórios", relatorioPanel);
            mainTabbedPane.addChangeListener(createRefreshListener(relatorioPanel));
        }

        // --- ABAS DE ADMINISTRAÇÃO (ADMIN) ---
        if (authService.isAdmin()) {
            JTabbedPane adminTabbedPane = new JTabbedPane();
            usuarioPanel = new UsuarioPanel(appContext);
            auditoriaPanel = new AuditoriaPanel(appContext);

            adminTabbedPane.addTab("Gestão de Usuários", usuarioPanel);
            adminTabbedPane.addTab("Logs de Auditoria", auditoriaPanel);
            mainTabbedPane.addTab("Administração", adminTabbedPane);

            // Listener para as sub-abas de admin
            adminTabbedPane.addChangeListener(e -> {
                Component selected = adminTabbedPane.getSelectedComponent();
                if (selected instanceof UsuarioPanel) ((UsuarioPanel) selected).refreshData();
                else if (selected instanceof AuditoriaPanel) ((AuditoriaPanel) selected).refreshData();
            });
        }

        add(mainTabbedPane);
    }

    private ChangeListener createRefreshListener(Component panel) {
        return e -> {
            if (mainTabbedPane.getSelectedComponent() == panel) {
                try {
                    // Usa reflection para chamar o método 'refreshData', se ele existir.
                    panel.getClass().getMethod("refreshData").invoke(panel);
                } catch (Exception ex) {
                    // O método não existe ou falhou, não faz nada. Silencioso.
                }
            }
        };
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

    private void showProactiveInsights() {
        if (aiAssistantPanel != null) {
            String insights = appContext.getAnalyticsService().getProactiveInsightsSummary();
            if (insights != null && !insights.isEmpty()) {
                mainTabbedPane.setSelectedComponent(aiAssistantPanel);
                aiAssistantPanel.appendMessage("Olá! Tenho alguns insights para você hoje:\n" + insights, false);
            }
        }
    }

    public void executeAction(Action action, Map<String, Object> params) {
        try {
            switch (action) {
                case GUIDE_NAVIGATE_TO_ADD_LOTE:
                    mainTabbedPane.setSelectedComponent(produtosEstoqueTabbedPane);
                    produtosEstoqueTabbedPane.setSelectedComponent(produtoPanel);
                    produtoPanel.selectFirstProduct();
                    UIGuide.highlightComponent(produtoPanel.getAddLoteButton());
                    break;

                case DIRECT_CREATE_CLIENT:
                    String nome = (String) params.get("nome");
                    String contato = (String) params.get("contato");
                    Cliente novoCliente = new Cliente(nome, contato, "");
                    appContext.getClienteService().salvar(novoCliente, authService.getUsuarioLogado().orElse(null));
                    UIMessageUtil.showInfoMessage(this, "Cliente '" + nome + "' foi criado com sucesso!", "Ação Concluída");
                    if (clientePanel != null) clientePanel.refreshData();
                    break;

                case DIRECT_GENERATE_SALES_REPORT_PDF:
                    UIMessageUtil.showInfoMessage(this, "Relatório de Vendas em PDF gerado (Simulação).\nNuma implementação real, abriríamos um diálogo para salvar o ficheiro.", "Ação Concluída");
                    break;

                case UI_CHANGE_THEME:
                    setTheme((String) params.get("theme"));
                    break;

                // NOVOS CASES
                case START_SALE_FOR_CLIENT:
                    mainTabbedPane.setSelectedComponent(vendaPanel);
                    vendaPanel.refreshData(); // Garante que a lista de clientes está atualizada
                    Cliente cliente = (Cliente) params.get("cliente");
                    vendaPanel.selecionarCliente(cliente);
                    break;

                case DISPLAY_COMPLEX_RESPONSE:
                    String text = (String) params.get("text");
                    aiAssistantPanel.appendMessage(text, false);
                    break;
            }
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao executar a ação: " + e.getMessage(), "Erro na Ação");
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