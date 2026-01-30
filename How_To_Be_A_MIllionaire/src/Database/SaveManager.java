package Database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import Pack_1.Question;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class SaveManager {

    private static final String SAVE_FILE = "SaveFile.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Boolean> usedMap = new LinkedHashMap<>();

    private final List<Question> allQuestions;

    public SaveManager(List<Question> allQuestions) {
        this.allQuestions = allQuestions;
        load();
        validateTiers();
        save();
    }

    // ------------------------------------------------------------
    // LOAD / SAVE
    // ------------------------------------------------------------

    private void load() {
        File file = new File(SAVE_FILE);

        if (file.exists()) {
            try {
                Map<String, Boolean> loaded =
                        mapper.readValue(file, new TypeReference<LinkedHashMap<String, Boolean>>() {});
                usedMap.putAll(loaded);
            } catch (Exception e) {
                System.out.println("Save file corrupted. Rebuilding...");
            }
        }

        // Ensure all question IDs exist
        for (Question q : allQuestions) {
            usedMap.putIfAbsent(q.getId(), false);
        }
    }

    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVE_FILE), usedMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------
    // BASIC OPERATIONS
    // ------------------------------------------------------------

    public boolean isUsed(String id) {
        return usedMap.getOrDefault(id, false);
    }

    public void markUsed(String id) {
        usedMap.put(id, true);
        save();
    }

    public void resetAll() {
        usedMap.replaceAll((k, v) -> false);
        save();
    }

    // ------------------------------------------------------------
    // TIER OPERATIONS
    // ------------------------------------------------------------

    public long countUnusedInTier(int tier) {
        String prefix = String.format("T%02d", tier);

        return usedMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .filter(e -> !e.getValue())
                .count();
    }

    public boolean hasUnusedInTier(int tier) {
        return countUnusedInTier(tier) > 0;
    }

    public void resetTier(int tier) {
        String prefix = String.format("T%02d", tier);

        usedMap.replaceAll((id, used) -> {
            if (id.startsWith(prefix)) {
                return false;
            }
            return used;
        });

        save();
    }

    // ------------------------------------------------------------
    // VALIDATION / AUTO-REPAIR
    // ------------------------------------------------------------

    private void validateTiers() {
        for (int tier = 1; tier <= 15; tier++) {
            if (!hasUnusedInTier(tier)) {
                System.out.println("Tier " + tier + " exhausted or corrupted. Resetting tier...");
                resetTier(tier);
            }
        }
    }

    // ------------------------------------------------------------
    // DEBUGGING / UTILITIES
    // ------------------------------------------------------------

    public int countUsedTotal() {
        return (int) usedMap.values().stream().filter(v -> v).count();
    }

    public void printStatus() {
        System.out.println("=== Save File Status ===");
        for (int tier = 1; tier <= 15; tier++) {
            System.out.println("Tier " + tier + ": " + countUnusedInTier(tier) + " unused");
        }
        System.out.println("Total used: " + countUsedTotal());
    }
}
