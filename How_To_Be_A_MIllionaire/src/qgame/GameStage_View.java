package qgame;

import java.awt.Dimension;
import java.net.URL;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
 *
 * <p><b>SCREEN-CLASS NOTE:</b> This class was originally a JavaFX Application subclass.
 * It has been refactored into a "screen class" that <b>builds and owns a Scene</b> but
 * does <b>not</b> extend Application and does <b>not</b> call launch() or show() directly.
 * The Stage lifecycle is now controlled by an external entry point (e.g., MainApp or FrontPage),
 * which constructs GameStage and retrieves its Scene via {@link #getScene()}.</p>
 *
 * <p>All original comments and logic have been preserved; only structural changes were made
 * to support the screen-class architecture.</p>
 *
 * @author Paria Abdzadeh
 * @author Taylor Houstoun
 * @version 2.0 (Merged Integrated, Screen-Class Refactor)
 */
public class GameStage_View {

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
	// 3. UI FIELDS 
	// =============================================================================================
	private GameBrain_Controller controller;
	private Label questionLabel, earnTitle, earnValue, timeLabel, ladderHeader, lifelineHeader;
	private Button btnA, btnB, btnC, btnD;
	private StackPane root; // Main container for CSS styling
	private String activeTheme = "default";
	private StackPane menuOverlay;
	private Node menuNode; // the front-page root reused as overlay content
	private Pane designModeLayer;
	Pane uiLayer = new Pane();

	// Mode-specific UI containers (kept as fields to toggle visibility)
	private VBox moneyLadder;
	private VBox playModeContent;
	private VBox designModeContent;
	private VBox lifelinePanel;
	private VBox topRightDashboard;
	private HBox modeToggle;
	
	// =============================================================================================
	// 3b. SUPPORT FIELDS - COUNTDOWNS / TIMELINES / SPECIAL FUNCTIONALITY
	// =============================================================================================
//	/** Whether the game is currently paused (menu shown). */
//	private volatile boolean paused = false;

	/** Optional game loop using AnimationTimer (if you have one). */
	private javafx.animation.AnimationTimer gameLoop;

	/** Optional Timeline used for countdowns or other scheduled tasks. */
	private javafx.animation.Timeline gameTimeline;

//	/** Optional background media player reference (if you use one in GameStage). */
//	private javafx.scene.media.MediaPlayer localBgPlayer; // if you create one here; otherwise ignore

//	/** Keep a list of nodes to disable while paused (convenience). */
	private final java.util.List<Node> pauseDisableList = new java.util.ArrayList<>();


	// =============================================================================================
	// 3c. SCREEN-CLASS FIELDS
	// =============================================================================================
	
	private final Stage stage;		// GameStage_View does Not own the lifecycle. Come from Controller.
	private final Scene scene;		// The Scene built by this screen class and exposed via getScene().

	
	// =============================================================================================
	// 4. GAME STAGE - MAIN CLASS BODY AND CONSTRUCTOR + PRIMARY VISIBILITY METHOD
	// =============================================================================================
	
	//---------------------------------------------------------------
	// The GameStage_View class method body
	/**
	 * Initializes and configures the game UI for the provided Stage.
	 * <p>
	 * This constructor replaces the original {@code start(Stage primaryStage)} method
	 * from the Application subclass. All original initialization logic has been moved
	 * here with comments preserved. The Stage is not shown here; the caller is
	 * responsible for calling {@code stage.setScene(gameStage.getScene())} and
	 * {@code stage.show()}.
	 * </p>
	 *
	 * @param primaryStage The primary stage for this application screen, supplied by MainApp/FrontPage.
	 */
	public GameStage_View(Stage primaryStage, GameBrain_Controller controller) {

	    this.stage = primaryStage;
	    this.controller = controller;
	    controller.setView(this);

	    // 1. Determine resolution
	    double targetWidth = computeTargetWidth();
	    double targetHeight = computeTargetHeight();

	    // 2. Safety clamp
	    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
	    double[] safe = clampToScreen(targetWidth, targetHeight, screenBounds);
	    targetWidth = safe[0];
	    targetHeight = safe[1];

	    // 3. Build root
	    root = new StackPane();

	    // 4. Background layer
	    Node backgroundView = buildBackgroundLayer(targetWidth, targetHeight);

	    // 5. UI layer (scaled 1920x1080)
	    uiLayer = buildUILayer();
	    applyScaling(uiLayer, targetWidth);

	    // 6. Menu overlay
	    buildMenuOverlay();

	    // 7. Add layers
	    root.getChildren().addAll(backgroundView, uiLayer, menuOverlay);

	    // 8. Scene
	    Scene localScene = new Scene(root, targetWidth, targetHeight);

	    // 9. ESC → Controller
	    localScene.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
	        if (ev.getCode() == KeyCode.ESCAPE) {
	            controller.onPauseToggleRequested();
	        }

	        if (ev.getCode() == KeyCode.Q &&
	            ev.isControlDown() &&
	            ev.isShiftDown()) {
	            controller.onDevTogglePressed();
	        }
	    });

	    // 10. CSS
	    try {
	        localScene.getStylesheets().add(
	            getClass().getResource("style.css").toExternalForm()
	        );
	    } catch (Exception e) {
	        System.out.println("Warning: CSS file 'style.css' not found.");
	    }

	    this.scene = localScene;
	}


	//---------------------------------------------------------------
	// Toggles visibility of UI components on or off
	/**
	 * Toggles the visibility of UI components based on the active mode.
	 * Rewritten to use setVisible() for the Atomic Grid system instead of BorderPane.
	 * @param isPlayMode true to show gameplay elements, false for design workspace.
	 */
	private void updatePlayModeVisibilities(boolean isPlayMode) {
	    if (isPlayMode) {
	        moneyLadder.setVisible(true);
	        moneyLadder.setManaged(true);
	        playModeContent.setVisible(true);
	        playModeContent.setManaged(true);
	        lifelinePanel.setVisible(true);
	        lifelinePanel.setManaged(true);
	        topRightDashboard.setVisible(true);
	        topRightDashboard.setManaged(true);

	        designModeContent.setVisible(false);
	        designModeContent.setManaged(false);
	        designModeContent.setMouseTransparent(true);
	    } else {
	        moneyLadder.setVisible(false);
	        moneyLadder.setManaged(false);
	        playModeContent.setVisible(false);
	        playModeContent.setManaged(false);
	        lifelinePanel.setVisible(false);
	        lifelinePanel.setManaged(false);
	        topRightDashboard.setVisible(false);
	        topRightDashboard.setManaged(false);

	        designModeContent.setVisible(true);
	        designModeContent.setManaged(true);
	        designModeContent.setMouseTransparent(false);
	    }
	}

	// ==============================================================================================
	// GAMESTAGE_VIEW CLASS BODY (ONLY) - GETTERS AND SETTERS
	// ==============================================================================================

	/**
	 * Returns the Scene built and managed by this GameStage screen class.
	 * <p>
	 * This replaces the original pattern where GameStage (as an Application subclass)
	 * directly called {@code primaryStage.setScene(scene)} and {@code primaryStage.show()}.
	 * The caller is now responsible for attaching this Scene to the Stage.
	 * </p>
	 *
	 * @return the Scene representing the Quantum Millionaire game UI.
	 */
	public Scene getScene() {
		return scene;
	}

	// ==============================================================================================
	// REGION BUILDERS (Merged Styles + Logic)
	// ==============================================================================================

	//---------------------------------------------------------------
	// Design mode board Builder (A vBox Board) -- REMOVE TO MENU?
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


	private VBox createMoneyLadder_LocalGS() {
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

	//---------------------------------------------------------------
	// Assembled the Top Right Dashboard for Display.
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

	//---------------------------------------------------------------
	// Assembled the Lifeline Panel for Display.
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

	//---------------------------------------------------------------
	// Assembled the main Game Board for Display
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

	//---------------------------------------------------------------
	// Creates UI Element - Answer Button
	private Button createAnswerBtn(String text) {
		Button btn = new Button(text);
		btn.getStyleClass().add("answer-btn");
		btn.setPrefWidth(550);
		btn.setPrefHeight(60);
		btn.setStyle("-fx-font-size: 20px;");
		return btn;
	}




	// ==============================================================================================
	// LOGIC & HELPER METHODS - PAUSE and UNPAUSE
	// ==============================================================================================

	//---------------------------------------------------------------
	// Applies a pause on numerous game function
	public void applyPause() {
	    // Pause AnimationTimer
	    if (gameLoop != null) {
	        try { gameLoop.stop(); } catch (Exception ignored) {}
	    }

	    // Pause Timeline
	    if (gameTimeline != null) {
	        try { gameTimeline.pause(); } catch (Exception ignored) {}
	    }

	    // Disable interactive nodes
	    for (Node n : pauseDisableList) {
	        n.setDisable(true);
	    }

	    // Block mouse events on main UI
	    if (uiLayer != null) {
	        uiLayer.setMouseTransparent(true);
	    }
	}

	//---------------------------------------------------------------
	// Applies resumed functions to the game
	public void applyResume() {
	    // Resume AnimationTimer
	    if (gameLoop != null) {
	        try { gameLoop.start(); } catch (Exception ignored) {}
	    }

	    // Resume Timeline
	    if (gameTimeline != null) {
	        try { gameTimeline.play(); } catch (Exception ignored) {}
	    }

	    // Re-enable interactive nodes
	    for (Node n : pauseDisableList) {
	        n.setDisable(false);
	    }

	    // Restore mouse events
	    if (uiLayer != null) {
	        uiLayer.setMouseTransparent(false);
	    }
	}



    //---------------------------------------------------------------
    // GameStage_View (gameView) - Primary Construction Methods
    //---------------------------------------------------------------
	
	// Target Width Resolution Computation
	private double computeTargetWidth() {
	    switch (SELECTED_RESOLUTION_OPTION) {
	        case 1: return RES_720P_WIDTH;
	        case 3: return RES_4K_WIDTH;
	        default: return RES_1080P_WIDTH;
	    }
	}

	//---------------------------------------------------------------
	// Target Height Resolution Computation
	private double computeTargetHeight() {
	    switch (SELECTED_RESOLUTION_OPTION) {
	        case 1: return RES_720P_HEIGHT;
	        case 3: return RES_4K_HEIGHT;
	        default: return RES_1080P_HEIGHT;
	    }
	}
	
	//---------------------------------------------------------------
	// Forces the view window into a certain size (A Screen Clamp)
	private double[] clampToScreen(double width, double height, Rectangle2D screenBounds) {
	    if (width > screenBounds.getWidth() || height > screenBounds.getHeight()) {
	        double widthRatio = screenBounds.getWidth() / width;
	        double heightRatio = screenBounds.getHeight() / height;
	        double safeScale = Math.min(widthRatio, heightRatio);
	        width *= safeScale;
	        height *= safeScale;
	    }
	    return new double[] { width, height };
	}

	//---------------------------------------------------------------
	// Builds the Background Layer (under the UI layer - background Image)
	private Node buildBackgroundLayer(double targetWidth, double targetHeight) {
	    ImageView backgroundView = new ImageView();
	    try {
	        Image bgImage = new Image(
	            getClass().getResource("/assets/WWTB_A_Millionaire_Background.png").toExternalForm()
	        );
	        backgroundView.setImage(bgImage);
	        backgroundView.setPreserveRatio(true);
	        backgroundView.setFitWidth(targetWidth);
	        backgroundView.setFitHeight(targetHeight);
	    } catch (Exception e) {
	        backgroundView.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #1a0b2e);");
	    }
	    return backgroundView;
	}
	
	//---------------------------------------------------------------
	// Builds the UI layer with a lot of UI creation code.
	private Pane buildUILayer() {

	    Pane layer = new Pane();
	    layer.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
	    layer.setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);

	    // --- MONEY LADDER (Left) ---
	    moneyLadder = new VBox();
	    moneyLadder.setAlignment(Pos.CENTER_LEFT);
	    moneyLadder.setPadding(new Insets(15));
	    moneyLadder.getStyleClass().add("ladder-container");
	    moneyLadder.relocate(30, 75);

	    // --- TOP RIGHT DASHBOARD ---
	    topRightDashboard = createTopRightDashboard();
	    topRightDashboard.relocate(1500, 30);

	    // --- LIFELINES (Bottom Left) ---
	    lifelinePanel = createLifelinePanel();
	    lifelinePanel.relocate(30, 870);

	    // --- GAME BOARD (Bottom Center) ---
	    playModeContent = createGameBoard();
	    playModeContent.relocate(560, 720);

	    // --- DESIGN MODE CONTENT (Center) ---
	    designModeContent = createDesignBoard();
	    designModeContent.relocate(DESIGN_WIDTH / 2 - 200, DESIGN_HEIGHT / 2 - 100);

	    // --- MODE TOGGLE (Top Center) ---
//	    modeToggle = createModeToggle();
//	    modeToggle.relocate(810, 30);

	    // --- MENU DIAMOND (Bottom Right) ---    ->   Removed For Now
//	    StackPane menuButton = createMenuDiamond();
//	    menuButton.relocate(1770, 930);

	    // Add all UI elements to the layer
	    layer.getChildren().addAll(
	        moneyLadder,
	        topRightDashboard,
	        lifelinePanel,
	        playModeContent,
	        designModeContent
	        //modeToggle
	        //menuButton    ->     Unused
	    );

	    return layer;
	}

	//---------------------------------------------------------------
	// Applies a target Scale Factor to entire project.
	private void applyScaling(Pane uiLayer, double targetWidth) {
	    double scaleFactor = targetWidth / DESIGN_WIDTH;
	    Scale scale = new Scale(scaleFactor, scaleFactor);
	    scale.setPivotX(0);
	    scale.setPivotY(0);
	    uiLayer.getTransforms().add(scale);
	}


    //---------------------------------------------------------------
    // Main Menu in Game (ESC Key Activated)  -- View Portion
    //---------------------------------------------------------------

	//---------------------------------------------------------------
	// ADD JAVADOCS, MENU OVERLAY METHOD
	// build overlay (no extra Stage)
	private void buildMenuOverlay() {
		menuOverlay = new StackPane();
		menuOverlay.setVisible(false);							// false = not visible on startup
		menuOverlay.setPickOnBounds(true);
		menuOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

		// keep the overlay sized to the window so the dim covers everything
		menuOverlay.prefWidthProperty().bind(root.widthProperty());
		menuOverlay.prefHeightProperty().bind(root.heightProperty());

		// Placeholder while no node is attached
		menuNode = buildPauseMenuContent();                   	// create fresh menu node (820x820)
		if (menuNode != null) {
			menuOverlay.getChildren().clear();
			StackPane.setAlignment(menuNode, Pos.CENTER);  		// center the node
			menuOverlay.getChildren().add(menuNode);
			System.out.println("[DBG] Menu node attached: " + menuNode.getClass().getName());
		} else {   												// fallback placeholder if factory failed

			Label placeholder = new Label("MENU LOAD FAIL");
			placeholder.setStyle("-fx-font-size: 36px; -fx-text-fill: white;");
			menuOverlay.getChildren().add(placeholder);
		}
	}

	//---------------------------------------------------------------
	// Show and Hide the main menu (on Pause)
	public void showPauseMenu() {
	    if (menuOverlay == null) {
	        menuOverlay = new StackPane();
	        menuOverlay.setVisible(false);
	        menuOverlay.setPickOnBounds(true);
	        menuOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
	        menuOverlay.prefWidthProperty().bind(root.widthProperty());
	        menuOverlay.prefHeightProperty().bind(root.heightProperty());
	        root.getChildren().add(menuOverlay);
	    }

	    refreshPauseMenu();
	    menuOverlay.setVisible(true);
	    menuOverlay.toFront();
	}

	public void hidePauseMenu() {
	    if (menuOverlay != null) {
	        menuOverlay.setVisible(false);
	    }
	    uiLayer.requestFocus();
	}
	
	//---------------------------------------------------------------
	// Refresh the menu when developer option buttons are toggled
	public void refreshPauseMenu() {
	    Node newMenu = buildPauseMenuContent();
	    setMenuNode(newMenu);
	}

	
	//---------------------------------------------------------------
	// Creates the node which is the Vbox and items for the Pause Menu content
	/**
	 * Build a fresh menu node: a fixed 900x900 StackPane with a MediaView background
	 * and a VBox of controls on top. The MediaPlayer is stored in this.localBgPlayer.
	 */
	private Node buildPauseMenuContent() {

	    VBox menu = new VBox(18);
	    menu.setAlignment(Pos.CENTER);
	    menu.setMaxWidth(820);
	    menu.setStyle(
	        "-fx-background-color: linear-gradient(#111, #1b1b2f);" +
	        "-fx-padding: 28;" +
	        "-fx-background-radius: 12;"
	    );

	    // --- Core Buttons ---
	    
	    Button resumeBtn   = makeMenuButton("Resume Game", e -> controller.onResumeRequested());
	    Button saveBtn     = makeMenuButton("Save Game",   e -> controller.onSaveRequested());
	    Button loadBtn     = makeMenuButton("Load Game",   e -> controller.onLoadRequested());
	    Button settingsBtn = makeMenuButton("Settings",    e -> controller.onSettingsRequested());

	    // --- Developer Row (side-by-side) ---
	    HBox devRow = new HBox(12);
	    devRow.setAlignment(Pos.CENTER);

	    Button playModeBtn   = makeMenuButton("Play Mode",   e -> controller.onPlayModeRequested());
	    Button designModeBtn = makeMenuButton("Design Mode", e -> controller.onDesignModeRequested());

	    devRow.getChildren().addAll(playModeBtn, designModeBtn);

	    // --- Return / Quit ---
	    Button returnBtn = makeMenuButton("Return to Main Menu", e -> controller.onReturnToMainMenuRequested());
	    Button quitBtn   = makeMenuButton("Quit", e -> controller.onQuitRequested());

	    // --- Add buttons in correct order ---
	    menu.getChildren().addAll(
	        resumeBtn,
	        saveBtn,
	        loadBtn,
	        settingsBtn
	    );

	    // Add developer row only if enabled
	    if (controller.getGameState().isDevOptionsEnabled()) {
	        menu.getChildren().add(devRow);
	    }

	    menu.getChildren().addAll(
	        returnBtn,
	        quitBtn
	    );

	    return menu;
	}



	//---------------------------------------------------------------
	// Helper for consistent menu button styling
	private Button makeMenuButton(String text, EventHandler<ActionEvent> handler) {
	    Button b = new Button(text);
	    b.setPrefWidth(300);
	    b.setStyle("-fx-font-size: 18px; -fx-padding: 12 20;");
	    b.setOnAction(handler);
	    return b;
	}

	//---------------------------------------------------------------
	//------------------- setMenuNode Setter ------------------------
	public void setMenuNode(Node node) {
	    menuOverlay.getChildren().clear();
	    StackPane.setAlignment(node, Pos.CENTER);
	    menuOverlay.getChildren().add(node);
	}

	
    //---------------------------------------------------------------
    // Settings Panel JavaSwing Menu  -  in 'Main Menu in Game'
    //---------------------------------------------------------------
	
	//---------------------------------------------------------------
	// Creates and displays the various settings panels
	public void showSettingsPanel() {

	    SwingNode swingNode = new SwingNode();

	    SwingUtilities.invokeLater(() -> {

	        JTabbedPane tabs = new JTabbedPane();

	        // --- Language Tab ---
	        JPanel langPanel = new JPanel();
	        JButton english = new JButton("English");
	        JButton farsi = new JButton("فارسی");
	        english.addActionListener(e -> controller.onLanguageSelected("english"));
	        farsi.addActionListener(e -> controller.onLanguageSelected("farsi"));
	        langPanel.add(english);
	        langPanel.add(farsi);
	        tabs.addTab("Language", langPanel);

	        // --- Theme Tab ---
	        JPanel themePanel = new JPanel();
	        JButton def = new JButton("Default");
	        JButton deut = new JButton("Deuteranopia");
	        JButton trit = new JButton("Tritanopia");
	        def.addActionListener(e -> controller.onThemeSelected("default"));
	        deut.addActionListener(e -> controller.onThemeSelected("theme-deuteranopia"));
	        trit.addActionListener(e -> controller.onThemeSelected("theme-tritanopia"));
	        themePanel.add(def);
	        themePanel.add(deut);
	        themePanel.add(trit);
	        tabs.addTab("Themes", themePanel);

	        // --- Look & Feel Tab ---
	        JPanel lnfPanel = new JPanel();
	        JButton modern = new JButton("Modern");
	        JButton classic = new JButton("Classic");
	        modern.addActionListener(e -> controller.onLookAndFeelSelected("modern-style"));
	        classic.addActionListener(e -> controller.onLookAndFeelSelected("classic-style"));
	        lnfPanel.add(modern);
	        lnfPanel.add(classic);
	        tabs.addTab("Look & Feel", lnfPanel);

	        swingNode.setContent(tabs);
	    });

	    VBox container = new VBox(20);
	    container.setAlignment(Pos.CENTER);
	    container.setMaxWidth(820);

	    Button returnBtn = makeMenuButton("Return", e -> refreshPauseMenu());

	    container.getChildren().addAll(swingNode, returnBtn);

	    setMenuNode(container);
	}

	//---------------------------------------------------------------
	// Build Menu Content for the 'Settings' Section
