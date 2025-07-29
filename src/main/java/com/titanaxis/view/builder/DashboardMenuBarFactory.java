package com.titanaxis.view.builder;

import com.titanaxis.app.AppContext;
import com.titanaxis.app.MainApp;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.LoginFrame;
import com.titanaxis.view.dialogs.DashboardCustomizationDialog;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import org.slf4j.Logger;

public class DashboardMenuBarFactory {
    private final DashboardFrame frame;
    private final AppContext appContext;
    private final UIPersonalizationService personalizationService;
    private final AuthService authService;
    private static final Logger logger = AppLogger.getLogger();

    public DashboardMenuBarFactory(DashboardFrame frame, AppContext appContext) {
        this.frame = frame;
        this.appContext = appContext;
        this.authService = appContext.getAuthService();
        this.personalizationService = new UIPersonalizationService(
                authService.getUsuarioLogado().map(Usuario::getNomeUsuario).orElse("default")
        );
    }

    public JMenuBar createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(createArquivoMenu());
        menuBar.add(createViewMenu());
        return menuBar;
    }

    private JMenu createArquivoMenu() {
        final JMenu menuArquivo = new JMenu(I18n.getString("dashboard.menu.file"));
        final JMenuItem logoutMenuItem = new JMenuItem(I18n.getString("dashboard.menu.logout"));
        logoutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        logoutMenuItem.addActionListener(e -> fazerLogout());

        final JMenuItem sairMenuItem = new JMenuItem(I18n.getString("dashboard.menu.exit"));
        sairMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        sairMenuItem.addActionListener(e -> frame.confirmarSaida());

        menuArquivo.add(createIdiomaMenu());
        menuArquivo.add(createTemaMenu());
        menuArquivo.addSeparator();
        menuArquivo.add(logoutMenuItem);
        menuArquivo.add(sairMenuItem);
        return menuArquivo;
    }

    private JMenu createViewMenu() {
        final JMenu menuView = new JMenu(I18n.getString("dashboard.menu.view"));
        final JMenuItem customizeDashboardItem = new JMenuItem(I18n.getString("dashboard.menu.customize"));
        customizeDashboardItem.addActionListener(e -> openCustomizationDialog());
        menuView.add(customizeDashboardItem);
        return menuView;
    }

    private JMenu createTemaMenu() {
        final JMenu menuTema = new JMenu(I18n.getString("dashboard.menu.changeTheme"));
        final ButtonGroup themeGroup = new ButtonGroup();
        final JRadioButtonMenuItem lightThemeItem = new JRadioButtonMenuItem(I18n.getString("dashboard.menu.lightTheme"));
        lightThemeItem.addActionListener(e -> frame.setTheme("light"));
        final JRadioButtonMenuItem darkThemeItem = new JRadioButtonMenuItem(I18n.getString("dashboard.menu.darkTheme"));
        darkThemeItem.addActionListener(e -> frame.setTheme("dark"));

        themeGroup.add(lightThemeItem);
        themeGroup.add(darkThemeItem);
        menuTema.add(lightThemeItem);
        menuTema.add(darkThemeItem);

        if ("light".equals(personalizationService.getPreference("theme", "dark"))) {
            lightThemeItem.setSelected(true);
        } else {
            darkThemeItem.setSelected(true);
        }
        return menuTema;
    }

    private JMenu createIdiomaMenu() {
        final JMenu menuIdioma = new JMenu(I18n.getString("dashboard.menu.language"));
        final ButtonGroup languageGroup = new ButtonGroup();
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
        return menuIdioma;
    }

    private void openCustomizationDialog() {
        DashboardCustomizationDialog dialog = new DashboardCustomizationDialog(frame, personalizationService, frame::rebuildAndShowHomePanel);
        dialog.setVisible(true);
    }

    private void fazerLogout() {
        if (UIMessageUtil.showConfirmDialog(frame, I18n.getString("dashboard.logout.confirm"), I18n.getString("dashboard.logout.title"))) {
            authService.logout();
            new LoginFrame(appContext).setVisible(true);
            frame.dispose();
        }
    }

    private void switchLanguage(String language, String country) {
        try {
            Locale newLocale = new Locale(language, country);
            personalizationService.savePreference("locale", newLocale.toLanguageTag());
            new UIPersonalizationService("default_user").savePreference("locale", newLocale.toLanguageTag());
            I18n.setLocale(newLocale);
            JOptionPane.showMessageDialog(frame, I18n.getString("dashboard.language.restartMessage"), I18n.getString("dashboard.language.restartTitle"), JOptionPane.INFORMATION_MESSAGE);

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
        } catch (URISyntaxException | IOException e) {
            logger.error("Falha ao tentar reiniciar a aplicação.", e);
            UIMessageUtil.showErrorMessage(frame, "Não foi possível reiniciar a aplicação automaticamente. Por favor, reinicie manualmente.", "Erro de Reinicialização");
            System.exit(0);
        }
    }
}