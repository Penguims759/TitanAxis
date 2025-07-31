package com.titanaxis.fx.view;

import com.titanaxis.fx.viewmodel.LoginViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Simple login view demonstrating MVVM principles.
 */
public class LoginView extends VBox {

    public LoginView(LoginViewModel viewModel) {
        setSpacing(10);
        setPadding(new Insets(20));

        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");

        loginButton.setOnAction(e -> viewModel.login(userField.getText(), passField.getText()));

        getChildren().addAll(
            new Label("Usuário"), userField,
            new Label("Senha"), passField,
            loginButton
        );
    }
}

