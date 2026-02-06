package Pack_1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * GameStage serves as the primary graphical user interface for the Quantum Millionaire application.
 * This class handles the initialization of the game environment, including the transitions
 * between Play Mode and Design Mode, localization for English and Farsi, and accessibility
 * theme management.
 * * <p>INTEGRATION NOTE: This class now utilizes an Atomic Design Grid (1920x1080 reference)
 * allowing for seamless resolution scaling (720p, 1080p, 4K) while maintaining all 
 * original game logic and localization features.</p>
 * * <p>Developed for the course instructed by Professor Paulo.</p>
 * @author Paria Abdzadeh
 * @author Taylor Houstoun
 * @version 2.0 (Merged Integrated)
 */
public class GameStage extends Application {

    // =============================================================================================
    // 1. THE "ATOMIC" DESIGN GRID (From Doc 2)
    // =============================================================================================
    // We will place all buttons/text based on this 1920x1080 "Virtual Paper".
    private static final double DESIGN_WIDTH = 1920; 
    private static final double DESIGN_HEIGHT = 1080;

    // =============================================================================================
    // 2. USER RESOLUTION OPTIONS
    // =============================================================================================
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
    // Set to: 1=720p, 2=1080p, 3=4K
    private static final int SELECTED_RESOLUTION_OPTION = 2; 

    // =============================================================================================
    // 3. UI FIELDS (From Doc 1 - Required for Logic)
    // =============================================================================================
    private Label questionLabel, earnTitle, earnValue, timeLabel, ladderHeader, lifelineHeader;
    private Button btnA, btnB, btnC, btnD;
    private StackPane root; // Main container for CSS styling
    
    // Mode-specific UI containers (kept as fields to toggle visibility)
    private VBox moneyLadder;
    private VBox playModeContent;
    private VBox designModeContent;
    private VBox lifelinePanel;
    private VBox topRightDashboard;
    private HBox modeToggle;

    /**
     * Initializes and displays the primary stage of the application.
     * Sets up the background image, layout managers, and initial view state.
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        
        // --- STEP 1: DETERMINE TARGET WINDOW SIZE (Resolution Logic) ---
        double targetWidth, targetHeight;

        switch (SELECTED_RESOLUTION_OPTION) {
            case 1:  targetWidth = RES_720P_WIDTH; targetHeight = RES_720P_HEIGHT; break;
            case 3:  targetWidth = RES_4K_WIDTH;   targetHeight = RES_4K_HEIGHT;   break;
            default: targetWidth = RES_1080P_WIDTH; targetHeight = RES_1080P_HEIGHT;
        }

        // --- STEP 2: SAFETY CLAMP (The "Fit to Screen" Logic) ---
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Check if target is wider than the physical screen
        if (targetWidth > screenBounds.getWidth() || targetHeight > screenBounds.getHeight()) {
            double widthRatio = screenBounds.getWidth() / targetWidth;
            double heightRatio = screenBounds.getHeight() / targetHeight;
            double safeScale = Math.min(widthRatio, heightRatio);
            targetWidth = targetWidth * safeScale;
            targetHeight = targetHeight * safeScale;
        }

        // --- STEP 3: SETUP ROOT & BACKGROUND ---
        root = new StackPane();
        root.setStyle("-fx-background-color: black;"); 

        ImageView backgroundView = new ImageView();
        try {
            Image bgImage = new Image("studio_bg.jpg"); 
            backgroundView.setImage(bgImage);
            backgroundView.setPreserveRatio(true);
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
        double scaleFactor = targetWidth / DESIGN_WIDTH;
        Scale scale = new Scale(scaleFactor, scaleFactor);
        scale.setPivotX(0);
        scale.setPivotY(0);
        uiLayer.getTransforms().add(scale);

        // --- STEP 6: PLACE ASSETS (Merging Logic from Doc 1 into Coordinates of Doc 2) ---
        
        // 6a. Money Ladder (Left)
        moneyLadder = createMoneyLadder();
        moneyLadder.relocate(30, 75); 
        
        // 6b. Dashboard (Top Right)
        topRightDashboard = createTopRightDashboard();
        topRightDashboard.relocate(1500, 30);

        // 6c. Lifelines (Bottom Left)
        lifelinePanel = createLifelinePanel(); 
        lifelinePanel.relocate(30, 870);

        // 6d. Game Board (Bottom Center)
        playModeContent = createGameBoard();
        playModeContent.relocate(560, 720); 

        // 6e. Design Mode Content (Center Screen)
        designModeContent = createDesignBoard();
        designModeContent.relocate(DESIGN_WIDTH / 2 - 200, DESIGN_HEIGHT / 2 - 100); // Centered relative to 1920x1080

        // 6f. Mode Toggle (Top Center)
        modeToggle = createModeToggle();
        modeToggle.relocate(810, 30); // Centered horizontally
        
        // 6g. Menu Diamond (Bottom Right)
        StackPane menuButton = createMenuDiamond();
        menuButton.relocate(1770, 930);

        // Add all potential children to the layer
        uiLayer.getChildren().addAll(moneyLadder, topRightDashboard, lifelinePanel, playModeContent, designModeContent, modeToggle, menuButton);

        // --- FINALIZE ---
        // Initialize Default View (Play Mode)
        updateViewMode(true);

        root.getChildren().addAll(backgroundView, uiLayer);
        Scene scene = new Scene(root, targetWidth, targetHeight);

        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Warning: CSS file 'style.css' not found.");
        }

        primaryStage.setTitle("Quantum Millionaire - Team Ria [Res: " + (int)targetWidth + "x" + (int)targetHeight + "]");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Toggles the visibility of UI components based on the active mode.
     * Rewritten to use setVisible() for the Atomic Grid system instead of BorderPane.
     * @param isPlayMode true to show gameplay elements, false for design workspace.
     */
    private void updateViewMode(boolean isPlayMode) {
        if (isPlayMode) {
            moneyLadder.setVisible(true);
            playModeContent.setVisible(true);
            lifelinePanel.setVisible(true);
            topRightDashboard.setVisible(true);
            designModeContent.setVisible(false);
        } else {
            moneyLadder.setVisible(false);
            playModeContent.setVisible(false);
            lifelinePanel.setVisible(false);
            topRightDashboard.setVisible(false);
            designModeContent.setVisible(true);
        }
    }

