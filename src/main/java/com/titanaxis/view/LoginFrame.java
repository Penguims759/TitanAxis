// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/LoginFrame.java
package com.titanaxis.view;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.UIMessageUtil; // Importado

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    private final AppContext appContext; // Adicionado final
    private final AuthService authService; // Adicionado final
    private final JTextField usernameField; // Adicionado final
    private final JPasswordField passwordField; // Adicionado final
    private static final Logger logger = AppLogger.getLogger();

    public LoginFrame(AppContext appContext) {
        super("Login - TitanAxis");
        this.appContext = appContext;
        this.authService = appContext.getAuthService();

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
        // Adicionado final aos campos para que sejam inicializados no construtor
        JLabel passLabel = new JLabel("Senha:");
        passwordField = new JPasswordField(20);

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
            UIMessageUtil.showErrorMessage(this, "Nome de utilizador e senha não podem ser vazios.", "Erro de Login");
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = authService.login(username, password);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                logger.info("Usuário " + usuario.getNomeUsuario() + " logou com sucesso.");
                UIMessageUtil.showInfoMessage(this, "Login bem-sucedido! Bem-vindo(a), " + usuario.getNomeUsuario() + "!", "Sucesso");

                DashboardFrame dashboard = new DashboardFrame(appContext);
                dashboard.setVisible(true);
                this.dispose();
            } else {
                UIMessageUtil.showErrorMessage(this, "Nome de utilizador ou senha inválidos.", "Erro de Login");
                logger.warning("Falha de login para o usuário: " + username);
            }
        } catch (PersistenciaException ex) {
            // Captura e trata exceções relacionadas à persistência de dados.
            logger.log(Level.SEVERE, "Erro de base de dados durante o login do usuário: " + username, ex);
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro ao conectar à base de dados. Por favor, tente novamente.", "Erro de Conexão");
        } catch (Exception ex) {
            // Captura quaisquer outras exceções inesperadas para evitar que a aplicação trave.
            logger.log(Level.SEVERE, "Erro inesperado durante o login do usuário: " + username, ex);
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro inesperado. Por favor, tente novamente.", "Erro");
        }
    }
}