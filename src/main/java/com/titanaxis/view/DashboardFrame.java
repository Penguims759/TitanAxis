// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/DashboardFrame.java
package com.titanaxis.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.titanaxis.app.AppContext;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.UIMessageUtil; // Importado
import com.titanaxis.view.panels.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent; // Importado
import javax.swing.event.ChangeListener; // Importado
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardFrame extends JFrame {
    private final AppContext appContext; // Adicionado final
    private final AuthService authService; // Adicionado final
    private static final Logger logger = AppLogger.getLogger();

    public DashboardFrame(AppContext appContext) {
        super("Dashboard - TitanAxis");
        this.appContext = appContext;
        this.authService = appContext.getAuthService();

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
    }

    private void setupNestedTabs() {
        final JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT); // Adicionado final
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        final VendaPanel vendaPanel = new VendaPanel(appContext); // Instância do VendaPanel
        mainTabbedPane.addTab("Vendas", vendaPanel);

        if (authService.isGerente()) {
            final JTabbedPane produtosEstoqueTabbedPane = new JTabbedPane(); // Adicionado final
            final ProdutoPanel produtoPanel = new ProdutoPanel(appContext); // Instância do ProdutoPanel
            final CategoriaPanel categoriaPanel = new CategoriaPanel(appContext); // Instância da CategoriaPanel
            final AlertaPanel alertaPanel = new AlertaPanel(appContext); // Instância da AlertaPanel
            final MovimentosPanel movimentosPanel = new MovimentosPanel(appContext); // Instância da MovimentosPanel

            produtosEstoqueTabbedPane.addTab("Gestão de Produtos e Lotes", produtoPanel); // Adicionado instância
            produtosEstoqueTabbedPane.addTab("Categorias", categoriaPanel); // Adicionado instância
            produtosEstoqueTabbedPane.addTab("Alertas de Estoque", alertaPanel); // Adicionado instância
            produtosEstoqueTabbedPane.addTab("Histórico de Movimentos", movimentosPanel); // Adicionado instância
            mainTabbedPane.addTab("Produtos & Estoque", produtosEstoqueTabbedPane);

            // ALTERADO: Adiciona ChangeListener para recarregar dados dos sub-painéis ao selecionar *suas* abas
            produtosEstoqueTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (produtosEstoqueTabbedPane.getSelectedComponent() == produtoPanel) {
                        produtoPanel.refreshData();
                    } else if (produtosEstoqueTabbedPane.getSelectedComponent() == categoriaPanel) { // NOVO: Refresh para CategoriaPanel
                        categoriaPanel.refreshData();
                    } else if (produtosEstoqueTabbedPane.getSelectedComponent() == alertaPanel) { // NOVO: Refresh para AlertaPanel
                        alertaPanel.refreshData();
                    } else if (produtosEstoqueTabbedPane.getSelectedComponent() == movimentosPanel) { // NOVO: Refresh para MovimentosPanel
                        movimentosPanel.refreshData();
                    }
                }
            });

            // NOVO: Adiciona ChangeListener para o mainTabbedPane para recarregar todos os dados
            // quando a aba "Produtos & Estoque" (pai) é selecionada
            mainTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mainTabbedPane.getSelectedComponent() == produtosEstoqueTabbedPane) {
                        logger.info("Aba 'Produtos & Estoque' selecionada. Recarregando dados dos subpainéis.");
                        produtoPanel.refreshData();
                        categoriaPanel.refreshData();
                        alertaPanel.refreshData();
                        movimentosPanel.refreshData();
                    }
                }
            });
        }

        if (authService.isGerente()) {
            final ClientePanel clientePanel = new ClientePanel(appContext); // Instância do ClientePanel
            mainTabbedPane.addTab("Clientes", clientePanel);
            // NOVO: ChangeListener para aba de Clientes
            mainTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mainTabbedPane.getSelectedComponent() == clientePanel) {
                        logger.info("Aba 'Clientes' selecionada. Recarregando dados.");
                        clientePanel.refreshData();
                    }
                }
            });
        }

        if (authService.isGerente()) {
            final RelatorioPanel relatorioPanel = new RelatorioPanel(appContext); // Instância do RelatorioPanel
            mainTabbedPane.addTab("Relatórios", relatorioPanel);
            // NOVO: ChangeListener para aba de Relatórios
            mainTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mainTabbedPane.getSelectedComponent() == relatorioPanel) {
                        logger.info("Aba 'Relatórios' selecionada. Recarregando dados.");
                        relatorioPanel.refreshData();
                    }
                }
            });
        }

        if (authService.isAdmin()) {
            final JTabbedPane adminTabbedPane = new JTabbedPane(); // Adicionado final
            // NOVO: Instâncias dos painéis de administração para poder referenciá-los no ChangeListener
            final UsuarioPanel usuarioPanel = new UsuarioPanel(appContext);
            final AuditoriaPanel auditoriaPanel = new AuditoriaPanel(appContext);

            adminTabbedPane.addTab("Gestão de Usuários", usuarioPanel);
            adminTabbedPane.addTab("Logs de Auditoria", auditoriaPanel);
            mainTabbedPane.addTab("Administração", adminTabbedPane);

            // NOVO: ChangeListener para abas de Administração
            adminTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (adminTabbedPane.getSelectedComponent() == usuarioPanel) {
                        usuarioPanel.refreshData();
                    } else if (adminTabbedPane.getSelectedComponent() == auditoriaPanel) {
                        auditoriaPanel.refreshData();
                    }
                }
            });

            // NOVO: ChangeListener para aba principal "Administração"
            mainTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mainTabbedPane.getSelectedComponent() == adminTabbedPane) {
                        logger.info("Aba 'Administração' selecionada. Recarregando dados dos subpainéis.");
                        usuarioPanel.refreshData();
                        auditoriaPanel.refreshData();
                    }
                }
            });
        }

        add(mainTabbedPane);
    }

    private void setupMenuBar() {
        final JMenuBar menuBar = new JMenuBar(); // Adicionado final
        final JMenu menuArquivo = new JMenu("Arquivo"); // Adicionado final
        final JMenu menuTema = new JMenu("Alterar Tema"); // Adicionado final
        final ButtonGroup themeGroup = new ButtonGroup(); // Adicionado final

        final JRadioButtonMenuItem lightThemeItem = new JRadioButtonMenuItem("Tema Claro"); // Adicionado final
        lightThemeItem.addActionListener(e -> setTheme("light"));
        final JRadioButtonMenuItem darkThemeItem = new JRadioButtonMenuItem("Tema Escuro"); // Adicionado final
        darkThemeItem.setSelected(true);
        darkThemeItem.addActionListener(e -> setTheme("dark"));

        themeGroup.add(lightThemeItem);
        themeGroup.add(darkThemeItem);
        menuTema.add(lightThemeItem);
        menuTema.add(darkThemeItem);

        final JMenuItem logoutMenuItem = new JMenuItem("Logout"); // Adicionado final
        logoutMenuItem.addActionListener(e -> fazerLogout());

        final JMenuItem sairMenuItem = new JMenuItem("Sair"); // Adicionado final
        sairMenuItem.addActionListener(e -> confirmarSaida());

        menuArquivo.add(menuTema);
        menuArquivo.addSeparator();
        menuArquivo.add(logoutMenuItem);
        menuArquivo.add(sairMenuItem);
        menuBar.add(menuArquivo);
        setJMenuBar(menuBar);
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

    private void setTheme(String themeName) {
        try {
            if ("light".equals(themeName)) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            }
            SwingUtilities.updateComponentTreeUI(this);
            logger.info("Tema alterado para: " + themeName);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Falha ao mudar o tema.", ex);
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro ao alterar o tema.", "Erro de Tema");
        }
    }
}