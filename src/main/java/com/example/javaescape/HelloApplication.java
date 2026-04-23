package com.example.javaescape;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("title-view-fr.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 480);
        applyGlobalStyles(scene);
        stage.setTitle("Escape");
        stage.setScene(scene);
        stage.show();
    }

    public static void applyGlobalStyles(Scene scene) {
        var css = HelloApplication.class.getResource("app.css");
        if (css != null && !scene.getStylesheets().contains(css.toExternalForm())) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
