package com.allaymc.exile.data;

import com.allaymc.exile.AllayMcPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CaseDataManager {
    private final AllayMcPlugin plugin;
    private final File dir;
    private final Map<String, ExileCase> cache = new HashMap<>();

    public CaseDataManager(AllayMcPlugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "cases");
        if (!dir.exists()) dir.mkdirs();
        loadAll();
    }

    public ExileCase getCase(String caseId) {
        return cache.get(caseId.toUpperCase(Locale.ROOT));
    }

    public Collection<ExileCase> getAllCases() {
        return cache.values();
    }

    public void putCase(ExileCase exileCase) {
        cache.put(exileCase.getCaseId().toUpperCase(Locale.ROOT), exileCase);
        saveCase(exileCase);
    }

    public String nextCaseId() {
        int next = cache.size() + 1;
        String caseId;
        do {
            caseId = String.format(Locale.US, "CASE-%04d", next++);
        } while (cache.containsKey(caseId));
        return caseId;
    }

    public void saveCase(ExileCase exileCase) {
        File file = new File(dir, exileCase.getCaseId() + ".yml");
        YamlConfiguration yml = new YamlConfiguration();

        yml.set("caseId", exileCase.getCaseId());
        yml.set("playerUuid", exileCase.getPlayerUuid().toString());
        yml.set("staffUuid", exileCase.getStaffUuid().toString());
        yml.set("reason", exileCase.getReason());
        yml.set("startTime", exileCase.getStartTime());
        yml.set("endTime", exileCase.getEndTime());
        yml.set("graceEndTime", exileCase.getGraceEndTime());
        yml.set("status", exileCase.getStatus().name());
        yml.set("paidItemsClaimed", exileCase.isPaidItemsClaimed());

        yml.set("requiredItems", serializeRequirements(exileCase.getRequiredItems()));
        yml.set("paidItems", serializeRequirements(exileCase.getPaidItems()));

        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save case " + exileCase.getCaseId());
        }
    }

    public void saveAll() {
        for (ExileCase exileCase : cache.values()) {
            saveCase(exileCase);
        }
    }

    private List<Map<String, Object>> serializeRequirements(List<RecoveryRequirement> requirements) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RecoveryRequirement req : requirements) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("material", req.getMaterial().name());
            entry.put("amount", req.getAmount());
            result.add(entry);
        }
        return result;
    }

    private List<RecoveryRequirement> deserializeRequirements(List<Map<?, ?>> maps) {
        List<RecoveryRequirement> requirements = new ArrayList<>();
        for (Map<?, ?> map : maps) {
            try {
                Material material = Material.valueOf(String.valueOf(map.get("material")));
                int amount = Integer.parseInt(String.valueOf(map.get("amount")));
                requirements.add(new RecoveryRequirement(material, amount));
            } catch (Exception ignored) {
            }
        }
        return requirements;
    }

    private void loadAll() {
        cache.clear();

        File[] files = dir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            String caseId = yml.getString("caseId");
            if (caseId == null) continue;

            try {
                ExileCase exileCase = new ExileCase();
                exileCase.setCaseId(caseId);
                exileCase.setPlayerUuid(UUID.fromString(yml.getString("playerUuid")));
                exileCase.setStaffUuid(UUID.fromString(yml.getString("staffUuid")));
                exileCase.setReason(yml.getString("reason", "No reason provided"));
                exileCase.setStartTime(yml.getLong("startTime", 0L));
                exileCase.setEndTime(yml.getLong("endTime", 0L));
                exileCase.setGraceEndTime(yml.getLong("graceEndTime", 0L));
                exileCase.setStatus(ExileCase.Status.valueOf(yml.getString("status", ExileCase.Status.ACTIVE_EXILE.name())));
                exileCase.setPaidItemsClaimed(yml.getBoolean("paidItemsClaimed", false));

                exileCase.setRequiredItems(deserializeRequirements(yml.getMapList("requiredItems")));
                exileCase.setPaidItems(deserializeRequirements(yml.getMapList("paidItems")));

                cache.put(caseId.toUpperCase(Locale.ROOT), exileCase);
            } catch (Exception ignored) {
            }
        }
    }
}
