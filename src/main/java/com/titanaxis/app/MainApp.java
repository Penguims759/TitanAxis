// src/main/java/com/titanaxis/app/MainApp.java
package com.titanaxis.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.DatabaseConnection;
import com.titanaxis.util.I18n;
import com.titanaxis.util.JpaUtil;
import com.titanaxis.view.LoginFrame;

import javax.swing.*;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp {
    private static final Logger logger = AppLogger.getLogger();

    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                UIPersonalizationService startupPrefs = new UIPersonalizationService("default_user");
                String savedLocaleTag = startupPrefs.getPreference("locale", "pt-BR");
                I18n.setLocale(Locale.forLanguageTag(savedLocaleTag));

                FlatDarkLaf.setup();
                logger.info("Look and Feel FlatLaf inicializado com sucesso.");
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Falha ao inicializar o Look and Feel FlatLaf", ex);
            }

            Injector injector = Guice.createInjector(new AppModule());
            AppContext appContext = injector.getInstance(AppContext.class);
            LoginFrame loginFrame = new LoginFrame(appContext);
            loginFrame.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(JpaUtil::close));
    }
}