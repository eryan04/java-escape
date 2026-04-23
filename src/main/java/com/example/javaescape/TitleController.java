package com.example.javaescape;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class TitleController {
    @FXML
    private Button startButton;

    @FXML
    private void onStart() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("hello-view-fr.fxml"));
        Scene dialogueScene = new Scene(loader.load(), 560, 480);
        HelloApplication.applyGlobalStyles(dialogueScene);
        Stage stage = (Stage) startButton.getScene().getWindow();
        stage.setTitle("Java Escape - Briefing");
        stage.setScene(dialogueScene);
    }
}
