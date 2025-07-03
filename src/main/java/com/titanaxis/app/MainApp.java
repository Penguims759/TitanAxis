package com.titanaxis.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.titanaxis.util.DatabaseConnection;
import com.titanaxis.util.JpaUtil; // IMPORTAR JPA UTIL
import com.titanaxis.view.LoginFrame;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Inicializa a base de dados com Flyway ANTES de qualquer outra coisa
        DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                FlatDarkLaf.setup();
            } catch (Exception ex) {
                System.err.println("Falha ao inicializar o Look and Feel FlatLaf");
            }

            AppContext appContext = new AppContext();

            LoginFrame loginFrame = new LoginFrame(appContext);
            loginFrame.setVisible(true);
        });

        // Adiciona um "shutdown hook" para fechar os recursos ao sair da aplicação
        Runtime.getRuntime().addShutdownHook(new Thread(JpaUtil::close));
    }
}