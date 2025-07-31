package com.titanaxis.fx.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the {@code LoginView}.
 * Contains only the minimal logic required for demonstration purposes.
 */
public class LoginViewModel {

    private final StringProperty lastUser = new SimpleStringProperty();

    public void login(String user, String password) {
        // TODO: integrar com o serviço de autenticação real
        lastUser.set(user);
    }

    public StringProperty lastUserProperty() {
        return lastUser;
    }
}

