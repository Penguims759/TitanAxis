// src/main/java/com/titanaxis/app/MainApp.java
package com.titanaxis.app;

import com.titanaxis.util.DatabaseConnection;
import com.titanaxis.view.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;

/**
 * Classe principal da aplicação de Gerenciamento de Estoque.
 * Responsável por inicializar a base de dados e exibir a tela de login.
 */
public class MainApp {
    public static void main(String[] args) {
        // A inicialização do banco de dados pode ocorrer antes
        DatabaseConnection.initializeDatabase();

        // Colocamos toda a lógica de UI dentro do invokeLater
        SwingUtilities.invokeLater(() -> {
            // --- CÓDIGO CORRIGIDO ---
            // Movemos a configuração do tema para dentro do invokeLater.
            // Isso garante que ele seja definido na thread de eventos da UI
            // antes que qualquer janela seja criada.
            try {
                FlatDarkLaf.setup(); // Define o tema escuro como padrão
            } catch (Exception ex) {
                System.err.println("Falha ao inicializar o Look and Feel FlatLaf");
            }

            // Agora, com o tema já garantido, criamos a janela de login
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}