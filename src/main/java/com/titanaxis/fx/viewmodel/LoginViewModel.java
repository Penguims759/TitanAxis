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

    /**
     * Simulates the login process by recording the current username as the
     * last successful user.
     */
    public void login() {
        lastUser.set(username.get());
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
}

