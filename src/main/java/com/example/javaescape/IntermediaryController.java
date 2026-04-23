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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

public class IntermediaryController {

    private final List<String> dialogues = List.of(
            "Bien joué. Tu as bien avancé jusqu'ici. Tu as résolu toutes les énigmes, et maintenant, nous avons une meilleure idée de l'endroit où la bombe pourrait être. Mais la tâche n'est pas encore terminée.",
            "Maintenant, il te faut localiser l'emplacement exact. Pour cela, tu vas interroger des suspects. Certains te diront la vérité, d'autres mentiront. Ce sera à toi de discerner qui est fiable et qui ne l'est pas.",
            "Le temps presse. Chaque erreur pourrait nous coûter cher, alors fais attention. Nous n'avons pas de marge pour les hésitations. Trouve où elle se cache, et on pourra passer à l'étape suivante.",
            "Tu es notre seul espoir, et je sais que tu peux le faire. Trouve la bombe, localise-la avec précision. C'est à toi de mener cette mission à bien.",
            "Allez, il ne reste plus beaucoup de temps. Trouve cette bombe, et sauve tout le monde."
    );

    private int currentDialogueIndex = 0;
    private Timeline typewriterTimeline;
    private boolean isAnimating = false;

    @FXML private Label dialogueText;
    @FXML private Label speakerName;
    @FXML private ImageView speakerImage;
    @FXML private Button nextButton;
    @FXML private VBox endPanel;

    @FXML
    private void initialize() {
        speakerImage.setImage(createChefAvatar());
        if (endPanel != null) endPanel.setVisible(false);
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
            finishTypewriterNow();
            return;
        }
        if (currentDialogueIndex < dialogues.size() - 1) {
            currentDialogueIndex++;
            startTypewriter(dialogues.get(currentDialogueIndex));
        } else {
            showEndPanel();
        }
    }

    private void onSpaceKey() {
        if (isAnimating) finishTypewriterNow();
        else onNextDialogue();
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
            nextButton.setText(currentDialogueIndex < dialogues.size() - 1 ? "Continuer" : "Suivant");
        });
        typewriterTimeline.play();
    }

    private void finishTypewriterNow() {
        if (typewriterTimeline != null) typewriterTimeline.stop();
        dialogueText.setText(dialogues.get(currentDialogueIndex));
        isAnimating = false;
        nextButton.setText(currentDialogueIndex < dialogues.size() - 1 ? "Continuer" : "Suivant");
    }

    private void showEndPanel() {
        // Le démineur n'est pas implémenté — afficher un écran de fin de démo
        dialogueText.setVisible(false);
        speakerImage.setVisible(false);
        if (speakerName != null) speakerName.setVisible(false);
        nextButton.setVisible(false);
        if (endPanel != null) endPanel.setVisible(true);
    }

    private WritableImage createChefAvatar() {
        Canvas canvas = new Canvas(160, 160);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#1f2937"));
        gc.fillRoundRect(0, 0, 160, 160, 24, 24);

        gc.setFill(Color.web("#f59e0b"));
        gc.fillOval(58, 34, 44, 44);

        gc.setFill(Color.web("#60a5fa"));
        gc.fillRoundRect(38, 84, 84, 50, 18, 18);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(50, 124, 110, 124);

        return canvas.snapshot(null, null);
    }
}

