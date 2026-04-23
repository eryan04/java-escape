package com.example.javaescape;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
public class QuizApp extends Application {
    private List<Question> questions     = new ArrayList<>();
    private int  currentIndex            = 0;
    private int  correctCount            = 0;
    private boolean answerLocked         = false;
    private volatile boolean gameWon     = false;
    private Stage primaryStage;
    private final Map<Integer, TranslatedQuestion> cache = new ConcurrentHashMap<>();
    private final HttpClient      httpClient = HttpClient.newHttpClient();
    private final ExecutorService executor   = Executors.newCachedThreadPool();
    private final Label questionLabel    = new Label("Chargement...");
    private final Label scoreLabel       = new Label("Score : 0 / 5");
    private final Label feedbackLabel    = new Label("");
    private final Label categoryLabel    = new Label("");
    private final VBox  answersContainer = new VBox(10);
    public QuizApp() {}
    public void launchQuiz(Stage stage) {
        this.primaryStage = stage;
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(480);
        questionLabel.getStyleClass().add("question-label");
        scoreLabel.getStyleClass().add("score-label");
        feedbackLabel.getStyleClass().add("feedback-label");
        categoryLabel.getStyleClass().add("hint-label");
        answersContainer.getStyleClass().add("answers-container");
        answersContainer.setFillWidth(true);
        Label quizTitle = new Label("Quiz - Desamorcage");
        quizTitle.getStyleClass().add("screen-title");
        VBox card = new VBox(14, quizTitle, scoreLabel, categoryLabel, questionLabel, answersContainer, feedbackLabel);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(540);
        card.getStyleClass().addAll("panel", "quiz-panel");
        StackPane root = new StackPane(card);
        root.getStyleClass().add("app-root");
        Scene scene = new Scene(root, 620, 540);
        HelloApplication.applyGlobalStyles(scene);
        stage.setTitle("Java Escape - Quiz");
        stage.setScene(scene);
        stage.show();
        fetchQuestions();
    }
    @Override
    public void start(Stage stage) { launchQuiz(stage); }
    private void fetchQuestions() {
        questionLabel.setText("Chargement des questions...");
        answersContainer.getChildren().clear();
        answerLocked = false;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://opentdb.com/api.php?amount=50&type=multiple"))
                .GET().build();
        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    Gson gson = new Gson();
                    JsonObject obj = gson.fromJson(body, JsonObject.class);
                    JsonArray results = obj.getAsJsonArray("results");
                    List<Question> list = gson.fromJson(results, new TypeToken<List<Question>>(){}.getType());
                    list.forEach(Question::shuffleAnswers);
                    questions = list;
                    currentIndex = 0;
                    cache.clear();
                    Platform.runLater(this::showCurrentQuestion);
                    preFetch(0); preFetch(1); preFetch(2);
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> questionLabel.setText("Erreur reseau : " + ex.getMessage()));
                    return null;
                });
    }
    private void showCurrentQuestion() {
        if (gameWon) return;
        feedbackLabel.getStyleClass().removeAll("feedback-success", "feedback-error");
        feedbackLabel.setText("");
        answerLocked = false;
        if (correctCount >= 5) {
            gameWon = true;
            executor.shutdownNow();
            categoryLabel.setText("");
            questionLabel.setText("Mission accomplie ! Transition en cours...");
            scoreLabel.setText("Score : " + correctCount + " / 5");
            answersContainer.getChildren().clear();
            feedbackLabel.setText("");
            PauseTransition p = new PauseTransition(Duration.seconds(1.5));
            p.setOnFinished(e -> launchIntermediaryDialogue());
            p.play();
            return;
        }
        if (currentIndex >= questions.size()) { fetchQuestions(); return; }
        if (cache.containsKey(currentIndex)) {
            render(cache.get(currentIndex));
            preFetch(currentIndex + 1);
            return;
        }
        questionLabel.setText("Traduction en cours...");
        answersContainer.getChildren().clear();
        final int idx = currentIndex;
        executor.submit(() -> {
            TranslatedQuestion tq = translate(questions.get(idx));
            cache.put(idx, tq);
            Platform.runLater(() -> { if (!gameWon && currentIndex == idx) render(tq); });
        });
    }
    private void render(TranslatedQuestion tq) {
        if (gameWon) return;
        categoryLabel.setText("Categorie : " + tq.category);
        questionLabel.setText(tq.question);
        answersContainer.getChildren().clear();
        for (String ans : tq.answers) {
            Button btn = new Button(ans);
            btn.getStyleClass().add("answer-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> handleAnswer(btn, ans, tq.correct));
            answersContainer.getChildren().add(btn);
        }
        preFetch(currentIndex + 1);
    }
    private void preFetch(int idx) {
        if (gameWon || idx < 0 || idx >= questions.size() || cache.containsKey(idx)) return;
        executor.submit(() -> {
            if (gameWon) return;
            TranslatedQuestion tq = translate(questions.get(idx));
            cache.put(idx, tq);
        });
    }
    private TranslatedQuestion translate(Question q) {
        TranslatedQuestion tq = new TranslatedQuestion();
        tq.category = translateText(q.category);
        tq.question  = translateText(q.getDecodedQuestion());
        String origCorrect = q.getDecodedCorrectAnswer();
        List<String> translatedAnswers = new ArrayList<>();
        String translatedCorrect = null;
        for (String a : q.getDecodedAnswers()) {
            String t = translateText(a);
            translatedAnswers.add(t);
            if (a.equals(origCorrect)) translatedCorrect = t;
        }
        tq.answers = translatedAnswers;
        tq.correct = (translatedCorrect != null) ? translatedCorrect : translateText(origCorrect);
        return tq;
    }
    private String translateText(String text) {
        if (text == null || text.isBlank()) return text;
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=fr&dt=t&q=" + encoded;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET().build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JsonArray root = new Gson().fromJson(res.body(), JsonArray.class);
            StringBuilder sb = new StringBuilder();
            JsonArray segments = root.get(0).getAsJsonArray();
            for (int i = 0; i < segments.size(); i++) {
                sb.append(segments.get(i).getAsJsonArray().get(0).getAsString());
            }
            String result = sb.toString().trim();
            return result.isEmpty() ? text : result;
        } catch (Exception e) {
            return text;
        }
    }
    private void handleAnswer(Button clicked, String selected, String correct) {
        if (answerLocked || gameWon) return;
        answerLocked = true;
        if (selected.equals(correct)) {
            correctCount++;
            feedbackLabel.getStyleClass().add("feedback-success");
            feedbackLabel.setText("Correct !");
            clicked.setStyle("-fx-background-color: #1e7a4a; -fx-text-fill: white;");
        } else {
            feedbackLabel.getStyleClass().add("feedback-error");
            feedbackLabel.setText("Incorrect. Reponse : " + correct);
            clicked.setStyle("-fx-background-color: #7a1e1e; -fx-text-fill: white;");
        }
        scoreLabel.setText("Score : " + correctCount + " / 5");
        currentIndex++;
        answersContainer.getChildren().forEach(n -> n.setDisable(true));
        PauseTransition p = new PauseTransition(Duration.seconds(1.8));
        p.setOnFinished(e -> showCurrentQuestion());
        p.play();
    }
    // ─── Dialogue intermediaire ───────────────────────────────────────────────
    private void launchIntermediaryDialogue() {
        List<String> dialogues = Arrays.asList(
            "Bien joue. Tu as bien avance jusqu ici. Tu as resolu toutes les enigmes, et maintenant, nous avons une meilleure idee de l endroit ou la bombe pourrait etre. Mais la tache n est pas encore terminee.",
            "Maintenant, il te faut localiser l emplacement exact. Pour cela, tu vas interroger des suspects. Certains te diront la verite, d autres mentiront. Ce sera a toi de discerner qui est fiable et qui ne l est pas.",
            "Le temps presse. Chaque erreur pourrait nous couter cher, alors fais attention. Nous n avons pas de marge pour les hesitations. Trouve ou elle se cache, et on pourra passer a l etape suivante.",
            "Tu es notre seul espoir, et je sais que tu peux le faire. Trouve la bombe, localise-la avec precision. C est a toi de mener cette mission a bien.",
            "Allez, il ne reste plus beaucoup de temps. Trouve cette bombe, et sauve tout le monde."
        );
        buildDialogueScene("Rapport du Chef", dialogues, () -> showVictoryScreen());
    }
    // ─── Ecran Victoire ───────────────────────────────────────────────────────
    private void showVictoryScreen() {
        List<String> dialogues = Arrays.asList(
            "Tu l as fait... Tu as reussi a desamorcer la bombe et a sauver la ville. Je savais que tu en etais capable.",
            "Grace a toi, des vies ont ete sauvees aujourd hui. Tu as fait preuve de courage, d intelligence et de determination dans chaque etape de cette mission. Tu as tout donne, et ca a paye.",
            "Bien joue, vraiment. Tu as prouve qu il n y a rien que tu ne puisses accomplir. Je n oublierai jamais ce jour."
        );
        buildDialogueScene("BOMBE DESAMORCEE", dialogues, null);
    }
    // ─── Ecran Defaite ────────────────────────────────────────────────────────
    public void showDefeatScreen() {
        List<String> messages = Arrays.asList(
            "L echec est total. La bombe a explose.",
            "Le Chef, qui avait place toute sa confiance en toi, est mort dans l explosion.",
            "La ville a ete detruite. Des vies ont ete perdues. Tout est fini.",
            "On est tous tres decus !"
        );
        Label title = new Label("GAME OVER");
        title.getStyleClass().add("screen-title");
        title.setStyle("-fx-text-fill: #ff4444;");
        ImageView img = new ImageView(buildExplosionImage());
        img.setFitWidth(180); img.setFitHeight(180);
        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(460);
        msgLabel.getStyleClass().add("dialogue-label");
        msgLabel.setStyle("-fx-text-fill: #ff8f94;");
        Button nextBtn = new Button("Continuer");
        nextBtn.getStyleClass().add("primary-button");
        nextBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #8b0000, #5c0000);");
        VBox card = new VBox(18, title, img, msgLabel, nextBtn);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(520);
        card.setPadding(new Insets(30));
        card.getStyleClass().add("panel");
        card.setStyle("-fx-border-color: rgba(255,68,68,0.5);");
        StackPane root = new StackPane(card);
        root.getStyleClass().add("app-root");
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a0000, #2d0000);");
        Scene scene = new Scene(root, 620, 540);
        HelloApplication.applyGlobalStyles(scene);
        int[] idx = {0};
        Runnable show = new Runnable() {
            @Override public void run() {
                if (idx[0] >= messages.size()) {
                    nextBtn.setText("Fermer");
                    nextBtn.setOnAction(e -> primaryStage.close());
                    return;
                }
                msgLabel.setText(messages.get(idx[0]));
                nextBtn.setText(idx[0] < messages.size() - 1 ? "Suivant" : "Fermer");
            }
        };
        nextBtn.setOnAction(e -> { idx[0]++; show.run(); });
        scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.SPACE) { idx[0]++; show.run(); } });
        primaryStage.setTitle("Java Escape - Fin");
        primaryStage.setScene(scene);
        primaryStage.show();
        show.run();
    }
    // ─── Constructeur de scene dialogue generique ─────────────────────────────
    private void buildDialogueScene(String titleText, List<String> dialogues, Runnable onFinish) {
        Label title = new Label(titleText);
        title.getStyleClass().add("screen-title");
        Label speakerLabel = new Label("Chef");
        speakerLabel.getStyleClass().add("speaker-name");
        ImageView img = new ImageView(
            titleText.contains("DESAMORCEE") ? buildVictoryImage() : buildChefImage()
        );
        img.setFitWidth(150); img.setFitHeight(150);
        Label dialogueLabel = new Label();
        dialogueLabel.setWrapText(true);
        dialogueLabel.setMaxWidth(460);
        dialogueLabel.getStyleClass().add("dialogue-label");
        Label hintLabel = new Label("[ESPACE] pour continuer");
        hintLabel.getStyleClass().add("hint-label");
        Button nextBtn = new Button("Passer");
        nextBtn.getStyleClass().add("primary-button");
        VBox card = new VBox(14, title, img, speakerLabel, dialogueLabel, hintLabel, nextBtn);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(520);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("panel");
        StackPane root = new StackPane(card);
        root.getStyleClass().add("app-root");
        Scene scene = new Scene(root, 620, 580);
        HelloApplication.applyGlobalStyles(scene);
        // Logique typewriter
        int[] idx = {0};
        Timeline[] tl = {null};
        boolean[] animating = {false};
        Runnable showDialogue = new Runnable() {
            @Override public void run() {
                if (idx[0] >= dialogues.size()) {
                    if (onFinish != null) {
                        onFinish.run();
                    } else {
                        // Fin victoire : desactiver le bouton
                        nextBtn.setDisable(true);
                        nextBtn.setText("Fin");
                    }
                    return;
                }
                String text = dialogues.get(idx[0]);
                dialogueLabel.setText("");
                animating[0] = true;
                nextBtn.setText("Passer");
                if (tl[0] != null) tl[0].stop();
                tl[0] = new Timeline();
                for (int i = 1; i <= text.length(); i++) {
                    final String sub = text.substring(0, i);
                    tl[0].getKeyFrames().add(new KeyFrame(Duration.millis(i * 28L), ev -> dialogueLabel.setText(sub)));
                }
                tl[0].setOnFinished(ev -> {
                    animating[0] = false;
                    nextBtn.setText(idx[0] < dialogues.size() - 1 ? "Continuer" : (onFinish != null ? "Suite" : "Terminer"));
                });
                tl[0].play();
            }
        };
        Runnable onAction = () -> {
            if (animating[0]) {
                if (tl[0] != null) tl[0].stop();
                dialogueLabel.setText(dialogues.get(idx[0]));
                animating[0] = false;
                nextBtn.setText(idx[0] < dialogues.size() - 1 ? "Continuer" : (onFinish != null ? "Suite" : "Terminer"));
            } else {
                idx[0]++;
                showDialogue.run();
            }
        };
        nextBtn.setOnAction(e -> onAction.run());
        scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.SPACE) onAction.run(); });
        primaryStage.setTitle("Java Escape - " + titleText);
        primaryStage.setScene(scene);
        primaryStage.show();
        showDialogue.run();
    }
    // ─── Images generees par Canvas ───────────────────────────────────────────
    private WritableImage buildVictoryImage() {
        Canvas c = new Canvas(150, 150);
        GraphicsContext gc = c.getGraphicsContext2D();
        // Fond vert fonce
        gc.setFill(Color.web("#0a1f0a"));
        gc.fillRoundRect(0, 0, 150, 150, 20, 20);
        // Halo vert
        RadialGradient glow = new RadialGradient(0, 0, 75, 75, 65, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#22c55e", 0.5)), new Stop(1, Color.TRANSPARENT));
        gc.setFill(glow);
        gc.fillOval(10, 10, 130, 130);
        // Bombe
        gc.setFill(Color.web("#374151"));
        gc.fillOval(40, 50, 60, 55);
        gc.setFill(Color.web("#1f2937"));
        gc.fillRoundRect(90, 44, 18, 8, 4, 4);
        // Checkmark vert
        gc.setStroke(Color.web("#22c55e"));
        gc.setLineWidth(6);
        gc.strokeLine(50, 80, 65, 95);
        gc.strokeLine(65, 95, 100, 58);
        // Texte
        gc.setFill(Color.web("#22c55e"));
        gc.setFont(javafx.scene.text.Font.font("System Bold", 11));
        gc.fillText("DESAMORCEE", 18, 130);
        return c.snapshot(null, null);
    }
    private WritableImage buildExplosionImage() {
        Canvas c = new Canvas(150, 150);
        GraphicsContext gc = c.getGraphicsContext2D();
        gc.setFill(Color.web("#1a0000"));
        gc.fillRoundRect(0, 0, 150, 150, 20, 20);
        RadialGradient exp = new RadialGradient(0, 0, 75, 75, 60, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#ff4500", 0.9)), new Stop(0.5, Color.web("#ff8c00", 0.6)), new Stop(1, Color.TRANSPARENT));
        gc.setFill(exp);
        gc.fillOval(15, 15, 120, 120);
        // Rayons explosion
        gc.setStroke(Color.web("#ffcc00", 0.8));
        gc.setLineWidth(3);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            gc.strokeLine(75 + 30 * Math.cos(angle), 75 + 30 * Math.sin(angle),
                          75 + 65 * Math.cos(angle), 75 + 65 * Math.sin(angle));
        }
        gc.setFill(Color.web("#ffcc00"));
        gc.setFont(javafx.scene.text.Font.font("System Bold", 14));
        gc.fillText("BOOM !", 42, 82);
        return c.snapshot(null, null);
    }
    private WritableImage buildChefImage() {
        Canvas c = new Canvas(150, 150);
        GraphicsContext gc = c.getGraphicsContext2D();
        gc.setFill(Color.web("#1f2937"));
        gc.fillRoundRect(0, 0, 150, 150, 20, 20);
        gc.setFill(Color.web("#f59e0b"));
        gc.fillOval(53, 28, 44, 44);
        gc.setFill(Color.web("#60a5fa"));
        gc.fillRoundRect(33, 78, 84, 46, 14, 14);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(45, 114, 105, 114);
        return c.snapshot(null, null);
    }
    private static class TranslatedQuestion {
        String category, question, correct;
        List<String> answers;
    }
    public static void main(String[] args) { launch(args); }
}