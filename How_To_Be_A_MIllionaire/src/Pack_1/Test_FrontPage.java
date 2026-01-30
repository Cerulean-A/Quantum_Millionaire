package Pack_1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class Test_FrontPage extends Application {

    @Override
    public void start(Stage stage) {
        String path = getClass().getResource("/assets/Millionaire_Mover.mp4").toExternalForm();

        Media media = new Media(path);
        MediaPlayer player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE); // loop forever
        player.setAutoPlay(true);

        MediaView mediaView = new MediaView(player);
        mediaView.setPreserveRatio(false); // stretch to fill
        mediaView.setFitWidth(900);
        mediaView.setFitHeight(900);
        //Git Test DELETE
        // Create your UI elements
        Button playBtn = new Button("Play");
        Button pauseBtn = new Button("Pause");

        //playBtn.setOnAction(e -> player.play());
        //pauseBtn.setOnAction(e -> player.pause());

        // Layout for buttons
        HBox controls = new HBox(10, playBtn, pauseBtn);
        controls.setAlignment(Pos.BOTTOM_CENTER);
        controls.setPadding(new Insets(20));
        
        StackPane root = new StackPane();
        root.getChildren().add(mediaView);

        // You can now add buttons, labels, etc. on top of the video
        // root.getChildren().add(menuBox);
        
        root.getChildren().add(controls);

        Scene scene = new Scene(root, 900, 900);
        stage.setScene(scene);        
        stage.setFullScreen(true); // Fullscreen
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

