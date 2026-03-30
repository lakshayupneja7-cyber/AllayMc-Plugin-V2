package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.BorderStateManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BorderService {
    private final AllayMcPlugin plugin;
    private final BorderStateManager borderStateManager;

    public BorderService(AllayMcPlugin plugin, BorderStateManager borderStateManager) {
        this.plugin = plugin;
        this.borderStateManager = borderStateManager;
    }

    public boolean isOverworld(Location location) {
        if (location == null || location.getWorld() == null) return false;
        String world = plugin.getConfig().getString("borders.overworld.world", "world");
        return location.getWorld().getName().equalsIgnoreCase(world);
    }

    public boolean isInsideMainBorder(Location location) {
        if (!isOverworld(location)) return false;

        double half = borderStateManager.getOverworldMainSize() / 2.0;
        double cx = plugin.getConfig().getDouble("borders.overworld.center-x", 0.0);
        double cz = plugin.getConfig().getDouble("borders.overworld.center-z", 0.0);

        return Math.abs(location.getX() - cx) <= half &&
                Math.abs(location.getZ() - cz) <= half;
    }

    public boolean isOutsideExileBorder(Location location) {
        if (!isOverworld(location)) return false;

        double threshold = plugin.getConfig().getDouble("borders.overworld.exile-threshold-blocks", 2000000.0);

        return Math.abs(location.getX()) >= threshold &&
                Math.abs(location.getZ()) >= threshold;
    }

    public boolean isDangerZone(Location location) {
        if (!isOverworld(location)) return false;
        return !isInsideMainBorder(location) && !isOutsideExileBorder(location);
    }

    public void showMainBorderHit(Player player, Location attempted) {
        showBarrierFeedback(player, attempted, plugin.getConfig().getString(
                "barrier.main-message",
                "&cA mysterious force blocks you at the server border."
        ), true);
    }

    public void showExileBorderHit(Player player, Location attempted, boolean exiled) {
        String msg = exiled
                ? plugin.getConfig().getString("barrier.exile-message-exiled", "&cThe exile border rejects your return.")
                : plugin.getConfig().getString("barrier.exile-message-normal", "&cThe exile lands reject you.");
        showBarrierFeedback(player, attempted, msg, false);
    }

    public void applyKnockback(Player player, Location from, Location to) {
        if (!plugin.getConfig().getBoolean("barrier.knockback", true)) return;

        Vector push = from.toVector().subtract(to.toVector());
        if (push.lengthSquared() == 0) return;

        push.normalize().multiply(plugin.getConfig().getDouble("danger-zone.pushback-strength", 1.55));
        push.setY(0.15);
        player.setVelocity(push);
    }

    private void showBarrierFeedback(Player player, Location attempted, String message, boolean main) {
        player.sendMessage(plugin.getMessageUtil().color(message));

        if (plugin.getConfig().getBoolean("barrier.sounds", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.9f, 1.5f);
        }

        if (plugin.getConfig().getBoolean("barrier.particles", true)) {
            World world = player.getWorld();
            Particle particle = main ? Particle.END_ROD : Particle.ELECTRIC_SPARK;
            double yBase = player.getLocation().getY();
            for (double y = yBase - 0.5; y <= yBase + 2.5; y += 0.35) {
                world.spawnParticle(particle, attempted.getX(), y, attempted.getZ(), 8, 0.15, 0.05, 0.15, 0.0);
            }
        }
    }
}
