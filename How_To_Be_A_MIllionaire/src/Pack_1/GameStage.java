package Pack_1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GameStage extends Application {

    // =============================================================================================
    // 1. THE "ATOMIC" DESIGN GRID
    // =============================================================================================
    // We will place all buttons/text based on this 1920x1080 "Virtual Paper".
    // Do not change these numbers. They are the fixed coordinate system.
    private static final double DESIGN_WIDTH = 1920; 
    private static final double DESIGN_HEIGHT = 1080;

    // =============================================================================================
    // 2. USER RESOLUTION OPTIONS
    // =============================================================================================
    // These are the physical window sizes the user can toggle between.
    
    // Option A: 720p (Windowed, small)
    private static final double RES_720P_WIDTH = 1280;
    private static final double RES_720P_HEIGHT = 720;

    // Option B: 1080p (Standard Full HD)
    private static final double RES_1080P_WIDTH = 1920; 
    private static final double RES_1080P_HEIGHT = 1080;
    
    // Option C: 4K (Ultra HD)
    private static final double RES_4K_WIDTH = 3840; 
    private static final double RES_4K_HEIGHT = 2160;

    // --- DEVELOPER SWITCH ---
    // Change this variable to test different "User Selections"
    // Set to: 1=720p, 2=1080p, 3=4K
    private static final int SELECTED_RESOLUTION_OPTION = 3; 

    @Override
    public void start(Stage primaryStage) {
        
        // --- STEP 1: DETERMINE TARGET WINDOW SIZE ---
        double targetWidth, targetHeight;

        switch (SELECTED_RESOLUTION_OPTION) {
            case 1:  targetWidth = RES_720P_WIDTH; targetHeight = RES_720P_HEIGHT; break;
            case 3:  targetWidth = RES_4K_WIDTH;   targetHeight = RES_4K_HEIGHT;   break;
            default: targetWidth = RES_1080P_WIDTH; targetHeight = RES_1080P_HEIGHT;
        }

        // --- STEP 2: SAFETY CLAMP (The "Fit to Screen" Logic) ---
        // If the user asks for 4K but the screen is only 1080p, we must shrink the window.
        // This is a universal guard-rail, however. It a screen only supports 480p, it will size to it.
        // to fit the screen, otherwise controls will be unreachable.
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Check if target is wider than the physical screen
        if (targetWidth > screenBounds.getWidth() || targetHeight > screenBounds.getHeight()) {
            System.out.println("(!) Requested resolution " + targetWidth + "x" + targetHeight + " is too large for this screen.");
            
            // Calculate the max scale that fits the screen
            double widthRatio = screenBounds.getWidth() / targetWidth;
            double heightRatio = screenBounds.getHeight() / targetHeight;
            double safeScale = Math.min(widthRatio, heightRatio);
            
            // Apply the safe scale (e.g., shrink 4K down to fit on a 1080p laptop)
            targetWidth = targetWidth * safeScale;
            targetHeight = targetHeight * safeScale;
            
            System.out.println(" -> Resized window to " + targetWidth + "x" + targetHeight);
        }

        // --- STEP 3: SETUP ROOT & BACKGROUND ---
        StackPane root = new StackPane();
        // Determine centering behavior (Black bars if aspect ratio mismatches)
        root.setStyle("-fx-background-color: black;"); 

        ImageView backgroundView = new ImageView();
        try {
            Image bgImage = new Image("studio_bg.jpg"); 
            backgroundView.setImage(bgImage);
            backgroundView.setPreserveRatio(true); // Keep art aspect ratio correct
            backgroundView.setFitWidth(targetWidth);
            backgroundView.setFitHeight(targetHeight);
        } catch (Exception e) {
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #1a0b2e);"); 
        }

        // --- STEP 4: THE UI LAYER (The Scalable Canvas) ---
        // This Pane is ALWAYS 1920x1080 internally.
        Pane uiLayer = new Pane();
        uiLayer.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        uiLayer.setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);

        // --- STEP 5: CALCULATE SCALE FACTOR ---
        // How much do we shrink/grow the 1920 design to fit the actual window?
        // Example: If Window is 1280 (720p) and Design is 1920. Scale = 0.66
        double scaleFactor = targetWidth / DESIGN_WIDTH;
        
        Scale scale = new Scale(scaleFactor, scaleFactor);
        scale.setPivotX(0);
        scale.setPivotY(0);
        uiLayer.getTransforms().add(scale);

        // --- STEP 6: PLACE ATOMIC ASSETS (Based on 1920x1080 Grid) ---
        
        // Left: Money Ladder
        VBox moneyLadder = createMoneyLadder();
        moneyLadder.relocate(30, 75); 
        uiLayer.getChildren().add(moneyLadder);

        // Top Right: Dashboard
        VBox topRightPanel = createTopRightDashboard();
        topRightPanel.relocate(1500, 30);
        uiLayer.getChildren().add(topRightPanel);

        // Bottom Left: Lifelines
        VBox lifelinesPanel = createLifelinePanel(); 
        lifelinesPanel.relocate(30, 870);
        uiLayer.getChildren().add(lifelinesPanel);

        // Center: Game Board
        VBox gameBoard = createGameBoard();
        gameBoard.relocate(560, 720); 
        uiLayer.getChildren().add(gameBoard);

        // Bottom Right: Menu
        StackPane menuButton = createMenuDiamond();
        menuButton.relocate(1770, 930);
        uiLayer.getChildren().add(menuButton);

        // --- FINALIZE ---
        root.getChildren().addAll(backgroundView, uiLayer);

        Scene scene = new Scene(root, targetWidth, targetHeight);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("Quantum Millionaire [Res: " + (int)targetWidth + "x" + (int)targetHeight + "]");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ==============================================================================================
    // REGION BUILDERS (Designed for 1920x1080 Canvas)
    // ==============================================================================================

    private VBox createMoneyLadder() {
        VBox ladder = new VBox(5);
        ladder.setAlignment(Pos.CENTER_LEFT);
        ladder.setPadding(new Insets(15));
        ladder.getStyleClass().add("ladder-container"); 

        Label header = new Label("Question Value");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        ladder.getChildren().add(header);

        String[] values = {"$1,000,000", "$500,000", "$300,000", "$200,000", "$10,000", "$5,000", "$2,000", "$1,000", "$500", "$100"};
        
        for (String val : values) {
            Label lbl = new Label(val);
            lbl.setPrefWidth(240);
            lbl.setFont(Font.font("Arial", 16));
            lbl.setAlignment(Pos.CENTER);
            lbl.getStyleClass().add("ladder-cell"); 
            if (val.equals("$2,000")) lbl.setId("current-level");
            ladder.getChildren().add(lbl);
        }
        return ladder;
    }

    private VBox createTopRightDashboard() {
        VBox dashboard = new VBox(15);
        dashboard.setAlignment(Pos.TOP_RIGHT);

        VBox earningsBox = new VBox();
        earningsBox.setAlignment(Pos.CENTER);
        earningsBox.getStyleClass().add("dashboard-box");
        Label earnTitle = new Label("EARNINGS:");
        earnTitle.setTextFill(Color.WHITE);
        earnTitle.setFont(Font.font("Arial", 16));
        Label earnValue = new Label("$1,000");
        earnValue.setStyle("-fx-font-size: 48px; -fx-text-fill: gold; -fx-font-weight: bold;");
        earningsBox.getChildren().addAll(earnTitle, earnValue);

        HBox timerBox = new HBox();
        timerBox.setAlignment(Pos.CENTER_RIGHT);
        timerBox.getStyleClass().add("timer-box");
        Label timeLabel = new Label("24 SEC");
        timeLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: #ffcc00; -fx-font-weight: bold;");
        timerBox.getChildren().add(timeLabel);

        dashboard.getChildren().addAll(earningsBox, timerBox);
        return dashboard;
    }

    private VBox createLifelinePanel() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.getStyleClass().add("lifeline-panel");
        
        Label header = new Label("LIFELINES");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        HBox icons = new HBox(15);
        icons.setAlignment(Pos.CENTER);
        
        Button l1 = new Button("50:50");
        Button l2 = new Button("📞");
        Button l3 = new Button("👥");
        
        l1.setPrefSize(70, 70); 
        l2.setPrefSize(70, 70);
        l3.setPrefSize(70, 70);
        
        l1.getStyleClass().add("lifeline-btn");
        l2.getStyleClass().add("lifeline-btn");
        l3.getStyleClass().add("lifeline-btn");
        
        icons.getChildren().addAll(l1, l2, l3);
        box.getChildren().addAll(header, icons);
        return box;
    }

    private VBox createGameBoard() {
        VBox board = new VBox(20);
        board.setAlignment(Pos.BOTTOM_CENTER);
        
        Label question = new Label("Which planet is known as the 'Red Planet'?");
        question.getStyleClass().add("question-box");
        question.setWrapText(true);
        question.setPrefWidth(800);
        question.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");
        question.setAlignment(Pos.CENTER);

        GridPane answers = new GridPane();
        answers.setHgap(30);
        answers.setVgap(20);
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
        Button menuBtn = new Button();
        menuBtn.getStyleClass().add("menu-diamond"); 
        
        menuBtn.setScaleX(1.5);
        menuBtn.setScaleY(1.5);

        Label icon = new Label("☰"); 
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        icon.setMouseTransparent(true); 

        menuBtn.setOnAction(e -> System.out.println("Open In-Game Menu / Settings"));
        diamondContainer.getChildren().addAll(menuBtn, icon);
        return diamondContainer;
    }

    private Button createAnswerBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("answer-btn");
        btn.setPrefWidth(550);
        btn.setPrefHeight(60);
        btn.setStyle("-fx-font-size: 20px;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}