// src/main/java/com/titanaxis/view/LoginFrame.java
package com.titanaxis.view;

import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.AppLogger;

import javax.swing.*;
import java.awt.*;
import java.util.Optional; // Import necessário
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Janela de login da aplicação. Permite que os usuários insiram suas credenciais
 * para aceder ao sistema.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private static final Logger logger = AppLogger.getLogger();

    public LoginFrame() {
        super("Login - TitanAxis");
        this.authService = new AuthService();

        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Bem-vindo ao Sistema TitanAxis");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Nome de Utilizador:");
        usernameField = new JTextField(20);

        JLabel passLabel = new JLabel("Senha:");
        passwordField = new JPasswordField(20);

        // Adiciona um ActionListener ao campo de senha para permitir o login com a tecla Enter
        passwordField.addActionListener(e -> performLogin());

        inputPanel.add(userLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passLabel);
        inputPanel.add(passwordField);

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(20));

        JButton loginButton = new JButton("Entrar");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        panel.add(loginButton);
        add(panel);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome de utilizador e senha não podem ser vazios.", "Erro de Login", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = authService.login(username, password);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                logger.info("Usuário " + usuario.getNomeUsuario() + " logou com sucesso.");
                JOptionPane.showMessageDialog(this, "Login bem-sucedido! Bem-vindo(a), " + usuario.getNomeUsuario() + "!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                DashboardFrame dashboard = new DashboardFrame(authService);

                // ALTERAÇÃO: Tornamos o dashboard visível ANTES de descartar o login.
                dashboard.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Nome de utilizador ou senha inválidos.", "Erro de Login", JOptionPane.ERROR_MESSAGE);
                logger.warning("Falha de login para o usuário: " + username);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Erro inesperado durante o login do usuário: " + username, ex);
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado. Por favor, tente novamente.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}