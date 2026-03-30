package com.allaymc.exile.data;

import com.allaymc.exile.AllayMcPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CaseHistoryManager {
    private final AllayMcPlugin plugin;
    private final File dir;

    public CaseHistoryManager(AllayMcPlugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "history");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void appendCase(UUID playerUuid, String playerName, String caseId, String status) {
        File file = new File(dir, playerUuid.toString() + ".yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        yml.set("playerName", playerName);
        int total = yml.getInt("totalExiles", 0) + 1;
        yml.set("totalExiles", total);

        String path = "cases." + caseId;
        yml.set(path + ".status", status);
        yml.set(path + ".timestamp", System.currentTimeMillis());

        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save history for " + playerName);
        }
    }

    public void updateCase(UUID playerUuid, String caseId, String status) {
        File file = new File(dir, playerUuid.toString() + ".yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        String path = "cases." + caseId;
        yml.set(path + ".status", status);
        yml.set(path + ".updated", System.currentTimeMillis());

        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to update history for case " + caseId);
        }
    }
}
