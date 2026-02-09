package qgame;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

/**
 * FrontPage represents the main menu screen of the game.
 * 
 * This class does NOT extend Application. Instead, it is a reusable
 * "screen object" that builds a Scene and returns it to the main app.
 * 
 * The MainApp class creates an instance of FrontPage and places its Scene
 * on the Stage. When the user presses Play, this class switches the Stage
 * to the GameStageScreen.
 */
public class FrontPage {

    /** The Stage (window) shared by all screens */
    private final Stage stage;

    /** The Scene representing this front-page screen */
    private final Scene scene;

    /**
     * Constructor builds the entire front-page UI.
     * 
     * @param stage The primary Stage passed from MainApp.
     */
    public FrontPage(Stage stage) {
        this.stage = stage;

        // Load the intro video from resources
        String path = getClass().getResource("/assets/Millionaire_Mover.mp4").toExternalForm();
        Media media = new Media(path);
        MediaPlayer player = new MediaPlayer(media);

        // Loop the video forever
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setAutoPlay(true);

     // Display the video as the background
        MediaView mediaView = new MediaView(player);
        mediaView.setPreserveRatio(false);
        mediaView.setFitWidth(900);
        mediaView.setFitHeight(900);

        // Create Play button and controls
        HBox controls = new HBox(10, createModeToggleForFrontPage(stage));
        controls.setAlignment(Pos.BOTTOM_CENTER);
        controls.setPadding(new Insets(20));
        controls.setTranslateY(-40);

        // StackPane allows the video to be the background
        StackPane root = new StackPane(mediaView, controls);

        // Build the Scene for this screen
        scene = new Scene(root, 900, 900);

    }
    
    private HBox createModeToggleForFrontPage(Stage stage) {
        HBox toggleBox = new HBox(0);
        toggleBox.setAlignment(Pos.CENTER);

        ToggleButton playBtn = new ToggleButton("Play Mode");
        ToggleButton designBtn = new ToggleButton("Design Mode");

        playBtn.setStyle("-fx-font-size: 18px;");
        designBtn.setStyle("-fx-font-size: 18px;");

        playBtn.getStyleClass().add("toggle-left");
        designBtn.getStyleClass().add("toggle-right");

        ToggleGroup group = new ToggleGroup();
        playBtn.setToggleGroup(group);
        designBtn.setToggleGroup(group);

        // Default selection
        playBtn.setSelected(true);

        // NEW: Navigation instead of updateViewMode()
        playBtn.setOnAction(e -> {
            GameStage game = new GameStage(stage);
            stage.setScene(game.getScene());
            stage.setTitle("Quantum Millionaire - Play Mode");
        });

        designBtn.setOnAction(e -> {
            GameStage game = new GameStage(stage);
            game.showDesignMode();
            stage.setScene(game.getScene());
            stage.setTitle("Quantum Millionaire - Design Mode");
        });


        toggleBox.getChildren().addAll(playBtn, designBtn);
        return toggleBox;
    }


    /**
     * Returns the Scene for this front page.
     * 
     * MainApp will call this and place it on the Stage.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Switches from the front page to the main game screen.
     * 
     * This method creates a GameStageScreen object and replaces the
     * current Scene on the Stage with the game's Scene.
     */
    private void goToGame() {
        GameStage game = new GameStage(stage);
        stage.setScene(game.getScene());
    }
    

}
