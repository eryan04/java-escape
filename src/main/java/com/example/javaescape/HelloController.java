package com.example.javaescape;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class HelloController {

    private final List<String> dialogues = List.of(
            "Écoute-moi bien. Une bombe a été placée quelque part en ville, et tout repose sur toi.\nNous n'avons pas de temps à perdre. Chaque seconde compte.",
            "Voici la situation : tu vas devoir résoudre une série d'énigmes. Chacune te donnera des indices pour localiser la bombe. Le temps presse, mais nous avons encore une chance si tu agis rapidement et avec précision.",
            "Je sais que ce n'est pas facile, mais je crois en toi. Nous avons les outils nécessaires, et tu as l'intelligence pour déchiffrer ces énigmes. Chaque réponse correcte nous rapproche de la solution.",
            "Ne laisse pas la pression te faire trébucher. Résous les énigmes, trouve l'emplacement de la bombe, et nous pourrons la désamorcer avant qu'il ne soit trop tard. On compte sur toi. La ville compte sur toi."
    );

    private int currentDialogueIndex = 0;
    private Timeline typewriterTimeline;
    private boolean isAnimating = false;

    @FXML private Label dialogueText;
    @FXML private Label speakerName;
    @FXML private ImageView speakerImage;
    @FXML private Button nextButton;

    @FXML
    private void initialize() {
        speakerImage.setImage(createChefAvatar());
        // Écoute la touche ESPACE sur la scène dès qu'elle est disponible
        dialogueText.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.SPACE) onSpaceKey();
                });
            }
        });
        startTypewriter(dialogues.get(0));
    }

    @FXML
    protected void onNextDialogue() {
        if (isAnimating) {
            // Premier appui : compléter l'animation instantanément
            finishTypewriterNow();
            return;
        }
        if (currentDialogueIndex < dialogues.size() - 1) {
            currentDialogueIndex++;
            startTypewriter(dialogues.get(currentDialogueIndex));
            nextButton.setText("Continuer");
        } else {
            // Dernier dialogue passé : lancer le quiz
            launchQuiz();
        }
    }

    private void onSpaceKey() {
        if (isAnimating) {
            finishTypewriterNow();
        } else {
            onNextDialogue();
        }
    }

    private void startTypewriter(String text) {
        if (typewriterTimeline != null) typewriterTimeline.stop();
        dialogueText.setText("");
        isAnimating = true;
        nextButton.setText("Passer");

        typewriterTimeline = new Timeline();
        for (int i = 1; i <= text.length(); i++) {
            final String sub = text.substring(0, i);
            typewriterTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 30L), e -> dialogueText.setText(sub))
            );
        }
        typewriterTimeline.setOnFinished(e -> {
            isAnimating = false;
            nextButton.setText(currentDialogueIndex < dialogues.size() - 1 ? "Continuer" : "Lancer le quiz");
        });
        typewriterTimeline.play();
    }

    private void finishTypewriterNow() {
        if (typewriterTimeline != null) typewriterTimeline.stop();
        dialogueText.setText(dialogues.get(currentDialogueIndex));
        isAnimating = false;
        nextButton.setText(currentDialogueIndex < dialogues.size() - 1 ? "Continuer" : "Lancer le quiz");
    }

    private void launchQuiz() {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        QuizApp quiz = new QuizApp();
        quiz.launchQuiz(stage);
    }


    private WritableImage createChefAvatar() {
        Canvas canvas = new Canvas(180, 180);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#1f2937"));
        gc.fillRoundRect(0, 0, 180, 180, 24, 24);

        gc.setFill(Color.web("#f59e0b"));
        gc.fillOval(68, 42, 44, 44);

        gc.setFill(Color.web("#60a5fa"));
        gc.fillRoundRect(48, 92, 84, 52, 18, 18);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(60, 134, 120, 134);

        return canvas.snapshot(null, null);
    }
}
