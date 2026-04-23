package com.example.javaescape;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.List;

public class HelloController {
    private final List<String> dialogues = List.of(
            "Ecoute-moi bien. Une bombe a ete placee quelque part en ville, et tout repose sur toi.",
            "Nous n'avons pas de temps a perdre. Chaque seconde compte.",
            "Tu vas devoir resoudre une serie d'enigmes. Chacune donnera des indices pour localiser la bombe.",
            "Ne laisse pas la pression te faire trebucher. La ville compte sur toi."
    );

    private int currentDialogueIndex;

    @FXML
    private Label dialogueText;

    @FXML
    private ImageView speakerImage;

    @FXML
    private Button nextButton;

    @FXML
    private void initialize() {
        speakerImage.setImage(createChefAvatar());
        showDialogue();
    }

    @FXML
    protected void onNextDialogue() {
        if (currentDialogueIndex < dialogues.size() - 1) {
            currentDialogueIndex++;
            showDialogue();
            return;
        }

        dialogueText.setText("Fin du briefing. Passe a la premiere enigme.");
        nextButton.setDisable(true);
    }

    private void showDialogue() {
        dialogueText.setText(dialogues.get(currentDialogueIndex));
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