    // ==============================================================================================
    // REGION BUILDERS (Merged Styles + Logic)
    // ==============================================================================================

    /**
     * Creates the mode switching control using ToggleButtons.
     * @return HBox containing the Play/Design mode toggle controls.
     */
    private HBox createModeToggle() {
        HBox toggleBox = new HBox(0);
        toggleBox.setAlignment(Pos.CENTER);
        
        ToggleButton playBtn = new ToggleButton("Play Mode");
        ToggleButton designBtn = new ToggleButton("Design Mode");
        
        // Increase font size for high-res grid
        playBtn.setStyle("-fx-font-size: 18px;");
        designBtn.setStyle("-fx-font-size: 18px;");
        
        playBtn.getStyleClass().add("toggle-left");
        designBtn.getStyleClass().add("toggle-right");

        ToggleGroup group = new ToggleGroup();
        playBtn.setToggleGroup(group);
        designBtn.setToggleGroup(group);
        playBtn.setSelected(true);

        playBtn.setOnAction(e -> updateViewMode(true));
        designBtn.setOnAction(e -> updateViewMode(false));

        toggleBox.getChildren().addAll(playBtn, designBtn);
        return toggleBox;
    }

    /**
     * Builds the workspace for the Design Mode.
     * @return VBox containing the "Add Question" button and editor instructions.
     */
    private VBox createDesignBoard() {
        VBox board = new VBox(20);
        board.setAlignment(Pos.CENTER);
        
        Button addBtn = new Button("+ ADD NEW QUESTION");
        addBtn.getStyleClass().add("add-question-btn");
        addBtn.setStyle("-fx-font-size: 24px; -fx-padding: 20 40;"); // Up-scaled styling
        
        Label instr = new Label("Editor Mode Active");
        instr.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 20px;");
        
        board.getChildren().addAll(addBtn, instr);
        return board;
    }

    private VBox createMoneyLadder() {
        VBox ladder = new VBox(5);
        ladder.setAlignment(Pos.CENTER_LEFT);
        ladder.setPadding(new Insets(15));
        ladder.getStyleClass().add("ladder-container"); 

        ladderHeader = new Label("Question Value");
        ladderHeader.setTextFill(Color.WHITE);
        ladderHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        ladder.getChildren().add(ladderHeader);

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
        
        earnTitle = new Label("EARNINGS:");
        earnTitle.setTextFill(Color.WHITE);
        earnTitle.setFont(Font.font("Arial", 16));
        
        earnValue = new Label("$1,000");
        earnValue.setStyle("-fx-font-size: 48px; -fx-text-fill: gold; -fx-font-weight: bold;");
        earningsBox.getChildren().addAll(earnTitle, earnValue);

        HBox timerBox = new HBox();
        timerBox.setAlignment(Pos.CENTER_RIGHT);
        timerBox.getStyleClass().add("timer-box");
        
        timeLabel = new Label("24 SEC");
        timeLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: #ffcc00; -fx-font-weight: bold;");
        timerBox.getChildren().add(timeLabel);

        dashboard.getChildren().addAll(earningsBox, timerBox);
        return dashboard;
    }

    private VBox createLifelinePanel() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.getStyleClass().add("lifeline-panel");
        
        lifelineHeader = new Label("LIFELINES");
        lifelineHeader.setTextFill(Color.WHITE);
        lifelineHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        HBox icons = new HBox(15);
        icons.setAlignment(Pos.CENTER);
        
