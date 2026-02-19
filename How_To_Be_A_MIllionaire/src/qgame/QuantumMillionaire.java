package qgame;

import javafx.application.Application;
import javafx.stage.Stage;

public class QuantumMillionaire extends Application {

    @Override
    public void start(Stage stage) {

        // Create the controller FIRST, giving it the Stage
        GameBrain_Controller controller = new GameBrain_Controller(stage);

        // Create the FrontPage, giving it both the Stage and the Controller
        FrontPage front = new FrontPage(stage, controller);

        stage.setScene(front.getScene());
        stage.setTitle("Quantum Millionaire");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
