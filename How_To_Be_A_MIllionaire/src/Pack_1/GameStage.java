package Pack_1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * GameStage serves as the primary graphical user interface for the Quantum Millionaire application.
 * This class handles the initialization of the game environment, including the transitions
 * between Play Mode and Design Mode, localization for English and Farsi, and accessibility
 * theme management.
 * * <p>Developed for the course instructed by Professor Paulo.</p>
 * * @author Paria Abdzadeh
 * @author Taylor Houstoun
 * @version 1.2
 */
public class GameStage extends Application {

    /** Default window width for the application. */
    private static final double WINDOW_WIDTH = 1280;
    /** Default window height for the application. */
    private static final double WINDOW_HEIGHT = 720;

    // --- UI FIELDS ---
    private Label questionLabel, earnTitle, earnValue, timeLabel, ladderHeader, lifelineHeader;
    private Button btnA, btnB, btnC, btnD;
    private StackPane root;
    private BorderPane mainLayout;

    // Mode-specific UI containers
    private VBox moneyLadder;
    private VBox playModeContent;
    private VBox designModeContent;
    private VBox lifelinePanel;
    private VBox topRightDashboard;

    /**
     * Initializes and displays the primary stage of the application.
     * Sets up the background image, layout managers, and initial view state.
     * * @param primaryStage The primary stage for this application, onto which
     * the application scene is set.
     */
    @Override
    public void start(Stage primaryStage) {
        root = new StackPane();

        // 1. Background Setup
        ImageView backgroundView = new ImageView();
        try {
            backgroundView.setImage(new Image("studio_bg.jpg"));
            backgroundView.fitWidthProperty().bind(primaryStage.widthProperty());
            backgroundView.fitHeightProperty().bind(primaryStage.heightProperty());
        } catch (Exception e) {
            // Fallback gradient if image is missing
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #1a0b2e);");
        }

        // 2. Main Layout Initialization
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        moneyLadder = createMoneyLadder();
        playModeContent = createGameBoard();
        designModeContent = createDesignBoard();
        lifelinePanel = createLifelinePanel();
        topRightDashboard = createTopRightDashboard();

        // 3. Top Bar Construction (Toggle + Spacer + Dashboard)
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_CENTER);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(createModeToggle(), spacer, topRightDashboard);
        mainLayout.setTop(topBar);

        // 4. Interaction Menu (Diamond Button)
        StackPane menuButton = createMenuDiamond();
        mainLayout.setRight(menuButton);
        BorderPane.setAlignment(menuButton, Pos.BOTTOM_RIGHT);

        // 5. Default View state
        updateViewMode(true);

