package com.titanaxis.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.titanaxis.app.AppContext;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.view.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardFrame extends JFrame {
    private final AppContext appContext;
    private final AuthService authService;
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
        JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        mainTabbedPane.addTab("Vendas", new VendaPanel(appContext));

        if (authService.isGerente()) {
            JTabbedPane produtosEstoqueTabbedPane = new JTabbedPane();
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
            JTabbedPane adminTabbedPane = new JTabbedPane();
            adminTabbedPane.addTab("Gestão de Usuários", new UsuarioPanel(appContext));
            adminTabbedPane.addTab("Logs de Auditoria", new AuditoriaPanel(appContext));
            mainTabbedPane.addTab("Administração", adminTabbedPane);
        }

        add(mainTabbedPane);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");
        JMenu menuTema = new JMenu("Alterar Tema");
        ButtonGroup themeGroup = new ButtonGroup();

        JRadioButtonMenuItem lightThemeItem = new JRadioButtonMenuItem("Tema Claro");
        lightThemeItem.addActionListener(e -> setTheme("light"));
        JRadioButtonMenuItem darkThemeItem = new JRadioButtonMenuItem("Tema Escuro");
        darkThemeItem.setSelected(true);
        darkThemeItem.addActionListener(e -> setTheme("dark"));

        themeGroup.add(lightThemeItem);
        themeGroup.add(darkThemeItem);
        menuTema.add(lightThemeItem);
        menuTema.add(darkThemeItem);

        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> fazerLogout());

        JMenuItem sairMenuItem = new JMenuItem("Sair");
        sairMenuItem.addActionListener(e -> confirmarSaida());

        menuArquivo.add(menuTema);
        menuArquivo.addSeparator();
        menuArquivo.add(logoutMenuItem);
        menuArquivo.add(sairMenuItem);
        menuBar.add(menuArquivo);
        setJMenuBar(menuBar);
    }

    private void fazerLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja sair da sua conta?",
                "Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            new LoginFrame(appContext).setVisible(true);
            this.dispose();
        }
    }

    private void confirmarSaida() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja fechar a aplicação?",
                "Sair do Sistema",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
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
            JOptionPane.showMessageDialog(this,
                    "Ocorreu um erro ao alterar o tema.",
                    "Erro de Tema",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}