//	private Node buildSettingsMenuContent() {
//
//	    VBox menu = new VBox(18);
//	    menu.setAlignment(Pos.CENTER);
//	    menu.setMaxWidth(820);
//	    menu.setStyle(
//	        "-fx-background-color: linear-gradient(#111, #1b1b2f);" +
//	        "-fx-padding: 28;" +
//	        "-fx-background-radius: 12;"
//	    );
//
//	    // --- Title ---
//	    Label title = new Label("Settings");
//	    title.setStyle("-fx-font-size: 32px; -fx-text-fill: white;");
//
//	    // --- Language Buttons ---
//	    HBox langRow = new HBox(12);
//	    langRow.setAlignment(Pos.CENTER);
//	    Button englishBtn = makeMenuButton("English", e -> controller.onLanguageSelected("english"));
//	    Button farsiBtn   = makeMenuButton("Farsi",   e -> controller.onLanguageSelected("farsi"));
//	    langRow.getChildren().addAll(englishBtn, farsiBtn);
//
//	    // --- Theme Buttons ---
//	    HBox themeRow = new HBox(12);
//	    themeRow.setAlignment(Pos.CENTER);
//	    Button darkBtn  = makeMenuButton("Dark Theme",  e -> controller.onThemeSelected("dark"));
//	    Button lightBtn = makeMenuButton("Light Theme", e -> controller.onThemeSelected("light"));
//	    themeRow.getChildren().addAll(darkBtn, lightBtn);
//
//	    // --- Colorblind Mode ---
//	    HBox colorRow = new HBox(12);
//	    colorRow.setAlignment(Pos.CENTER);
//	    Button normalBtn = makeMenuButton("Normal Colors", e -> controller.onColorModeSelected("normal"));
//	    Button cbBtn     = makeMenuButton("Colorblind Mode", e -> controller.onColorModeSelected("colorblind"));
//	    colorRow.getChildren().addAll(normalBtn, cbBtn);
//
//	    // --- Look & Feel ---
//	    HBox lookRow = new HBox(12);
//	    lookRow.setAlignment(Pos.CENTER);
//	    Button modernBtn = makeMenuButton("Modern UI", e -> controller.onLookAndFeelSelected("modern"));
//	    Button classicBtn = makeMenuButton("Classic UI", e -> controller.onLookAndFeelSelected("classic"));
//	    lookRow.getChildren().addAll(modernBtn, classicBtn);
//
//	    // --- Back Button ---
//	    Button backBtn = makeMenuButton("Back", e -> showPauseMenu());
//
//	    // --- Add everything ---
//	    menu.getChildren().addAll(
//	        title,
//	        langRow,
//	        themeRow,
//	        colorRow,
//	        lookRow,
//	        backBtn
//	    );
//
//	    return menu;
//	}


	//---------------------------------------------------------------
	// Apply Colourblindness Theme
	public void applyTheme(String themeName) {
	    // Remove any existing theme classes
	    root.getStyleClass().removeAll(
	        "theme-deuteranopia",
	        "theme-tritanopia",
	        "default"
	    );

	    // Add the new theme class
	    root.getStyleClass().add(themeName);
	}

	//---------------------------------------------------------------
	// Alternate Apply Colourblindness Theme
