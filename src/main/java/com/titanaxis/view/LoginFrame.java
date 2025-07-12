// File: penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/LoginFrame.java
package com.titanaxis.view;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    private final AppContext appContext;
    private final AuthService authService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private static final Logger logger = AppLogger.getLogger();

    public LoginFrame(AppContext appContext) {
        super(I18n.getString("login.title")); // ALTERADO
        this.appContext = appContext;
        this.authService = appContext.getAuthService();

        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(I18n.getString("login.welcomeMessage")); // ALTERADO
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel(I18n.getString("login.usernameLabel")); // ALTERADO
        usernameField = new JTextField(20);
        JLabel passLabel = new JLabel(I18n.getString("login.passwordLabel")); // ALTERADO
        passwordField = new JPasswordField(20);

        passwordField.addActionListener(e -> performLogin());

        inputPanel.add(userLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passLabel);
        inputPanel.add(passwordField);

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(20));

        JButton loginButton = new JButton(I18n.getString("login.loginButton")); // ALTERADO
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        panel.add(loginButton);
        add(panel);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.emptyFields"), I18n.getString("login.error.title")); // ALTERADO
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = authService.login(username, password);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                logger.info("Usuário " + usuario.getNomeUsuario() + " logou com sucesso.");
                UIMessageUtil.showInfoMessage(this, I18n.getString("login.success.message", usuario.getNomeUsuario()), I18n.getString("login.success.title")); // ALTERADO

                DashboardFrame dashboard = new DashboardFrame(appContext);
                dashboard.setVisible(true);
                this.dispose();
            } else {
                UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.invalidCredentials"), I18n.getString("login.error.title")); // ALTERADO
                logger.warning("Falha de login para o usuário: " + username);
            }
        } catch (PersistenciaException ex) {
            logger.log(Level.SEVERE, "Erro de base de dados durante o login do usuário: " + username, ex);
            UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.dbConnection"), I18n.getString("login.error.connectionTitle")); // ALTERADO
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Erro inesperado durante o login do usuário: " + username, ex);
            UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.unexpected"), I18n.getString("login.error.genericTitle")); // ALTERADO
        }
    }
}