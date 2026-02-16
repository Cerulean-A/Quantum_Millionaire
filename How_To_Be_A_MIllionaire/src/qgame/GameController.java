package qgame;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * GameController
 *
 * This controller is written to be usable programmatically (new GameController())
 * as well as via FXML if you later choose to reintroduce FXML.
 *
 * Important wiring notes:
 * - If you create this controller programmatically, call setLadderContainer(...) before calling initialize()
 *   (or call initialize() and then finishInitAfterInjection() after injecting the container).
 * - If you use FXML, the loader will inject fields and call initialize() automatically.
 *
 * The controller creates the ladder VBox (myLadderVBox) using MoneyLadderFactory and
 * either attaches it to ladderContainer (if present) or defers attaching until finishInitAfterInjection().
 *
 * Theme state (cssTheme) and ladder asset switching live here. GameStage should delegate theme changes
 * to controller.applyTheme(...) so CSS + assets remain in sync.
 */
public class GameController {

    // Constructor accessor for the ladder VBox so other classes can query it
    public VBox getLadderVBox() {
        return myLadderVBox;
    }

    // UI container in your layout where ladder will be added.
    // Previously annotated with @FXML; for programmatic usage we provide a public setter.
    // If you later use FXML, the loader will still inject this field if you re-add @FXML.
    private Pane ladderContainer; // a placeholder Pane in your layout where ladder will be added

    /**
     * Non-FXML setter for programmatic injection.
     * Call this from GameStage before initialize() to ensure the ladder is attached immediately.
     */
    public void setLadderContainer(Pane ladderContainer) {
        this.ladderContainer = ladderContainer;
    }

    // Rung Fields
    private double rungWidth = 240;
    private double rungHeight = 40;

    // Keep a reference to the ladder VBox so you can call setActive/setFinished/setScheme later
    private VBox myLadderVBox;

    // Track current CSS theme name (string used by your applyTheme logic)
    private String cssTheme = "default"; // default at startup

    // Example menu (you may build this in FXML instead)
    private Menu colorsMenu;

    // Images folder (adjust to your project)
    // Make this assignable so GameStage can override the path if needed
    private Path imagesDir = Path.of("C:/Users/Taylor/git/Quantum_Millionaire_Local/How_To_Be_A_MIllionaire/Resources/sprites/money_ladder");

    // Pearl variant mapping (index -> variant token)
    private final Map<Integer, String> pearlMap = new HashMap<>();

    /**
     * Set the design size for ladder rungs.
     *
     * <p>Call this method before {@code initialize()} (or before the controller
     * creates the ladder) to ensure the factory and ImageViews use the requested
     * design dimensions. Values are interpreted as design pixels and will be
     * combined with the uiLayer scale factor to produce final on-screen sizes.</p>
     *
     * @param w the desired rung width in design pixels (must be &gt; 0)
     * @param h the desired rung height in design pixels (must be &gt; 0)
     * @throws IllegalArgumentException if {@code w <= 0} or {@code h <= 0}
     * @since 1.0
     */
    public void setRungSize(double w, double h) {
        if (w <= 0 || h <= 0) throw new IllegalArgumentException("rung size must be positive");
        this.rungWidth = w;
        this.rungHeight = h;
    }
    
    /**
     * Allow GameStage to set the imagesDir before initialize() runs.
     * If null is passed, the default path remains in use.
     */
    public void setImagesDir(Path imagesDir) {
        if (imagesDir != null) this.imagesDir = imagesDir;
    }

    /**
     * Programmatic initialization entrypoint.
     * If you call initialize() directly, ensure setLadderContainer(...) has been called first,
     * or call finishInitAfterInjection() after injecting the container.
     */
    public void initProgrammatic() {
        initialize();
    }