        root.getChildren().addAll(backgroundView, mainLayout);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Warning: CSS file 'style.css' not found in Pack_1.");
        }

        primaryStage.setTitle("Quantum Millionaire - Team Ria");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Toggles the visibility of UI components based on the active mode.
     * In Play Mode, the Money Ladder, Lifelines, and Dashboard are shown.
     * In Design Mode, these are hidden to provide a clean editing workspace.
     * * @param isPlayMode true to show gameplay elements, false for design workspace.
     */
    private void updateViewMode(boolean isPlayMode) {
        if (isPlayMode) {
            mainLayout.setLeft(moneyLadder);
            mainLayout.setCenter(playModeContent);
            mainLayout.setBottom(lifelinePanel);
            topRightDashboard.setVisible(true);
        } else {
            mainLayout.setLeft(null);
            mainLayout.setBottom(null);
            mainLayout.setCenter(designModeContent);
            topRightDashboard.setVisible(false);
        }
    }

    /**
     * Creates the mode switching control using ToggleButtons.
     * * @return HBox containing the Play/Design mode toggle controls.
     */
    private HBox createModeToggle() {
        HBox toggleBox = new HBox(0);
        ToggleButton playBtn = new ToggleButton("Play Mode");
        ToggleButton designBtn = new ToggleButton("Design Mode");
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
     * * @return VBox containing the "Add Question" button and editor instructions.
     */
    private VBox createDesignBoard() {
        VBox board = new VBox(20);
        board.setAlignment(Pos.CENTER);
        Button addBtn = new Button("+ ADD NEW QUESTION");
        addBtn.getStyleClass().add("add-question-btn");
        Label instr = new Label("Editor Mode Active");
        instr.setStyle("-fx-text-fill: rgba(255,255,255,0.4);");
        board.getChildren().addAll(addBtn, instr);
        return board;
    }

    /**
     * Constructs the vertical money ladder indicating question values.
     * * @return VBox containing the prize value labels.
     */
    private VBox createMoneyLadder() {
        VBox ladder = new VBox(2);
        ladder.setAlignment(Pos.CENTER_LEFT);
        ladder.getStyleClass().add("ladder-container");
        ladderHeader = new Label("Question Value");
        ladderHeader.setTextFill(Color.WHITE);
        ladder.getChildren().add(ladderHeader);
        String[] vals = {"$1,000,000", "$500,000", "$300,000", "$200,000", "$10,000", "$5,000", "$2,000", "$1,000", "$500", "$100"};
        for (String v : vals) {
            Label lbl = new Label(v);
            lbl.setPrefWidth(180); lbl.setAlignment(Pos.CENTER);
            lbl.getStyleClass().add("ladder-cell");
            if (v.equals("$2,000")) lbl.setId("current-level");
            ladder.getChildren().add(lbl);
        }
        return ladder;
    }

    /**
     * Creates the dashboard for user earnings and the countdown timer.
     * * @return VBox containing the earnings and timer displays.
     */
    private VBox createTopRightDashboard() {
        VBox db = new VBox(10);
        db.setAlignment(Pos.TOP_RIGHT);
        VBox earnBox = new VBox();
        earnBox.setAlignment(Pos.CENTER);
        earnBox.getStyleClass().add("dashboard-box");
        earnTitle = new Label("EARNINGS:");
        earnTitle.setTextFill(Color.WHITE);
        earnValue = new Label("$1,000");
        earnValue.setStyle("-fx-font-size: 36px; -fx-text-fill: gold; -fx-font-weight: bold;");
        earnBox.getChildren().addAll(earnTitle, earnValue);
        HBox timerBox = new HBox();
        timerBox.setAlignment(Pos.CENTER_RIGHT);
        timerBox.getStyleClass().add("timer-box");
        timeLabel = new Label("24 SEC");
        timeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #ffcc00; -fx-font-weight: bold;");
        timerBox.getChildren().add(timeLabel);
        db.getChildren().addAll(earnBox, timerBox);
        return db;
    }

    /**
     * Constructs the lifeline selection panel.
     * * @return VBox containing the lifeline action buttons.
     */
    private VBox createLifelinePanel() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.getStyleClass().add("lifeline-panel");
        lifelineHeader = new Label("LIFELINES");
        lifelineHeader.setTextFill(Color.WHITE);
        HBox icons = new HBox(8);
        icons.setAlignment(Pos.CENTER);
        String[] life = {"50:50", "📞", "👥"};
        for (String s : life) {
            Button b = new Button(s);
            b.getStyleClass().add("lifeline-btn");
            icons.getChildren().add(b);
        }
        box.getChildren().addAll(lifelineHeader, icons);
        return box;
    }

    /**
     * Builds the main gameplay board including the question and answer choices.
     * * @return VBox containing the question label and answer grid.
     */
    private VBox createGameBoard() {
        VBox board = new VBox(10);
        board.setAlignment(Pos.BOTTOM_CENTER);
        questionLabel = new Label("Which planet is known as the 'Red Planet'?");
        questionLabel.getStyleClass().add("question-box");
        questionLabel.setWrapText(true);
        questionLabel.setPrefWidth(800);
        GridPane ans = new GridPane();
        ans.setHgap(15); ans.setVgap(10); ans.setAlignment(Pos.CENTER);
        btnA = createAnswerBtn("A: Venus"); btnB = createAnswerBtn("B: Mars");
        btnC = createAnswerBtn("C: Jupiter"); btnD = createAnswerBtn("D: Saturn");
        ans.add(btnA, 0, 0); ans.add(btnB, 1, 0); ans.add(btnC, 0, 1); ans.add(btnD, 1, 1);
        board.getChildren().addAll(questionLabel, ans);
        return board;
    }

    /**
     * Creates the settings menu triggered by a diamond-shaped button.
     * * @return StackPane containing the button and its icon.
     */
    private StackPane createMenuDiamond() {
        StackPane container = new StackPane();
        Button menuBtn = new Button();
        menuBtn.getStyleClass().add("menu-diamond");
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
        Label icon = new Label("☰"); icon.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
        icon.setMouseTransparent(true);
        container.getChildren().addAll(menuBtn, icon);
        return container;
    }

    /**
     * Utility method for creating uniform answer buttons.
     * * @param text The text to display on the button.
     * @return A stylized Button for answer selection.
     */
    private Button createAnswerBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("answer-btn");
        b.setPrefWidth(400);
        return b;
    }

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

    /**
     * Applies a color-blind friendly theme to the application root.
     * * @param t The CSS class name of the theme to apply.
     */
    private void applyTheme(String t) {
        root.getStyleClass().removeAll("theme-deuteranopia", "theme-tritanopia");
        if (!t.equals("default")) root.getStyleClass().add(t);
    }

    /**
     * Applies a specific visual style (e.g., Modern vs Classic) to the UI components.
     * * @param s The CSS class name for the look-and-feel style.
     */
    private void applyLookAndFeel(String s) {
        root.getStyleClass().removeAll("modern-style", "classic-style");
        if (!s.equals("default")) root.getStyleClass().add(s);
    }

    /**
     * Main entry point for the JavaFX application.
     * * @param args Command line arguments.
     */
    public static void main(String[] args) { launch(args); }
}