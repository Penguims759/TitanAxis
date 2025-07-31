package com.titanaxis.fx.viewmodel;

import com.titanaxis.util.I18n;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * ViewModel backing the {@code DashboardView}.
 */
public class DashboardViewModel {

    private final ReadOnlyStringWrapper welcomeMessage = new ReadOnlyStringWrapper();

    public DashboardViewModel(String username) {
        welcomeMessage.set(I18n.getString("dashboard.welcomeMessage", username));
    }

    public ReadOnlyStringProperty welcomeMessageProperty() {
        return welcomeMessage.getReadOnlyProperty();
    }
}
