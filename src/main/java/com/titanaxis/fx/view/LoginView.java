package com.titanaxis.fx.view;

import com.titanaxis.fx.viewmodel.LoginViewModel;
import com.titanaxis.util.I18n;
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
        userField.textProperty().bindBidirectional(viewModel.usernameProperty());

        PasswordField passField = new PasswordField();
        passField.textProperty().bindBidirectional(viewModel.passwordProperty());

        Button loginButton = new Button(I18n.getString("login.loginButton"));

        loginButton.setOnAction(e -> viewModel.login());

        getChildren().addAll(
            new Label(I18n.getString("login.usernameLabel")), userField,
            new Label(I18n.getString("login.passwordLabel")), passField,
            loginButton
        );
    }
}

