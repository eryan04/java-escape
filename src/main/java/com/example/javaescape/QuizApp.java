package com.example.javaescape;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class QuizApp extends Application {

    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswersCount = 0;

    private Label questionLabel = new Label("Chargement...");
    private Label scoreLabel = new Label("Score: 0/5");
    private Label feedbackLabel = new Label("");
    private VBox answersContainer = new VBox(10);
    
    private Stage stage;

    public QuizApp() {}
    
    public QuizApp(Stage stage) {
        this.stage = stage;
    }

    public void launchQuiz(Stage primaryStage) {
        this.stage = primaryStage;
        VBox root = new VBox(20, scoreLabel, questionLabel, answersContainer, feedbackLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(20));

        Scene scene = new Scene(root, 560, 480);
        primaryStage.setTitle("Quizz JavaFX - Objectif 5 !");
        primaryStage.setScene(scene);
        primaryStage.show();

        fetchQuestions();
    }

    @Override
    public void start(Stage primaryStage) {
        launchQuiz(primaryStage);
    }

    private void fetchQuestions() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://opentdb.com/api.php?amount=50&type=multiple"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP " + response.statusCode());
                    }
                    return response.body();
                })
                .thenAccept(this::parseJson)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        questionLabel.setText("Erreur réseau: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void parseJson(String responseBody) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        JsonArray results = jsonObject.getAsJsonArray("results");

        questions = gson.fromJson(results, new TypeToken<List<Question>>(){}.getType());
        questions.forEach(Question::shuffleAnswers);

        Platform.runLater(this::displayNextQuestion);
    }

    private void displayNextQuestion() {
        if (correctAnswersCount >= 5) {
            questionLabel.setText("Félicitations ! Vous avez atteint 5 bonnes réponses.");
            answersContainer.getChildren().clear();
            return;
        }

        if (currentQuestionIndex >= questions.size()) {
            fetchQuestions();
            return;
        }

        Question q = questions.get(currentQuestionIndex);
        questionLabel.setText(q.getDecodedQuestion());

        answersContainer.getChildren().clear();
        for (String answer : q.getDecodedAnswers()) {
            Button btn = new Button(answer);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> handleAnswer(answer, q.getDecodedCorrectAnswer()));
            answersContainer.getChildren().add(btn);
        }
    }

    private void handleAnswer(String selected, String correct) {
        if (selected.equals(correct)) {
            correctAnswersCount++;
            feedbackLabel.setText("Correct ! Bravo.");
            feedbackLabel.setStyle("-fx-text-fill: green;");
        } else {
            feedbackLabel.setText("Incorrect ! La bonne réponse était : " + correct);
            feedbackLabel.setStyle("-fx-text-fill: red;");
        }

        scoreLabel.setText("Score: " + correctAnswersCount + "/5");
        currentQuestionIndex++;

        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ex) {}
            Platform.runLater(this::displayNextQuestion);
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
