package com.titanaxis.app;

import atlantafx.base.theme.Dracula;
import com.titanaxis.fx.view.LoginView;
import com.titanaxis.fx.viewmodel.LoginViewModel;
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
        Scene scene = new Scene(new LoginView(viewModel), 320, 240);

        stage.setTitle("TitanAxis");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

