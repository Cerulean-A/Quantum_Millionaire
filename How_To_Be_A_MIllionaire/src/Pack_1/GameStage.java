package Pack_1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GameStage extends Application {

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    // --- UI FIELDS ---
    private Label questionLabel;
    private Label earnTitle;
    private Label earnValue;
    private Label timeLabel;
    private Label ladderHeader;
    private Label lifelineHeader;
    private Button btnA, btnB, btnC, btnD;
    private StackPane root;

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane();

        // 1. Background Logic
        ImageView backgroundView = new ImageView();
        try {
            Image bgImage = new Image("studio_bg.jpg");
            backgroundView.setImage(bgImage);
            backgroundView.setPreserveRatio(false);

            // Binds the image size to the window size so it stays full-screen when resized
            backgroundView.fitWidthProperty().bind(primaryStage.widthProperty());
            backgroundView.fitHeightProperty().bind(primaryStage.heightProperty());
        } catch (Exception e) {
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #1a0b2e);");
        }

        // 2. Main Layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        mainLayout.setLeft(createMoneyLadder());

        HBox topContainer = new HBox(createTopRightDashboard());
        topContainer.setAlignment(Pos.TOP_RIGHT);
        mainLayout.setTop(topContainer);

        mainLayout.setBottom(createBottomRegion());

        // Background goes first so it stays behind the UI
        root.getChildren().addAll(backgroundView, mainLayout);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Link CSS
        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found. Ensure style.css is in the same folder.");
        }

        // --- ASSIGNMENT REQUIREMENTS: Resizable & Team Name ---
        primaryStage.setTitle("Quantum Millionaire - Team Ria");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ==============================================================================================
    // LOGIC METHODS
    // ==============================================================================================

    private void switchToFarsi() {
        questionLabel.setText("کدام سیاره به 'سیاره سرخ' معروف است؟");
        earnTitle.setText("دارایی:");
        timeLabel.setText("۲۴ ثانیه");
        ladderHeader.setText("ارزش سوال");
        lifelineHeader.setText("کمک‌کننده‌ها");

        btnA.setText("الف: زهره");
        btnB.setText("ب: مریخ");
        btnC.setText("ج: مشتری");
        btnD.setText("د: زحل");

        root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    private void switchToEnglish() {
        questionLabel.setText("Which planet is known as the 'Red Planet'?");
        earnTitle.setText("EARNINGS:");
        timeLabel.setText("24 SEC");
        ladderHeader.setText("Question Value");
        lifelineHeader.setText("LIFELINES");

        btnA.setText("A: Venus");
        btnB.setText("B: Mars");
        btnC.setText("C: Jupiter");
        btnD.setText("D: Saturn");

        root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    }

    private void applyTheme(String themeClass) {
        root.getStyleClass().removeAll("theme-deuteranopia", "theme-tritanopia");
        if (!themeClass.equals("default")) {
            root.getStyleClass().add(themeClass);
        }
    }

    private void applyLookAndFeel(String styleClass) {
        root.getStyleClass().removeAll("modern-style", "classic-style");
        if (!styleClass.equals("default")) {
            root.getStyleClass().add(styleClass);
        }
    }

    // ==============================================================================================
    // REGION BUILDERS
    // ==============================================================================================

    private VBox createMoneyLadder() {
        VBox ladder = new VBox(2);
        ladder.setAlignment(Pos.CENTER_LEFT);
        ladder.getStyleClass().add("ladder-container");

        ladderHeader = new Label("Question Value");
        ladderHeader.setTextFill(Color.WHITE);
        ladderHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ladder.getChildren().add(ladderHeader);

        String[] values = {"$1,000,000", "$500,000", "$300,000", "$200,000", "$10,000", "$5,000", "$2,000", "$1,000", "$500", "$100"};
        for (String val : values) {
            Label lbl = new Label(val);
            lbl.setPrefWidth(180);
            lbl.setAlignment(Pos.CENTER);
            lbl.getStyleClass().add("ladder-cell");
            if (val.equals("$2,000")) lbl.setId("current-level");
            ladder.getChildren().add(lbl);
        }
        return ladder;
    }

    private VBox createTopRightDashboard() {
        VBox dashboard = new VBox(10);
        dashboard.setAlignment(Pos.TOP_RIGHT);

        VBox earningsBox = new VBox();
        earningsBox.setAlignment(Pos.CENTER);
        earningsBox.getStyleClass().add("dashboard-box");

        earnTitle = new Label("EARNINGS:");
        earnTitle.setTextFill(Color.WHITE);
        earnValue = new Label("$1,000");
        earnValue.setStyle("-fx-font-size: 36px; -fx-text-fill: gold; -fx-font-weight: bold;");
        earningsBox.getChildren().addAll(earnTitle, earnValue);

        HBox timerBox = new HBox();
        timerBox.setAlignment(Pos.CENTER_RIGHT);
        timerBox.getStyleClass().add("timer-box");
        timeLabel = new Label("24 SEC");
        timeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #ffcc00; -fx-font-weight: bold;");
        timerBox.getChildren().add(timeLabel);

        dashboard.getChildren().addAll(earningsBox, timerBox);
        return dashboard;
    }

    private BorderPane createBottomRegion() {
        BorderPane bottomLayout = new BorderPane();
        bottomLayout.setPadding(new Insets(0, 20, 20, 20));

        bottomLayout.setLeft(createLifelinePanel());
        bottomLayout.setCenter(new StackPane(createGameBoard()));

        StackPane menuButton = createMenuDiamond();
        BorderPane.setAlignment(menuButton, Pos.BOTTOM_RIGHT);
        bottomLayout.setRight(menuButton);

        return bottomLayout;
    }

    private VBox createLifelinePanel() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.getStyleClass().add("lifeline-panel");

        lifelineHeader = new Label("LIFELINES");
        lifelineHeader.setTextFill(Color.WHITE);
        lifelineHeader.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        HBox icons = new HBox(8);
        icons.setAlignment(Pos.CENTER);
        Button l1 = new Button("50:50");
        Button l2 = new Button("📞");
        Button l3 = new Button("👥");
        l1.getStyleClass().add("lifeline-btn");
        l2.getStyleClass().add("lifeline-btn");
        l3.getStyleClass().add("lifeline-btn");

        icons.getChildren().addAll(l1, l2, l3);
        box.getChildren().addAll(lifelineHeader, icons);
        return box;
    }

    private VBox createGameBoard() {
        VBox board = new VBox(10);
        board.setAlignment(Pos.BOTTOM_CENTER);

        questionLabel = new Label("Which planet is known as the 'Red Planet'?");
        questionLabel.getStyleClass().add("question-box");
        questionLabel.setWrapText(true);
        questionLabel.setAlignment(Pos.CENTER);

        GridPane answers = new GridPane();
        answers.setHgap(15);
        answers.setVgap(10);
        answers.setAlignment(Pos.CENTER);

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

    private StackPane createMenuDiamond() {
        StackPane diamondContainer = new StackPane();
        diamondContainer.setAlignment(Pos.CENTER);

        Button menuBtn = new Button();
        menuBtn.getStyleClass().add("menu-diamond");

        ContextMenu mainMenu = new ContextMenu();

        // 1. Language
        Menu langMenu = new Menu("Language / زبان");
        MenuItem engItem = new MenuItem("English");
        MenuItem farItem = new MenuItem("فارسی");
        engItem.setOnAction(e -> switchToEnglish());
        farItem.setOnAction(e -> switchToFarsi());
        langMenu.getItems().addAll(engItem, farItem);

        // 2. Accessibility (Colors)
        Menu colorMenu = new Menu("Colors");
        MenuItem defColor = new MenuItem("Default");
        MenuItem deutColor = new MenuItem("Deuteranopia");
        MenuItem tritColor = new MenuItem("Tritanopia");
        defColor.setOnAction(e -> applyTheme("default"));
        deutColor.setOnAction(e -> applyTheme("theme-deuteranopia"));
        tritColor.setOnAction(e -> applyTheme("theme-tritanopia"));
        colorMenu.getItems().addAll(defColor, deutColor, tritColor);

        // 3. Look and Feel
        Menu lnfMenu = new Menu("Look and Feel");
        MenuItem modernItem = new MenuItem("Quantum Modern");
        MenuItem classicItem = new MenuItem("Classic TV Show");
        modernItem.setOnAction(e -> applyLookAndFeel("modern-style"));
        classicItem.setOnAction(e -> applyLookAndFeel("classic-style"));
        lnfMenu.getItems().addAll(modernItem, classicItem);

        mainMenu.getItems().addAll(langMenu, colorMenu, lnfMenu);

        // Position menu above the button
        menuBtn.setOnAction(e -> mainMenu.show(menuBtn, Side.TOP, 0, -60));

        Label icon = new Label("☰");
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        icon.setMouseTransparent(true);

        diamondContainer.getChildren().addAll(menuBtn, icon);
        return diamondContainer;
    }

    private Button createAnswerBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("answer-btn");
        btn.setPrefWidth(400);
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}