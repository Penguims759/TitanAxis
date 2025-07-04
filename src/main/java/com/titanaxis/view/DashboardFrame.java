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

        mainTabbedPane.addTab("Vendas", new VendaPanel(appContext));

        if (authService.isGerente()) {
            final JTabbedPane produtosEstoqueTabbedPane = new JTabbedPane(); // Adicionado final
            produtosEstoqueTabbedPane.addTab("Gestão de Produtos e Lotes", new ProdutoPanel(appContext));
            produtosEstoqueTabbedPane.addTab("Categorias", new CategoriaPanel(appContext));
            produtosEstoqueTabbedPane.addTab("Alertas de Estoque", new AlertaPanel(appContext));
            // ALTERADO: Agora usa o novo MovimentosPanel que segue o padrão MVP
            produtosEstoqueTabbedPane.addTab("Histórico de Movimentos", new MovimentosPanel(appContext));
            mainTabbedPane.addTab("Produtos & Estoque", produtosEstoqueTabbedPane);
        }

        if (authService.isGerente()) {
            mainTabbedPane.addTab("Clientes", new ClientePanel(appContext));
        }

        if (authService.isGerente()) {
            mainTabbedPane.addTab("Relatórios", new RelatorioPanel(appContext));
        }

        if (authService.isAdmin()) {
            final JTabbedPane adminTabbedPane = new JTabbedPane(); // Adicionado final
            adminTabbedPane.addTab("Gestão de Usuários", new UsuarioPanel(appContext));
            adminTabbedPane.addTab("Logs de Auditoria", new AuditoriaPanel(appContext));
            mainTabbedPane.addTab("Administração", adminTabbedPane);
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