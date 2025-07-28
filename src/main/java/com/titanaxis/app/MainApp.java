package com.titanaxis.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.titanaxis.service.UIPersonalizationService;
import com.titanaxis.service.ValidationService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.ConfigurationManager;
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
        // Inicializar configurações
        ConfigurationManager config = ConfigurationManager.getInstance();
        logger.info("Iniciando " + config.getAppName() + " v" + config.getAppVersion());

        // Inicializar banco de dados
        DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                // Configurar localização baseada nas configurações
                String savedLocale = config.getDefaultLocale();
                UIPersonalizationService startupPrefs = new UIPersonalizationService("default_user");
                String userLocale = startupPrefs.getPreference("locale", savedLocale);
                I18n.setLocale(Locale.forLanguageTag(userLocale));
                logger.info("Localização configurada para: " + userLocale);

                // Configurar tema baseado nas configurações
                String defaultTheme = config.getDefaultTheme();
                String savedTheme = startupPrefs.getPreference("theme", defaultTheme);
                
                if ("light".equalsIgnoreCase(savedTheme)) {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                } else {
                    FlatDarkLaf.setup();
                }
                
                logger.info("Look and Feel configurado: " + savedTheme);
                logger.info("Look and Feel FlatLaf inicializado com sucesso.");
                
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Falha ao inicializar o Look and Feel FlatLaf", ex);
                // Tentar usar Look and Feel padrão do sistema
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
                    logger.info("Usando Look and Feel padrão do sistema como fallback");
                } catch (Exception fallbackEx) {
                    logger.log(Level.WARNING, "Falha ao configurar Look and Feel padrão", fallbackEx);
                }
            }

            try {
                // Verificar conectividade do banco antes de prosseguir
                if (!JpaUtil.isOpen()) {
                    logger.severe("Conexão com banco de dados não está disponível. Encerrando aplicação.");
                    JOptionPane.showMessageDialog(null, 
                        "Erro de conexão com banco de dados.\nVerifique as configurações e tente novamente.", 
                        "Erro de Conexão", 
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                    return;
                }

                logger.info("Conexão com banco verificada: " + JpaUtil.getConnectionInfo());

                // Inicializar injeção de dependência
                Injector injector = Guice.createInjector(new AppModule());
                AppContext appContext = injector.getInstance(AppContext.class);
                
                // Criar e exibir tela de login
                LoginFrame loginFrame = new LoginFrame(appContext);
                loginFrame.setVisible(true);
                
                logger.info("Aplicação inicializada com sucesso");

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Erro fatal durante inicialização da aplicação", ex);
                JOptionPane.showMessageDialog(null, 
                    "Erro fatal durante inicialização:\n" + ex.getMessage(), 
                    "Erro Fatal", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

        // Configurar shutdown hooks para limpeza adequada de recursos
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Iniciando processo de encerramento da aplicação...");
            
            try {
                // Fechar ValidationService
                ValidationService.close();
                logger.info("ValidationService fechado");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Erro ao fechar ValidationService", e);
            }

            try {
                // Fechar JpaUtil (conexões de banco)
                JpaUtil.close();
                logger.info("Conexões de banco fechadas");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Erro ao fechar conexões de banco", e);
            }

            logger.info("Processo de encerramento concluído");
        }, "Shutdown-Hook"));

        // Log de informações do sistema para debug
        logSystemInfo(config);
    }

    /**
     * Registra informações do sistema para ajudar no debug.
     */
    private static void logSystemInfo(ConfigurationManager config) {
        logger.info("=== Informações do Sistema ===");
        logger.info("Java Version: " + System.getProperty("java.version"));
        logger.info("Java Vendor: " + System.getProperty("java.vendor"));
        logger.info("OS Name: " + System.getProperty("os.name"));
        logger.info("OS Version: " + System.getProperty("os.version"));
        logger.info("OS Architecture: " + System.getProperty("os.arch"));
        logger.info("User Home: " + System.getProperty("user.home"));
        logger.info("Working Directory: " + System.getProperty("user.dir"));
        logger.info("Available Processors: " + Runtime.getRuntime().availableProcessors());
        logger.info("Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
        logger.info("=== Configurações da Aplicação ===");
        logger.info("App Name: " + config.getAppName());
        logger.info("App Version: " + config.getAppVersion());
        logger.info("Database URL: " + config.getDatabaseUrl());
        logger.info("Default Locale: " + config.getDefaultLocale());
        logger.info("Default Theme: " + config.getDefaultTheme());
        logger.info("================================");
    }
}