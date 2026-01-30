package Pack_1;
//DELETE
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GameStage extends Application {

    // --- CONFIGURATION ---
    // Adjust these if your screen resolution is different
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    @Override
    public void start(Stage primaryStage) {
        // 1. ROOT NODE: A StackPane allows us to layer the UI *over* the background
        StackPane root = new StackPane();

        // 2. LAYER A: The Background Image (The "Set")
        ImageView backgroundView = new ImageView();
        try {
            // Ensure this file is in your project's resource path or same folder
            Image bgImage = new Image("studio_bg.jpg"); 
            backgroundView.setImage(bgImage);
            backgroundView.setPreserveRatio(false); // Stretch to fill
            backgroundView.setFitWidth(WINDOW_WIDTH);
            backgroundView.setFitHeight(WINDOW_HEIGHT);
        } catch (Exception e) {
            // Fallback dark gradient if image is missing to prevent crash
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #1a0b2e);"); 
        }

        // 3. LAYER B: The Main UI Layout (BorderPane)
        // This organizes the screen into Top, Left, Center, Right, and Bottom zones.
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // --- LEFT: Money Ladder (The "Question Value" list) ---
        VBox moneyLadder = createMoneyLadder();
        mainLayout.setLeft(moneyLadder);

        // --- TOP: Earnings & Timer (Aligned Right) ---
        VBox topRightPanel = createTopRightDashboard();
        // We wrap it in an HBox to force alignment to the right of the top region
        HBox topContainer = new HBox(topRightPanel);
        topContainer.setAlignment(Pos.TOP_RIGHT);
        mainLayout.setTop(topContainer);

        // --- BOTTOM: Lifelines + Question Area + Menu Diamond ---
        // Updated to return a BorderPane to handle the left/center/right split
        BorderPane bottomRegion = createBottomRegion();
        mainLayout.setBottom(bottomRegion);

        // COMBINING: Add layers to root (Background first, then Layout)
        root.getChildren().addAll(backgroundView, mainLayout);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // LINK CSS: This handles the visuals like glowing on hover
        // Ensure 'style.css' is in the same folder as this Java file
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("Quantum Millionaire");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ==============================================================================================
    // REGION BUILDERS
    // ==============================================================================================

    // --- LEFT SIDE: Money Ladder ---
    private VBox createMoneyLadder() {
        VBox ladder = new VBox(2); // Tight spacing like the image
        ladder.setAlignment(Pos.CENTER_LEFT);
        ladder.setPadding(new Insets(10));
        ladder.getStyleClass().add("ladder-container"); // CSS class

        // Header
        Label header = new Label("Question Value");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        header.setPadding(new Insets(0, 0, 5, 0));
        ladder.getChildren().add(header);

        // Values from Top to Bottom
        String[] values = {"$1,000,000", "$500,000", "$300,000", "$200,000", "$10,000", "$5,000", "$2,000", "$1,000", "$500", "$100"};
        
        for (String val : values) {
            Label lbl = new Label(val);
            lbl.setPrefWidth(180);
            lbl.setAlignment(Pos.CENTER);
            lbl.getStyleClass().add("ladder-cell"); 

            // Highlight the $2,000 cell based on your image
            if (val.equals("$2,000")) {
                lbl.setId("current-level"); // Special CSS ID
            }
            ladder.getChildren().add(lbl);
        }
        return ladder;
    }

    // --- TOP RIGHT: Earnings & Timer ---
    private VBox createTopRightDashboard() {
        VBox dashboard = new VBox(10);
        dashboard.setAlignment(Pos.TOP_RIGHT);

        // Earnings Box
        VBox earningsBox = new VBox();
        earningsBox.setAlignment(Pos.CENTER);
        earningsBox.getStyleClass().add("dashboard-box");
        Label earnTitle = new Label("EARNINGS:");
        earnTitle.setTextFill(Color.WHITE);
        Label earnValue = new Label("$1,000");
        earnValue.setStyle("-fx-font-size: 36px; -fx-text-fill: gold; -fx-font-weight: bold;");
        earningsBox.getChildren().addAll(earnTitle, earnValue);

        // Timer Box
        HBox timerBox = new HBox();
        timerBox.setAlignment(Pos.CENTER_RIGHT);
        timerBox.getStyleClass().add("timer-box");
        Label timeLabel = new Label("24 SEC");
        timeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #ffcc00; -fx-font-weight: bold;");
        timerBox.getChildren().add(timeLabel);

        dashboard.getChildren().addAll(earningsBox, timerBox);
        return dashboard;
    }

    // --- BOTTOM REGION: Lifelines + Questions + Menu ---
    private BorderPane createBottomRegion() {
        BorderPane bottomLayout = new BorderPane();
        bottomLayout.setPadding(new Insets(0, 20, 20, 20)); // Padding from edges

        // 1. LEFT: Lifelines Panel
        VBox lifelinesPanel = createLifelinePanel(); 
        bottomLayout.setLeft(lifelinesPanel);

        // 2. CENTER: The Game Board (Question + Answers)
        VBox gameBoard = createGameBoard();
        // We wrap it in a StackPane to ensure it stays perfectly centered
        StackPane centerWrapper = new StackPane(gameBoard);
        bottomLayout.setCenter(centerWrapper);

        // 3. RIGHT: The Watermarked Diamond
        StackPane menuButton = createMenuDiamond();
        // Align it to the bottom right of the container
        BorderPane.setAlignment(menuButton, Pos.BOTTOM_RIGHT);
        bottomLayout.setRight(menuButton);
        
        return bottomLayout;
    }

    // ==============================================================================================
    // COMPONENT HELPERS
    // ==============================================================================================

    private VBox createLifelinePanel() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.BOTTOM_CENTER); // Align to bottom to match image
        box.getStyleClass().add("lifeline-panel");
        
        Label header = new Label("LIFELINES");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        
        HBox icons = new HBox(8);
        icons.setAlignment(Pos.CENTER);
        
        Button l1 = new Button("50:50");
        Button l2 = new Button("📞");
        Button l3 = new Button("👥");
        
        // Assign styles...
        l1.getStyleClass().add("lifeline-btn");
        l2.getStyleClass().add("lifeline-btn");
        l3.getStyleClass().add("lifeline-btn");
        
        icons.getChildren().addAll(l1, l2, l3);
        box.getChildren().addAll(header, icons);
        return box;
    }

    private VBox createGameBoard() {
        VBox board = new VBox(10);
        board.setAlignment(Pos.BOTTOM_CENTER);
        
        Label question = new Label("Which planet is known as the 'Red Planet'?");
        question.getStyleClass().add("question-box");
        question.setWrapText(true);
        question.setAlignment(Pos.CENTER);

        GridPane answers = new GridPane();
        answers.setHgap(15);
        answers.setVgap(10);
        answers.setAlignment(Pos.CENTER);
        
        answers.add(createAnswerBtn("A: Venus"), 0, 0);
        answers.add(createAnswerBtn("B: Mars"), 1, 0);
        answers.add(createAnswerBtn("C: Jupiter"), 0, 1);
        answers.add(createAnswerBtn("D: Saturn"), 1, 1);

        board.getChildren().addAll(question, answers);
        return board;
    }

    private StackPane createMenuDiamond() {
        StackPane diamondContainer = new StackPane();
        diamondContainer.setAlignment(Pos.CENTER);
        
        // The clickable button
        Button menuBtn = new Button();
        menuBtn.getStyleClass().add("menu-diamond"); // CSS defined in style.css
        
        // Optional: An icon inside (like a 'hamburger' menu)
        Label icon = new Label("☰"); 
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        icon.setMouseTransparent(true); // Let clicks pass through to the button

        // Action: Print to console for now
        menuBtn.setOnAction(e -> System.out.println("Open In-Game Menu / Settings"));

        diamondContainer.getChildren().addAll(menuBtn, icon);
        return diamondContainer;
    }

    private Button createAnswerBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("answer-btn");
        btn.setPrefWidth(400); // Wide buttons like the image
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}