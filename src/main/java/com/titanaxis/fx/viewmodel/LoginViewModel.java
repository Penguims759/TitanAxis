package com.titanaxis.fx.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the {@code LoginView}.
 * Contains only the minimal logic required for demonstration purposes.
 */
public class LoginViewModel {

    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty lastUser = new SimpleStringProperty();
    private Runnable onLoginSuccess;

    /**
     * Trigger a login attempt using the current username and password
     * values. Real authentication will be integrated later.
     */
    public void login() {
        // TODO: integrar com o serviço de autenticação real
        lastUser.set(username.get());
        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty lastUserProperty() {
        return lastUser;
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }
}

