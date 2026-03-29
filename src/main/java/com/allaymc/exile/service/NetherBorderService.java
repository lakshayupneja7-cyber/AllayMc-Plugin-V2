package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.BorderStateManager;
import org.bukkit.Location;

public class NetherBorderService {
    private final AllayMcPlugin plugin;
    private final BorderStateManager borderStateManager;

    public NetherBorderService(AllayMcPlugin plugin, BorderStateManager borderStateManager) {
        this.plugin = plugin;
        this.borderStateManager = borderStateManager;
    }

    public boolean isInsideMainBorder(Location location) {
        if (location == null || location.getWorld() == null) return false;
        String world = plugin.getConfig().getString("borders.nether.world", "world_nether");
        if (!location.getWorld().getName().equalsIgnoreCase(world)) return false;
        double half = borderStateManager.getNetherMainSize() / 2.0;
        double cx = plugin.getConfig().getDouble("borders.nether.center-x", 0.0);
        double cz = plugin.getConfig().getDouble("borders.nether.center-z", 0.0);
        return Math.abs(location.getX() - cx) <= half && Math.abs(location.getZ() - cz) <= half;
    }

    public boolean isOutsideExileBorder(Location location) {
        if (location == null || location.getWorld() == null) return false;
        String world = plugin.getConfig().getString("borders.nether.world", "world_nether");
        if (!location.getWorld().getName().equalsIgnoreCase(world)) return false;
        double threshold = plugin.getConfig().getDouble("borders.nether.exile-threshold-blocks", 250000.0);
        return Math.abs(location.getX()) >= threshold && Math.abs(location.getZ()) >= threshold;
    }

    public boolean isDangerZone(Location location) {
        return !isInsideMainBorder(location) && !isOutsideExileBorder(location);
    }
}
