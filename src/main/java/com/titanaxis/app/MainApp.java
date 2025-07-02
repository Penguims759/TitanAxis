package com.titanaxis.app;

import com.titanaxis.util.DatabaseConnection;
import com.titanaxis.view.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
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
    }
}