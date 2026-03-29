package com.allaymc.exile.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationUtil {
    private LocationUtil() {}

    public static String serialize(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
    }

    public static Location deserialize(String str) {
        try {
            if (str == null || str.isEmpty()) return null;
            String[] parts = str.split(";");
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]),
                    Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
        } catch (Exception ignored) {
            return null;
        }
    }
}