//	public void applyTheme(String t) {
//	    // Remove previously applied theme class
//	    if (!"default".equals(activeTheme)) {
//	        root.getStyleClass().remove(activeTheme);
//	    }
//
//	    // Apply the new theme class
//	    if (!"default".equals(t)) {
//	        root.getStyleClass().add(t);
//	    }
//
//	    // Update tracker
//	    activeTheme = t;
//	}

	
	//---------------------------------------------------------------
	// Apply 'Look and Feel' theme
	public void applyLookAndFeel(String style) {
	    root.getStyleClass().removeAll("modern-style", "classic-style");
	    root.getStyleClass().add(style);
	}
	
	//---------------------------------------------------------------
	// Alternate Apply 'Look and Feel' theme
//	private void applyLookAndFeel(String s) {
//		root.getStyleClass().removeAll("modern-style", "classic-style");
//		if (!s.equals("default")) root.getStyleClass().add(s);
//	}

	// TODO: Replace with real localization later
	//---------------------------------------------------------------
	// Apply English Language Translation
	public void applyLanguageEnglish() {
	    if (questionLabel != null)
	        questionLabel.setText("Which planet is known as the 'Red Planet'?");

	    earnTitle.setText("EARNINGS:");
	    timeLabel.setText("24 SEC");

	    root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
	}

	// TODO: Replace with real localization later
	//---------------------------------------------------------------
	// Apply Farsi Language Translation
	public void applyLanguageFarsi() {
	    if (questionLabel != null)
	        questionLabel.setText("کدام سیاره به 'سیاره سرخ' معروف است؟");

	    earnTitle.setText("دارایی:");
	    timeLabel.setText("۲۴ ثانیه");

	    root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
	}
	
    //---------------------------------------------------------------
    // Design Mode Switch-over
    //---------------------------------------------------------------	
	
	//---------------------------------------------------------------
	// Build a Design Mode Panel (no more board)
	private Node buildDesignModePanelContent() {

	    SwingNode swingNode = new SwingNode();

	    SwingUtilities.invokeLater(() -> {
	        JPanel panel = new JPanel();
	        panel.setPreferredSize(new java.awt.Dimension(900, 700));
	        panel.add(new JLabel("Design Mode (empty panel for now)"));
	        swingNode.setContent(panel);
	    });

	    VBox container = new VBox(20);
	    container.setAlignment(Pos.CENTER);
	    container.setMaxWidth(900);
	    container.setMaxHeight(700);

	    container.getChildren().add(swingNode);

	    return container;
	}



	//---------------------------------------------------------------
	// Make the panel visible (show it)
	public void showDesignModePanel() {

	    updatePlayModeVisibilities(false);						// Switch visibility to Design Mode
	   
	    designModeContent.getChildren().clear();				// Clear old content
	    
	    Node designPanel = buildDesignModePanelContent();		// Build the Swing panel
	    
	    designModeContent.getChildren().add(designPanel);		// Add it to the design mode container
	}

	//---------------------------------------------------------------
	// Exits design mode. --- Not sure if work right
	private void exitDesignMode() {
	   
	    designModeContent.getChildren().clear();				// Clear design mode UI

	    updatePlayModeVisibilities(true);						// Switch back to play mode

	    showPauseMenu();										// Show pause menu again (still paused)
	}

	//---------------------------------------------------------------
	// Return to, or activate, Play Mode
	public void showPlayMode() { updatePlayModeVisibilities(true); }

}
