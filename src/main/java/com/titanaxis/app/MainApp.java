package com.titanaxis.app;

import atlantafx.base.theme.Dracula;
import com.titanaxis.fx.view.DashboardView;
import com.titanaxis.fx.view.LoginView;
import com.titanaxis.fx.viewmodel.DashboardViewModel;
import com.titanaxis.fx.viewmodel.LoginViewModel;
import com.titanaxis.util.I18n;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the JavaFX-based TitanAxis application.
 * The view layer follows a simple MVVM structure where the
 * {@link LoginView} interacts with a {@link LoginViewModel}.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        LoginViewModel viewModel = new LoginViewModel();
        viewModel.setOnLoginSuccess(() -> {
            DashboardViewModel dashboardVM = new DashboardViewModel(viewModel.usernameProperty().get());
            Scene dashboardScene = new Scene(new DashboardView(dashboardVM), 800, 600);
            stage.setScene(dashboardScene);
            stage.setTitle(I18n.getString("dashboard.title"));
        });

        Scene scene = new Scene(new LoginView(viewModel), 320, 240);

        stage.setTitle(I18n.getString("login.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

