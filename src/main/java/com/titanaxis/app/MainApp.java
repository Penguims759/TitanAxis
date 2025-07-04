package com.titanaxis.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.titanaxis.util.AppLogger; // Importado
import com.titanaxis.util.DatabaseConnection;
import com.titanaxis.util.JpaUtil;
import com.titanaxis.view.LoginFrame;

import javax.swing.*;
import java.util.logging.Level; // Importado
import java.util.logging.Logger; // Importado

public class MainApp {
    private static final Logger logger = AppLogger.getLogger(); // Adicionado

    public static void main(String[] args) {
        // A inicialização da base de dados permanece como o primeiro passo.
        DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                FlatDarkLaf.setup();
                logger.info("Look and Feel FlatLaf inicializado com sucesso."); // ALTERADO: Usando AppLogger
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Falha ao inicializar o Look and Feel FlatLaf", ex); // ALTERADO: Usando AppLogger
            }

            // ALTERAÇÃO: Usar o Guice para criar e configurar a aplicação.
            // 1. Cria o "injetor" usando as regras definidas no AppModule.
            Injector injector = Guice.createInjector(new AppModule());

            // 2. Pede ao injetor para fornecer a instância do AppContext.
            // O Guice irá construir o AppContext e todas as suas dependências (serviços e repositórios) automaticamente.
            AppContext appContext = injector.getInstance(AppContext.class);

            // 3. A partir daqui, a aplicação continua como antes, mas agora totalmente gerida pelo Guice.
            LoginFrame loginFrame = new LoginFrame(appContext);
            loginFrame.setVisible(true);
        });

        // Adiciona um "shutdown hook" para fechar os recursos ao sair da aplicação
        Runtime.getRuntime().addShutdownHook(new Thread(JpaUtil::close));
    }
}