package qgame;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MoneyLadderFactory
 *
 * - State names are capitalized single-cap words to match filenames: Resting, Active, Finished, Tier, Pearl
 * - Filenames tried (examples):
 *     Rung_PRISMA_Resting_1.png
 *     Rung_DEUTER_Pearl_W_6.png
 *     Rung_DEUTER_Tier_5.svg
 * - Supports PNG and SVG (tries .png then .svg)
 * - Supports per-index pearl variants via pearlVariantMap (index -> variant string)
 * - setScheme(...) switches asset scheme at runtime (for colorblind theme toggles)
 */
public class MoneyLadderFactory {

    public enum State { Resting, Active, Finished, Tier, Pearl }

    // Simple thread-safe image cache keyed by filename
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    // Public Rung model
    public static class Rung {
        public final int index;          // 1..15
        public State state;
        public final ImageView view;
        private String scheme;           // mutable so setScheme can update it
        private final Path imagesDir;
        private String pearlVariant;     // optional variant for Pearl state
        private final double fitWidth;
        private final double fitHeight;

        public Rung(int index, String scheme, Path imagesDir, double width, double height, String pearlVariant) {
            this.index = index;
            this.scheme = scheme;
            this.imagesDir = imagesDir;
            this.pearlVariant = pearlVariant;
            this.fitWidth = width;
            this.fitHeight = height;
            this.view = new ImageView();
            this.view.setFitWidth(width);
            this.view.setFitHeight(height);
            this.view.setPreserveRatio(true);
            this.view.setSmooth(true);  // These final two for downscaling
            this.view.setCache(true);     

            this.state = isMilestone() ? State.Tier : State.Resting;
            updateImage();
        }

        public void setPearlVariant(String variant) {
            this.pearlVariant = variant;
        }

        public void setScheme(String newScheme) {
            this.scheme = newScheme;
            updateImage();
        }

        public boolean isMilestone() {
            return (index % 5) == 0;
        }

        // Build candidate filenames in order of preference
        private List<String> candidateFilenames(State s) {
            List<String> list = new ArrayList<>();
            String idx = Integer.toString(index);

            switch (s) {
                case Pearl:
                    if (pearlVariant != null && !pearlVariant.isEmpty()) {
                        // exact pattern: Rung_<SCHEME>_Pearl_<Variant>_<Index>.<ext>
                        list.add(String.format("Rung_%s_Pearl_%s_%s", scheme, pearlVariant, idx));
                    }
                    // fallback: Rung_<SCHEME>_Pearl_<Index>.<ext>
                    list.add(String.format("Rung_%s_Pearl_%s", scheme, idx));
                    break;
                case Tier:
                    list.add(String.format("Rung_%s_Tier_%s", scheme, idx));
                    break;
                case Active:
                    list.add(String.format("Rung_%s_Active_%s", scheme, idx));
                    break;
                case Finished:
                    list.add(String.format("Rung_%s_Finished_%s", scheme, idx));
                    break;
                case Resting:
                default:
                    list.add(String.format("Rung_%s_Resting_%s", scheme, idx));
                    break;
            }

            // Generic fallback (in case of slight naming differences)
            list.add(String.format("Rung_%s_%s_%s", scheme, s.name(), idx));
            return list;
        }

        // Try to load image for a state; tries .png then .svg, filesystem then classpath, uses cache
        private Optional<Image> loadImageFor(State s) {
            for (String baseName : candidateFilenames(s)) {
                // try .png then .svg
                for (String ext : List.of(".png", ".svg")) {
                    String filename = baseName + ext;
                    // cache check
                    Image cached = IMAGE_CACHE.get(filename);
                    if (cached != null) return Optional.of(cached);

                    // filesystem
                    if (imagesDir != null) {
                        Path p = imagesDir.resolve(filename);
                        if (Files.exists(p)) {
                            try (InputStream is = Files.newInputStream(p)) {
                                Image img = new Image(is, (int)fitWidth, (int)fitHeight, true, true);
                                IMAGE_CACHE.put(filename, img);
                                return Optional.of(img);
                            } catch (Exception e) {
                                // continue to next candidate
                            }
                        }
                    }

                    // classpath resource fallback
                    InputStream res = getClass().getResourceAsStream("/" + filename);
                    if (res != null) {
                        try (InputStream is = res) {
                            Image img = new Image(is, (int)fitWidth, (int)fitHeight, true, true);
                            IMAGE_CACHE.put(filename, img);
                            return Optional.of(img);
                        } catch (Exception e) {
                            // ignore and continue
                        }
                    }
                }
            }
            return Optional.empty();
        }

        // Public API to change state
        public void setState(State s) {
            this.state = s;
            updateImage();
        }

        // Update the ImageView on the FX thread
        private void updateImage() {
            Optional<Image> img = loadImageFor(state);
            Platform.runLater(() -> view.setImage(img.orElse(null)));
            
            Platform.runLater(() -> {
                view.setImage(img.orElse(null));
                System.out.println("Rung " + index + " image native=" + (view.getImage()==null?"null":view.getImage().getWidth()+"x"+view.getImage().getHeight()) +
                                   " bounds=" + view.getBoundsInParent());
            });

        }
    }

