package com.allaymc.exile.data;

import com.allaymc.exile.AllayMcPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BorderStateManager {
    private final AllayMcPlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public BorderStateManager(AllayMcPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "borders.yml");
        if (!file.exists()) plugin.saveResource("borders.yml", false);
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public double getOverworldMainSize() { return config.getDouble("border-state.overworld-main-size", plugin.getConfig().getDouble("borders.overworld.main-size-blocks", 6400.0)); }
    public void setOverworldMainSize(double size) { config.set("border-state.overworld-main-size", size); save(); }
    public double getNetherMainSize() { return config.getDouble("border-state.nether-main-size", plugin.getConfig().getDouble("borders.nether.main-size-blocks", 800.0)); }
    public void setNetherMainSize(double size) { config.set("border-state.nether-main-size", size); save(); }

    private void save() { try { config.save(file); } catch (IOException e) { plugin.getLogger().severe("Failed to save borders.yml"); } }
}
