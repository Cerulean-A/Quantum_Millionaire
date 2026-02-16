package main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class RungExporter {

    // === CONFIG ===
    static final Path TEMPLATES_DIR = Path.of("C:/Users/Taylor/git/Quantum_Millionaire_Local/How_To_Be_A_MIllionaire/Resources/sprites/money_ladder/Templates");
    static final Path OUTPUT_ROOT = Path.of("C:/Users/Taylor/git/Quantum_Millionaire_Local/How_To_Be_A_MIllionaire/Resources/sprites/money_ladder/Old");
    static final String INKSCAPE_CMD = "C:/Program Files/Inkscape/bin/inkscape.exe"; // or full path to inkscape.exe

    // NUM = 1..15
    static final int RUNG_COUNT = 15;

    // TEXT labels in order (index 1..15). Replace these with your actual labels.
    static final List<String> TEXT_LABELS = List.of(
        "100", "200", "300", "500", "1,000",
        "2,000", "4,000", "8,000", "16,000",
        "32,000", "64,000", "125,000", "250,000",
        "500,000", "1 MILLION"
    );

    // Colour schemes: PRISMA is default
    static final List<String> SCHEMES = List.of("PRISMA", "DEUTER", "TRITAN");

    // States you use in templates (use exact strings that appear in filenames after scheme)
    static final List<String> STATES = List.of("Resting", "Active", "Tier", "Finished");

    // Pearl indices (1-based)
    static final Set<Integer> PEARL_INDICES = Set.of(5, 10, 15);

    // Export widths (pixels). Keep one value if you only want one resolution.
    static final List<Integer> EXPORT_WIDTHS = List.of(1170); // change/add 1170 for 3x if desired

    // === END CONFIG ===

    public static void main(String[] args) throws Exception {
        if (TEXT_LABELS.size() != RUNG_COUNT) {
            System.err.println("TEXT_LABELS must contain exactly " + RUNG_COUNT + " entries.");
            return;
        }

        Files.createDirectories(OUTPUT_ROOT);

        for (String scheme : SCHEMES) {
            // create scheme output folder
            Path schemeDir = OUTPUT_ROOT.resolve(scheme);
            Files.createDirectories(schemeDir);

            // discover pearl templates for this scheme (any file matching Rung_<SCHEME>_Pearl_*.svg)
            List<Path> pearlTemplates = Files.exists(TEMPLATES_DIR)
                ? Files.list(TEMPLATES_DIR)
                    .filter(p -> {
                        String n = p.getFileName().toString();
                        return n.startsWith("Rung_" + scheme + "_Pearl_") && n.endsWith("_TMPLT.svg");
                    })
                    .sorted()
                    .collect(Collectors.toList())
                : List.of();

            // For each rung index
            for (int i = 0; i < RUNG_COUNT; i++) {
                int num = i + 1; // NUM
                String text = TEXT_LABELS.get(i); // TEXT

                // Determine state template to use for non-pearl rungs.
                // You must have templates named like: Rung_<SCHEME>_<STATE>_TMPLT.svg
                // Prefer Tier template for Tier state, etc.
                // For this exporter we will export one file per state per rung (if you want only one state per rung, adjust accordingly).
                for (String state : STATES) {

                    // Choose template path
                	Path baseTemplate = TEMPLATES_DIR.resolve("Rung_" + scheme + "_" + state + "_TMPLT.svg");
                    if (!Files.exists(baseTemplate)) {
                        // skip if template missing for this state
                        System.out.println("Skipping missing template: " + baseTemplate);
                        continue;
                    }

                    Path chosenTemplate = baseTemplate;
                 // Assume:
                 // int num = rungIndex; // 1-based
                 // Path baseTemplate = TEMPLATES_DIR.resolve("Rung_" + scheme + "_" + state + "_TMPLT.svg");
                 // List<Path> pearlTemplates = ... // discovered and sorted for this scheme (expected size 3)
                 // Path chosenTemplate = baseTemplate; // default

                 // Only apply Tier/Pearl logic for indices 5,10,15
                    if (PEARL_INDICES.contains(num)) {

                        // Resting visuals at pearl indices use Tier template if available
                        if (state.equalsIgnoreCase("Resting") || state.equalsIgnoreCase("Tier")) {
                            Path tierTemplate = TEMPLATES_DIR.resolve("Rung_" + scheme + "_Tier_TMPLT.svg");
                            chosenTemplate = Files.exists(tierTemplate) ? tierTemplate : baseTemplate;
                        }

                        // Active visuals at pearl indices use the Pearl template (explicit mapping preferred)
                        if (state.equalsIgnoreCase("Active")) {
                            // explicit mapping by suffix: F -> 5, M -> 10, W -> 15
                            Map<Integer, String> explicitMap = Map.of(
                                5, "Rung_" + scheme + "_Pearl_F_TMPLT.svg",
                                10, "Rung_" + scheme + "_Pearl_M_TMPLT.svg",
                                15, "Rung_" + scheme + "_Pearl_W_TMPLT.svg"
                            );

                            String explicitName = explicitMap.get(num);
                            if (explicitName != null) {
                                Path explicitPearl = TEMPLATES_DIR.resolve(explicitName);
                                if (Files.exists(explicitPearl)) {
                                    chosenTemplate = explicitPearl;
                                } else if (pearlTemplates.size() == 3) {
                                    // ordered mapping fallback: 5->0, 10->1, 15->2
                                    int slot = (num == 5) ? 0 : (num == 10) ? 1 : 2;
                                    chosenTemplate = pearlTemplates.get(slot);
                                } else if (!pearlTemplates.isEmpty()) {
                                    chosenTemplate = pearlTemplates.get(0);
                                } else {
                                    // no pearl files: fall back to Tier template if available, else base
                                    Path tierTemplate = TEMPLATES_DIR.resolve("Rung_" + scheme + "_Tier_TMPLT.svg");
                                    chosenTemplate = Files.exists(tierTemplate) ? tierTemplate : baseTemplate;
                                }
                            } else {
                                // no explicit mapping entry found: use ordered mapping or fallback
                                if (pearlTemplates.size() == 3) {
                                    int slot = (num == 5) ? 0 : (num == 10) ? 1 : 2;
                                    chosenTemplate = pearlTemplates.get(slot);
                                } else if (!pearlTemplates.isEmpty()) {
                                    chosenTemplate = pearlTemplates.get(0);
                                } else {
                                    Path tierTemplate = TEMPLATES_DIR.resolve("Rung_" + scheme + "_Tier_TMPLT.svg");
                                    chosenTemplate = Files.exists(tierTemplate) ? tierTemplate : baseTemplate;
                                }
                            }
                        }

                    } else {
                        // Non-pearl indices: use the normal base template
                        chosenTemplate = baseTemplate;
                    }


                    // Read template and replace placeholders
                    String svg = Files.readString(chosenTemplate, StandardCharsets.UTF_8);
                    svg = svg.replace("{{NUM}}", xmlEscape(String.valueOf(num)));
                    svg = svg.replace("{{TEXT}}", xmlEscape(text));

                    // Save temporary SVG
                    String tempName = String.format("%s_%s_%d_temp.svg", scheme, state, num);
                    Path tempSvg = schemeDir.resolve(tempName);
                    Files.writeString(tempSvg, svg, StandardCharsets.UTF_8);

                    // Export PNG(s)
                    for (int width : EXPORT_WIDTHS) {
                        String outFileName = String.format("rung_%s_%s_%d.png", scheme, state, num);
                        Path outPng = schemeDir.resolve(outFileName);

                        List<String> cmd = new ArrayList<>();
                        cmd.add(INKSCAPE_CMD);
                        cmd.add(tempSvg.toString());
                        cmd.add("--export-type=png");
                        cmd.add("--export-width=" + width);
                        cmd.add("--export-filename=" + outPng.toString());

                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        pb.redirectErrorStream(true);
                        Process p = pb.start();

                        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = r.readLine()) != null) {
                                System.out.println(line);
                            }
                        }

                        int exit = p.waitFor();
                        if (exit != 0) {
                            System.err.println("Inkscape failed for " + tempSvg + " (exit " + exit + ")");
                        } else {
                            System.out.println("Exported " + outPng);
                        }
                    }

                    // cleanup temp svg
                    Files.deleteIfExists(tempSvg);
                } // end states loop
            } // end rung loop
        } // end scheme loop

        System.out.println("All exports complete.");
    }

    static String xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
