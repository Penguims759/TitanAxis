package com.titanaxis.fx.view;

import com.titanaxis.fx.viewmodel.DashboardViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Basic placeholder dashboard view implemented with JavaFX.
 */
public class DashboardView extends BorderPane {

    public DashboardView(DashboardViewModel viewModel) {
        setPadding(new Insets(20));

        Label welcome = new Label();
        welcome.textProperty().bind(viewModel.welcomeMessageProperty());

        setCenter(welcome);
    }
}
