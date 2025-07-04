package com.titanaxis.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.titanaxis.util.DatabaseConnection;
import com.titanaxis.util.JpaUtil;
import com.titanaxis.view.LoginFrame;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // A inicialização da base de dados permanece como o primeiro passo.
        DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                FlatDarkLaf.setup();
            } catch (Exception ex) {
                System.err.println("Falha ao inicializar o Look and Feel FlatLaf");
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