    // Create ladder: scheme (PRISMA/DEUTER/TRITAN), imagesDir, rung size, and pearlVariantMap (index->variant)
    public static VBox createMoneyLadder(String scheme, Path imagesDir, double rungWidth, double rungHeight, Map<Integer,String> pearlVariantMap) {
        VBox ladder = new VBox(0);
        ladder.setAlignment(Pos.CENTER_LEFT);
        ladder.setPadding(new Insets(0));
        ladder.getStyleClass().add("ladder-container");

        // ensure ladder is wide enough for rungs
        ladder.setMinWidth(rungWidth);
        //ladder.setPrefWidth(rungWidth + 8);	// Not needed
        //ladder.setMaxWidth(rungWidth);		// Not needed

        List<Rung> rungs = new ArrayList<>(15);
        for (int i = 15; i >= 1; i--) {
            String variant = pearlVariantMap != null ? pearlVariantMap.get(i) : null;
            Rung r = new Rung(i, scheme, imagesDir, rungWidth, rungHeight, variant);
            rungs.add(r);
            VBox cell = new VBox(r.view);
            cell.setAlignment(Pos.CENTER);
            
            cell.setPrefSize(rungWidth, rungHeight);
            cell.setMinSize(rungWidth, rungHeight);
            cell.setMaxSize(rungWidth, rungHeight);
            cell.setPadding(Insets.EMPTY);
            cell.setSpacing(0);
            cell.getStyleClass().add("ladder-cell");
            ladder.getChildren().add(cell);
        }
        ladder.setUserData(rungs);
        return ladder;
    }

    // Helper to get rungs list from ladder
    @SuppressWarnings("unchecked")
    private static List<Rung> rungsFrom(VBox ladder) {
        return (List<Rung>) ladder.getUserData();
    }

    // Switch scheme at runtime (e.g., when colorblind theme toggled)
    public static void setScheme(VBox ladder, String newScheme) {
        List<Rung> rungs = rungsFrom(ladder);
        if (rungs == null) return;
        for (Rung r : rungs) {
            r.setScheme(newScheme);
        }
    }

    // Set active rung: previous Active/Pearl -> Finished; target -> Active or Pearl if milestone
    public static void setActive(VBox ladder, int index) {
        List<Rung> rungs = rungsFrom(ladder);
        if (rungs == null) return;
        // convert previous active/pearl to finished
        for (Rung r : rungs) {
            if (r.state == State.Active || r.state == State.Pearl) {
                r.setState(State.Finished);
            }
        }
        if (index < 1 || index > rungs.size()) return;
        Rung target = rungs.get(index - 1);
        if (target.isMilestone()) {
            target.setState(State.Pearl);
        } else {
            target.setState(State.Active);
        }
    }

    // Mark rung finished: milestone -> Finished (as requested), normal -> Finished
    public static void setFinished(VBox ladder, int index) {
        List<Rung> rungs = rungsFrom(ladder);
        if (rungs == null) return;
        if (index < 1 || index > rungs.size()) return;
        Rung target = rungs.get(index - 1);
        target.setState(State.Finished);
    }

    // Reset all: milestones -> Tier, others -> Resting
    public static void resetAll(VBox ladder) {
        List<Rung> rungs = rungsFrom(ladder);
        if (rungs == null) return;
        for (Rung r : rungs) {
            r.setState(r.isMilestone() ? State.Tier : State.Resting);
        }
    }

    // Set or change a pearl variant for a specific index at runtime
    public static void setPearlVariant(VBox ladder, int index, String variant) {
        List<Rung> rungs = rungsFrom(ladder);
        if (rungs == null) return;
        if (index < 1 || index > rungs.size()) return;
        Rung r = rungs.get(index - 1);
        r.setPearlVariant(variant);
        if (r.state == State.Pearl) r.updateImage();
    }

    // Optional: preload all images for the current scheme to avoid runtime lag
    public static void preloadAllImages(String scheme, Path imagesDir, Map<Integer,String> pearlVariantMap) {
        for (int i = 1; i <= 15; i++) {
            String idx = Integer.toString(i);
            // build likely filenames and touch cache
            List<String> candidates = new ArrayList<>();
            boolean milestone = (i % 5) == 0;
            if (milestone) {
                // Tier and Pearl candidates
                candidates.add(String.format("Rung_%s_Tier_%s", scheme, idx));
                String variant = pearlVariantMap != null ? pearlVariantMap.get(i) : null;
                if (variant != null && !variant.isEmpty()) candidates.add(String.format("Rung_%s_Pearl_%s_%s", scheme, variant, idx));
                candidates.add(String.format("Rung_%s_Pearl_%s", scheme, idx));
            } else {
                candidates.add(String.format("Rung_%s_Resting_%s", scheme, idx));
                candidates.add(String.format("Rung_%s_Active_%s", scheme, idx));
                candidates.add(String.format("Rung_%s_Finished_%s", scheme, idx));
            }
            for (String base : candidates) {
                for (String ext : List.of(".png", ".svg")) {
                    String filename = base + ext;
                    if (IMAGE_CACHE.containsKey(filename)) continue;
                    // try filesystem
                    if (imagesDir != null) {
                        Path p = imagesDir.resolve(filename);
                        if (Files.exists(p)) {
                            try (InputStream is = Files.newInputStream(p)) {
                                Image img = new Image(is, 240, 40, true, true);
                                IMAGE_CACHE.put(filename, img);
                                break;
                            } catch (Exception e) { /* ignore */ }
                        }
                    }
                    // try classpath
                    InputStream res = MoneyLadderFactory.class.getResourceAsStream("/" + filename);
                    if (res != null) {
                        try (InputStream is = res) {
                            Image img = new Image(is, 240, 40, true, true);
                            IMAGE_CACHE.put(filename, img);
                            break;
                        } catch (Exception e) { /* ignore */ }
                    }
                }
            }
        }
    }
}

