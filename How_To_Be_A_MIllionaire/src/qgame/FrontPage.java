package qgame;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * FrontPage represents the main menu screen of the game.
 *
 * <p>This class does NOT extend Application. Instead, it is a reusable
 * "screen object" that builds a Scene and returns it to the main app.</p>
 *
 * <p>The MainApp class creates an instance of FrontPage and places its Scene
 * on the Stage. From here, the user can start the game (Play Mode) or,
 * via a hidden developer toggle, jump directly into Design Mode.</p>
 */
public class FrontPage {

    /** The Stage (window) shared by all screens */
    private final Stage stage;

    /** The game controller, which owns the GameStage lifecycle */
    private final GameBrain_Controller controller;

    /** The Scene representing this front-page screen */
    private final Scene scene;

    /** Background media player (kept as a field so we can stop it when switching screens) */
    private MediaPlayer bgPlayer;

    /** Developer row (Play Mode / Design Mode) that can be toggled via Ctrl+Shift+Q */
    private HBox devRow;

    /**
     * Constructor builds the entire front-page UI.
     *
     * @param stage       The primary Stage passed from MainApp.
     * @param controller  The game controller that will create and manage GameStage_View.
     */
    public FrontPage(Stage stage, GameBrain_Controller controller) {
        this.stage = stage;
        this.controller = controller;

        // Load the intro video from resources
        String path = getClass().getResource("/assets/Millionaire_Mover.mp4").toExternalForm();
        Media media = new Media(path);

        // Use a field so we can stop it when switching screens
        bgPlayer = new MediaPlayer(media);
        bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgPlayer.setAutoPlay(true);

        // Display the video as the background
        MediaView mediaView = new MediaView(bgPlayer);
        mediaView.setPreserveRatio(false);
        mediaView.setFitWidth(900);
        mediaView.setFitHeight(900);

        // Main "Play Game" button
        Button playGameBtn = new Button("Play Game");
        playGameBtn.setStyle("-fx-font-size: 24px; -fx-padding: 10 20;");
        playGameBtn.setOnAction(e -> startGameInPlayMode());

        // Developer row: Play Mode / Design Mode (hidden by default, toggled via Ctrl+Shift+Q)
        devRow = createDevRow();

        VBox controls = new VBox(12);
        controls.setAlignment(Pos.BOTTOM_CENTER);
        controls.setPadding(new Insets(20));
        controls.setTranslateY(-40);
        controls.getChildren().addAll(playGameBtn, devRow);

        // StackPane allows the video to be the background
        StackPane root = new StackPane(mediaView, controls);

        // Build the Scene for this screen
        scene = new Scene(root, 900, 900);

        // Keyboard shortcut: Ctrl + Shift + Q toggles the dev row visibility
        KeyCombination toggleDevRowCombo =
                new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

        scene.setOnKeyPressed(event -> {
            if (toggleDevRowCombo.match(event)) {
                boolean currentlyVisible = devRow.isVisible();
                devRow.setVisible(!currentlyVisible);
                devRow.setManaged(!currentlyVisible);
            }
        });

        // Center the front page once
        centerStageOnce(stage);
    }

    /**
     * Creates the developer row with Play Mode / Design Mode buttons.
     * This row is hidden by default and toggled via Ctrl+Shift+Q.
     */
    private HBox createDevRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);

        Button playModeBtn = new Button("Play Mode");
        playModeBtn.setStyle("-fx-font-size: 16px; -fx-padding: 6 14;");
        playModeBtn.setOnAction(e -> startGameInPlayMode());

        Button designModeBtn = new Button("Design Mode");
        designModeBtn.setStyle("-fx-font-size: 16px; -fx-padding: 6 14;");
        designModeBtn.setOnAction(e -> startGameInDesignMode());

        row.getChildren().addAll(playModeBtn, designModeBtn);

        // Hidden by default; toggled via Ctrl+Shift+Q
        row.setVisible(false);
        row.setManaged(false);

        return row;
    }

    /**
     * Starts the game in Play Mode from the front page.
     */
    private void startGameInPlayMode() {
        stopBackgroundMediaSafely();
        controller.startGameInPlayMode();  // Controller owns the lifecycle
    }

    /**
     * Starts the game directly in Design Mode from the front page (developer path).
     */
    private void startGameInDesignMode() {
        stopBackgroundMediaSafely();
        controller.startGameInDesignMode();  // Controller owns the lifecycle
    }

    /**
     * Safely stops the background media before switching screens.
     */
    private void stopBackgroundMediaSafely() {
        if (bgPlayer != null) {
            try {
                bgPlayer.stop();
            } catch (Exception ignored) {
                // ignore stop errors; continue switching
            }
        }
    }

    /**
     * Centers the provided Stage on the screen that currently contains it.
     */
    private void centerStageOnce(Stage stage) {
        Platform.runLater(() -> {
            Rectangle2D screenBounds = Screen.getScreensForRectangle(
                    stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()
            ).stream().findFirst().orElse(Screen.getPrimary()).getVisualBounds();

            stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2);
        });
    }

    /**
     * Returns the Scene for this front page.
     */
    public Scene getScene() {
        return scene;
    }
}