    /**
     * initialize
     *
     * Creates the ladder VBox and attempts to attach it to ladderContainer.
     * If ladderContainer is null, the method defers attaching but still builds the Colors menu
     * and preloads images so menu handlers exist.
     *
     * This method is safe to call whether ladderContainer is set or not.
     */
    public void initialize() {
        // Example pearl variants
        pearlMap.put(5, "M");
        pearlMap.put(10, "F");
        pearlMap.put(15, "W");

        // Create the ladder and keep the reference
        try {
            myLadderVBox = MoneyLadderFactory.createMoneyLadder("PRISMA", imagesDir, rungWidth, rungHeight, pearlMap);
        } catch (Exception ex) {
            System.err.println("MoneyLadderFactory.createMoneyLadder threw: " + ex);
            myLadderVBox = null;
        }

        if (myLadderVBox == null) {
            System.err.println("GameController.initialize: MoneyLadderFactory returned null or failed.");
        } else {
            System.out.println("GameController.initialize: created myLadderVBox children=" + myLadderVBox.getChildren().size());
        }

        // Guard against null container: if null, defer attaching but still build menu and preload images
        if (ladderContainer == null) {
            System.err.println("GameController.initialize: ladderContainer is null — deferring adding ladder.");
            // Build menu so handlers exist even if ladder isn't attached yet
            buildColorsMenu();
            // Preload images for default scheme to reduce lag later
            MoneyLadderFactory.preloadAllImages("PRISMA", imagesDir, pearlMap);
            return;
        }

        // Add ladder to your container
        ladderContainer.getChildren().clear();
        if (myLadderVBox != null) {
            ladderContainer.getChildren().add(myLadderVBox);
        }

        // Preload images for the default scheme to avoid lag
        MoneyLadderFactory.preloadAllImages("PRISMA", imagesDir, pearlMap);

        // Build and wire the Colors menu
        buildColorsMenu();
    }

    /**
     * finishInitAfterInjection
     *
     * Call this from GameStage after you inject the ladderContainer if initialize() ran earlier.
     * It will attach the ladder VBox to the injected container if it hasn't been attached yet.
     */
    public void finishInitAfterInjection() {
        if (ladderContainer != null && myLadderVBox != null && !ladderContainer.getChildren().contains(myLadderVBox)) {
            ladderContainer.getChildren().clear();
            ladderContainer.getChildren().add(myLadderVBox);
            System.out.println("GameController.finishInitAfterInjection: ladder attached to container.");
        }
    }

    /**
     * buildColorsMenu
     *
     * Builds the Colors menu and wires handlers to applyTheme.
     * Made public and idempotent so GameStage can call getColorsMenu() and insert it into its ContextMenu.
     */
    public void buildColorsMenu() {
        if (colorsMenu != null) return; // already built

        colorsMenu = new Menu("Colors");
        MenuItem dC = new MenuItem("Default");
        MenuItem deutC = new MenuItem("Deuteranopia");
        MenuItem tritC = new MenuItem("Tritanopia");

        dC.setOnAction(e -> applyTheme("default"));
        deutC.setOnAction(e -> applyTheme("theme-deuteranopia"));
        tritC.setOnAction(e -> applyTheme("theme-tritanopia"));

        colorsMenu.getItems().addAll(dC, deutC, tritC);

        // Add this menu to your MenuBar (not shown) or otherwise expose it in the UI
    }

    /**
     * getColorsMenu
     *
     * Returns the Colors menu, building it lazily if necessary.
     * Use this from GameStage to insert the menu into the diamond ContextMenu.
     */
    public Menu getColorsMenu() {
        if (colorsMenu == null) buildColorsMenu();
        return colorsMenu;
    }

    /**
     * applyTheme
     *
     * Called when user selects a theme from the menu (or at startup).
     * Public so GameStage can delegate to it.
     */
    public void applyTheme(String newCssTheme) {
        this.cssTheme = newCssTheme;

        // 1) Apply CSS (your existing logic)
        // Example: scene.getStylesheets().clear(); scene.getStylesheets().add(cssFor(cssTheme));
        // (You already have applyTheme logic for CSS; keep that here or call back to GameStage if needed.)

        // 2) Map CSS theme to asset scheme and switch ladder assets
        String scheme = schemeForCssTheme(cssTheme);
        if (myLadderVBox != null) {
            MoneyLadderFactory.setScheme(myLadderVBox, scheme);

            // 3) Optionally preload images for the new scheme
            MoneyLadderFactory.preloadAllImages(scheme, imagesDir, pearlMap);
        } else {
            System.err.println("applyTheme: myLadderVBox is null — cannot set scheme yet.");
        }
    }

    /**
     * Map CSS theme names to asset scheme names
     */
    private String schemeForCssTheme(String cssTheme) {
        return switch (cssTheme) {
            case "theme-deuteranopia" -> "DEUTER";
            case "theme-tritanopia" -> "TRITAN";
            default -> "PRISMA";
        };
    }

    // Example game-flow calls (call these from your game logic)
    public void markRungActive(int index) {
        MoneyLadderFactory.setActive(myLadderVBox, index);
    }

    public void markRungFinished(int index) {
        MoneyLadderFactory.setFinished(myLadderVBox, index);
    }

    public void resetLadder() {
        MoneyLadderFactory.resetAll(myLadderVBox);
    }
}