        String[] life = {"50:50", "📞", "👥"};
        for (String s : life) {
            Button b = new Button(s);
            b.setPrefSize(70, 70); 
            b.getStyleClass().add("lifeline-btn");
            icons.getChildren().add(b);
        }
        
        box.getChildren().addAll(lifelineHeader, icons);
        return box;
    }

    private VBox createGameBoard() {
        VBox board = new VBox(20);
        board.setAlignment(Pos.BOTTOM_CENTER);
        
        questionLabel = new Label("Which planet is known as the 'Red Planet'?");
        questionLabel.getStyleClass().add("question-box");
        questionLabel.setWrapText(true);
        questionLabel.setPrefWidth(800);
        questionLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");
        questionLabel.setAlignment(Pos.CENTER);

        GridPane answers = new GridPane();
        answers.setHgap(30);
        answers.setVgap(20);
        answers.setAlignment(Pos.CENTER);
        
        // Connect Class Fields (btnA, btnB) to the creation logic
        btnA = createAnswerBtn("A: Venus"); 
        btnB = createAnswerBtn("B: Mars");
        btnC = createAnswerBtn("C: Jupiter"); 
        btnD = createAnswerBtn("D: Saturn");

        answers.add(btnA, 0, 0);
        answers.add(btnB, 1, 0);
        answers.add(btnC, 0, 1);
        answers.add(btnD, 1, 1);

        board.getChildren().addAll(questionLabel, answers);
        return board;
    }

    private Button createAnswerBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("answer-btn");
        btn.setPrefWidth(550);
        btn.setPrefHeight(60);
        btn.setStyle("-fx-font-size: 20px;");
        return btn;
    }

    /**
     * Creates the settings menu triggered by a diamond-shaped button.
     * Merges Doc 1's ContextMenu logic with Doc 2's Diamond visual style.
     */
    private StackPane createMenuDiamond() {
        StackPane diamondContainer = new StackPane();
        diamondContainer.setAlignment(Pos.CENTER);
        
        Button menuBtn = new Button();
        menuBtn.getStyleClass().add("menu-diamond"); 
        menuBtn.setScaleX(1.5);
        menuBtn.setScaleY(1.5);

        // --- CONTEXT MENU LOGIC FROM DOC 1 ---
        ContextMenu menu = new ContextMenu();
        
        Menu lM = new Menu("Language");
        MenuItem eI = new MenuItem("English"); MenuItem fI = new MenuItem("فارسی");
        eI.setOnAction(e -> switchToEnglish()); fI.setOnAction(e -> switchToFarsi());
        lM.getItems().addAll(eI, fI);

        Menu cM = new Menu("Colors");
        MenuItem dC = new MenuItem("Default"); MenuItem deutC = new MenuItem("Deuteranopia"); MenuItem tritC = new MenuItem("Tritanopia");
        dC.setOnAction(e -> applyTheme("default")); deutC.setOnAction(e -> applyTheme("theme-deuteranopia")); tritC.setOnAction(e -> applyTheme("theme-tritanopia"));
        cM.getItems().addAll(dC, deutC, tritC);

        Menu lnf = new Menu("Look and Feel");
        MenuItem mod = new MenuItem("Modern"); MenuItem cls = new MenuItem("Classic");
        mod.setOnAction(e -> applyLookAndFeel("modern-style")); cls.setOnAction(e -> applyLookAndFeel("classic-style"));
        lnf.getItems().addAll(mod, cls);

        menu.getItems().addAll(lM, cM, lnf);
        menuBtn.setOnAction(e -> menu.show(menuBtn, Side.TOP, 0, -60));

        Label icon = new Label("☰"); 
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        icon.setMouseTransparent(true); 

        diamondContainer.getChildren().addAll(menuBtn, icon);
        return diamondContainer;
    }

    // ==============================================================================================
    // LOGIC & HELPER METHODS (From Doc 1)
    // ==============================================================================================

    /**
     * Switches the UI text and orientation to Persian (Farsi).
     */
    private void switchToFarsi() {
        if(questionLabel != null) questionLabel.setText("کدام سیاره به 'سیاره سرخ' معروف است؟");
        earnTitle.setText("دارایی:"); timeLabel.setText("۲۴ ثانیه");
        root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    /**
     * Switches the UI text and orientation to English.
     */
    private void switchToEnglish() {
        if(questionLabel != null) questionLabel.setText("Which planet is known as the 'Red Planet'?");
        earnTitle.setText("EARNINGS:"); timeLabel.setText("24 SEC");
        root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    }

    private void applyTheme(String t) {
        root.getStyleClass().removeAll("theme-deuteranopia", "theme-tritanopia");
        if (!t.equals("default")) root.getStyleClass().add(t);
    }

    private void applyLookAndFeel(String s) {
        root.getStyleClass().removeAll("modern-style", "classic-style");
        if (!s.equals("default")) root.getStyleClass().add(s);
    }

    public static void main(String[] args) { launch(args); }
}