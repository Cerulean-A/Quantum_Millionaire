package qgame;

import javafx.application.Application;
import javafx.stage.Stage;

public class QuantumMillionaire extends Application {

	@Override
	public void start(Stage stage) {
		FrontPage front = new FrontPage(stage);
		stage.setScene(front.getScene());
		stage.setTitle("Quantum Millionaire");
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
