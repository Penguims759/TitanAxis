package com.titanaxis.view;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import org.slf4j.Logger;

public class LoginFrame extends JFrame {
    private final AppContext appContext;
    private final AuthService authService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private static final Logger logger = AppLogger.getLogger();

    public LoginFrame(AppContext appContext) {
        super(I18n.getString("login.title"));
        this.appContext = appContext;
        this.authService = appContext.getAuthService();

        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(I18n.getString("login.welcomeMessage"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel(I18n.getString("login.usernameLabel"));
        usernameField = new JTextField(20);
        JLabel passLabel = new JLabel(I18n.getString("login.passwordLabel"));
        passwordField = new JPasswordField(20);

        passwordField.addActionListener(e -> performLogin());

        inputPanel.add(userLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passLabel);
        inputPanel.add(passwordField);

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(20));

        JButton loginButton = new JButton(I18n.getString("login.loginButton"));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        panel.add(loginButton);
        add(panel);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.emptyFields"), I18n.getString("error.title"));
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = authService.login(username, password);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                logger.info("Usuário " + usuario.getNomeUsuario() + " logou com sucesso.");
                UIMessageUtil.showInfoMessage(this, I18n.getString("login.success.message", usuario.getNomeUsuario()), I18n.getString("success.title"));

                DashboardFrame dashboard = new DashboardFrame(appContext);
                dashboard.setVisible(true);
                this.dispose();
            } else {
                UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.invalidCredentials"), I18n.getString("error.title"));
                logger.warning("Falha de login para o usuário: " + username);
            }
        } catch (PersistenciaException ex) {
            logger.error("Erro de base de dados durante o login do usuário: " + username, ex);
            UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.dbConnection"), I18n.getString("login.error.connectionTitle"));
        } catch (Exception ex) {
            logger.error("Erro inesperado durante o login do usuário: " + username, ex);
            UIMessageUtil.showErrorMessage(this, I18n.getString("login.error.unexpected"), I18n.getString("error.title"));
        }
    }
}