package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileData;
import com.allaymc.exile.util.InventoryUtil;
import com.allaymc.exile.util.LocationUtil;
import com.allaymc.exile.util.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ExileService {
    private final AllayMcPlugin plugin;
    private final com.allaymc.exile.data.PlayerDataManager playerDataManager;

    public ExileService(AllayMcPlugin plugin, com.allaymc.exile.data.PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    public AllayMcPlugin getPlugin() {
        return plugin;
    }

    public void disableVanillaWorldBorder() {
        String worldName = plugin.getConfig().getString("borders.overworld.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        WorldBorder border = world.getWorldBorder();
        border.setCenter(0.0, 0.0);
        border.setSize(5.9999968E7);
        border.setDamageAmount(0.0);
        border.setDamageBuffer(5.9999968E7);
        border.setWarningDistance(0);
        border.setWarningTime(0);
    }

    public void startTimerTask() {
        long periodTicks = Math.max(20L, plugin.getConfig().getLong("settings.timer-check-seconds", 1) * 20L);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ExileData data = playerDataManager.getData(player.getUniqueId());
                if (!data.isExiled()) continue;

                long remaining = data.getExileEndTime() - System.currentTimeMillis();

                if (remaining <= 0) {
                    String caseId = data.getActiveCaseId();
                    if (caseId != null && !caseId.isBlank()) {
                        var exileCase = plugin.getCaseDataManager().getCase(caseId);
                        if (exileCase != null && exileCase.getStatus() == com.allaymc.exile.data.ExileCase.Status.ACTIVE_EXILE) {
                            plugin.getExileCaseService().markRecoveryPending(exileCase);
                            plugin.getRecoveryService().openRecovery(player, exileCase);
                            player.sendMessage(plugin.getMessageUtil().get("player-time-ended"));
                        }
                    }
                } else {
                    player.sendActionBar(Component.text(plugin.getMessageUtil().color(
                            "&cExiled &7| &e" + TimeUtil.formatDuration(remaining) + " &7left"
                    )));
                }
            }
        }, 20L, periodTicks);
    }

    public void startDangerZoneTask() {
        if (!plugin.getConfig().getBoolean("danger-zone.enabled", true)) return;

        long periodTicks = Math.max(20L, plugin.getConfig().getLong("danger-zone.hit-interval-seconds", 1) * 20L);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                boolean danger = plugin.getBorderService().isDangerZone(player.getLocation())
                        || plugin.getNetherBorderService().isDangerZone(player.getLocation());

                if (danger) {
                    player.damage(plugin.getConfig().getDouble("danger-zone.damage-per-hit", 8.0));
                    player.sendActionBar(Component.text(plugin.getMessageUtil().color(
                            plugin.getConfig().getString("danger-zone.warning-actionbar", "&4TURN BACK")
                    )));
                }
            }
        }, 20L, periodTicks);
    }

    public boolean isExiled(UUID uuid) {
        return playerDataManager.getData(uuid).isExiled();
    }

    public long getRemaining(UUID uuid) {
        return Math.max(0L, playerDataManager.getData(uuid).getExileEndTime() - System.currentTimeMillis());
    }

    public void exilePlayer(Player player, long durationMillis, String reason) {
        ExileData data = playerDataManager.getData(player.getUniqueId());

        saveNormalState(player, data);
        clearPlayer(player);

        if (!data.getExileInventory().isEmpty()) {
            loadExileInventory(player, data);
        }

        Location exileLocation = generateRandomExileLocation(player.getWorld().getEnvironment() == World.Environment.NETHER);
        if (exileLocation == null) {
            player.sendMessage(ChatColor.RED + "Could not find a safe exile location.");
            return;
        }

        data.setExiled(true);
        data.setExileEndTime(System.currentTimeMillis() + durationMillis);
        data.setExileCount(data.getExileCount() + 1);
        data.setReason(reason == null || reason.isBlank() ? "No reason provided" : reason);
        data.setExileLocation(LocationUtil.serialize(exileLocation));

        player.teleport(exileLocation);
        playerDataManager.save(player.getUniqueId());

        String time = TimeUtil.formatDuration(durationMillis);

        player.sendTitle(
                plugin.getMessageUtil().raw("player-exiled-title"),
                plugin.getMessageUtil().raw("player-exiled-subtitle").replace("%time%", time),
                10, 60, 20
        );

        player.sendMessage(plugin.getMessageUtil().color(
                "&cYou have been exiled for &6" + time + "&c. Reason: &f" + data.getReason()
        ));

        notifyStaffExile(player, exileLocation, time, data.getReason());
    }

    private void notifyStaffExile(Player target, Location exileLocation, String time, String reason) {
        String msg = plugin.getMessageUtil().color(
                "&8[&bAllayMc&8] &c" + target.getName() +
                        " &7was exiled for &6" + time +
                        " &7| Reason: &f" + reason +
                        " &7| Location: &e" + exileLocation.getBlockX() + ", " + exileLocation.getBlockZ()
        );

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("allaymc.exile.others")) {
                online.sendMessage(msg);
            }
        }

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(msg);
    }

    public void rerollExileLocation(Player player) {
        ExileData data = playerDataManager.getData(player.getUniqueId());
        Location exileLocation = generateRandomExileLocation(player.getWorld().getEnvironment() == World.Environment.NETHER);
        if (exileLocation != null) {
            data.setExileLocation(LocationUtil.serialize(exileLocation));
            player.teleport(exileLocation);
            playerDataManager.save(player.getUniqueId());
        }
    }

    public void extendPlayer(Player player, long extraMillis) {
        ExileData data = playerDataManager.getData(player.getUniqueId());
        data.setExileEndTime(data.getExileEndTime() + extraMillis);
        playerDataManager.save(player.getUniqueId());

        player.sendMessage(plugin.getMessageUtil().get("player-extended-chat")
                .replace("%time%", TimeUtil.formatDuration(extraMillis)));
    }

    public void freePlayer(Player player, boolean timeEnded) {
        ExileData data = playerDataManager.getData(player.getUniqueId());

        saveExileState(player, data);
        clearPlayer(player);
        loadNormalState(player, data);

        data.setExiled(false);
        data.setExileEndTime(0L);
        data.setReason("No reason provided");
        data.setExileLocation("");
        data.setActiveCaseId("");

        teleportToReturn(player, data);
        playerDataManager.save(player.getUniqueId());

        player.sendMessage(plugin.getMessageUtil().get(timeEnded ? "player-time-ended" : "player-freed-chat"));
    }

    public void removePlayerPermanently(Player player) {
        String mode = plugin.getConfig().getString("punishments.exileremove.mode", "kick").toLowerCase();

        switch (mode) {
            case "ban" -> {
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        plugin.getConfig().getString("punishments.exileremove.ban-reason", "Removed from server by exile punishment."),
                        null,
                        "AllayMc"
                );
                player.kickPlayer(plugin.getConfig().getString("punishments.exileremove.ban-reason", "Removed from server by exile punishment."));
            }
            default -> player.kickPlayer(plugin.getConfig().getString("punishments.exileremove.kick-message", "Removed."));
        }
    }

    public void applyExileStateIfNeeded(Player player) {
        ExileData data = playerDataManager.getData(player.getUniqueId());
        if (!data.isExiled()) return;
        if (System.currentTimeMillis() >= data.getExileEndTime()) return;

        saveExileState(player, data);
        clearPlayer(player);
        loadExileInventory(player, data);

        Location exileLoc = LocationUtil.deserialize(data.getExileLocation());
        if (exileLoc == null) {
            exileLoc = generateRandomExileLocation(player.getWorld().getEnvironment() == World.Environment.NETHER);
            if (exileLoc != null) {
                data.setExileLocation(LocationUtil.serialize(exileLoc));
            }
        }

        if (exileLoc != null) {
            player.teleport(exileLoc);
        }

        playerDataManager.save(player.getUniqueId());
    }

    public void savePlayerStateOnQuit(Player player) {
        ExileData data = playerDataManager.getData(player.getUniqueId());
        if (data.isExiled()) {
            saveExileState(player, data);
        } else {
            saveNormalState(player, data);
        }
        playerDataManager.save(player.getUniqueId());
    }

    public void saveAllOnlineExileStates() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerStateOnQuit(player);
        }
    }

    public Location getSavedExileLocation(UUID uuid) {
        return LocationUtil.deserialize(playerDataManager.getData(uuid).getExileLocation());
    }

    private void saveNormalState(Player player, ExileData data) {
        data.setNormalInventory(InventoryUtil.itemStackArrayToBase64(player.getInventory().getStorageContents()));
        data.setNormalArmor(InventoryUtil.itemStackArrayToBase64(player.getInventory().getArmorContents()));
        data.setNormalOffhand(InventoryUtil.itemStackArrayToBase64(new ItemStack[]{player.getInventory().getItemInOffHand()}));
        data.setNormalLocation(LocationUtil.serialize(player.getLocation()));
    }

    private void loadNormalState(Player player, ExileData data) {
        ItemStack[] storage = InventoryUtil.itemStackArrayFromBase64(data.getNormalInventory());
        ItemStack[] armor = InventoryUtil.itemStackArrayFromBase64(data.getNormalArmor());
        ItemStack[] offhand = InventoryUtil.itemStackArrayFromBase64(data.getNormalOffhand());

        player.getInventory().setStorageContents(fixSize(storage, 36));
        player.getInventory().setArmorContents(fixSize(armor, 4));
        player.getInventory().setItemInOffHand(offhand.length > 0 ? offhand[0] : null);
        player.updateInventory();
    }

    private void saveExileState(Player player, ExileData data) {
        data.setExileInventory(InventoryUtil.itemStackArrayToBase64(player.getInventory().getStorageContents()));
        data.setExileArmor(InventoryUtil.itemStackArrayToBase64(player.getInventory().getArmorContents()));
        data.setExileOffhand(InventoryUtil.itemStackArrayToBase64(new ItemStack[]{player.getInventory().getItemInOffHand()}));
    }

    private void loadExileInventory(Player player, ExileData data) {
        ItemStack[] storage = InventoryUtil.itemStackArrayFromBase64(data.getExileInventory());
        ItemStack[] armor = InventoryUtil.itemStackArrayFromBase64(data.getExileArmor());
        ItemStack[] offhand = InventoryUtil.itemStackArrayFromBase64(data.getExileOffhand());

        player.getInventory().setStorageContents(fixSize(storage, 36));
        player.getInventory().setArmorContents(fixSize(armor, 4));
        player.getInventory().setItemInOffHand(offhand.length > 0 ? offhand[0] : null);
        player.updateInventory();
    }

    private ItemStack[] fixSize(ItemStack[] items, int size) {
        ItemStack[] fixed = new ItemStack[size];
        for (int i = 0; i < Math.min(items.length, size); i++) {
            fixed[i] = items[i];
        }
        return fixed;
    }

    private void clearPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        player.updateInventory();
    }

    private void teleportToReturn(Player player, ExileData data) {
        Location target = null;

        if (plugin.getConfig().getBoolean("settings.restore-normal-location-on-free", true)
                && data.getNormalLocation() != null
                && !data.getNormalLocation().isEmpty()) {
            target = LocationUtil.deserialize(data.getNormalLocation());
        }

        if (target == null && plugin.getConfig().getBoolean("return-location.use-config-return-if-normal-location-missing", true)) {
            World world = Bukkit.getWorld(plugin.getConfig().getString("return-location.world"));
            if (world != null) {
                target = new Location(
                        world,
                        plugin.getConfig().getDouble("return-location.x"),
                        plugin.getConfig().getDouble("return-location.y"),
                        plugin.getConfig().getDouble("return-location.z"),
                        (float) plugin.getConfig().getDouble("return-location.yaw"),
                        (float) plugin.getConfig().getDouble("return-location.pitch")
                );
            }
        }

        if (target != null) {
            player.teleport(target);
        }
    }

    private Location generateRandomExileLocation(boolean nether) {
        String prefix = nether ? "borders.nether" : "borders.overworld";
        World world = Bukkit.getWorld(plugin.getConfig().getString(prefix + ".world", nether ? "world_nether" : "world"));
        if (world == null) return null;

        int minCoord = (int) Math.round(plugin.getConfig().getDouble(prefix + ".exile-spawn-min", nether ? 250000.0 : 2000000.0));
        int maxCoord = (int) Math.round(plugin.getConfig().getDouble(prefix + ".exile-spawn-max", nether ? 300000.0 : 2300000.0));
        int threshold = (int) Math.round(plugin.getConfig().getDouble(prefix + ".exile-threshold-blocks", nether ? 250000.0 : 2000000.0));
        int tries = plugin.getConfig().getInt("borders.safe-teleport-tries", 100);

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < tries; i++) {
            int x = random.nextInt(minCoord, maxCoord + 1);
            int z = random.nextInt(minCoord, maxCoord + 1);

            if (random.nextBoolean()) x = -x;
            if (random.nextBoolean()) z = -z;

            if (Math.abs(x) < threshold || Math.abs(z) < threshold) {
                continue;
            }

            int y = world.getHighestBlockYAt(x, z) + 1;
            if (y < world.getMinHeight() + 1 || y > world.getMaxHeight()) {
                continue;
            }

            Block ground = world.getBlockAt(x, y - 1, z);
            Material groundType = ground.getType();

            if (!isSafeGround(groundType)) {
                continue;
            }

            Block feet = world.getBlockAt(x, y, z);
            Block head = world.getBlockAt(x, y + 1, z);

            if (!feet.isPassable() || !head.isPassable()) {
                continue;
            }

            return new Location(world, x + 0.5, y, z + 0.5);
        }

        return null;
    }

    private boolean isSafeGround(Material material) {
        if (material.isAir()) return false;

        if (plugin.getConfig().getBoolean("borders.avoid-water", true)
                && (material == Material.WATER || material == Material.KELP || material == Material.SEAGRASS)) {
            return false;
        }

        if (plugin.getConfig().getBoolean("borders.avoid-lava", true)
                && (material == Material.LAVA || material == Material.MAGMA_BLOCK)) {
            return false;
        }

        if (plugin.getConfig().getBoolean("borders.avoid-leaves", true)
                && material.name().endsWith("_LEAVES")) {
            return false;
        }

        return true;
    }
}
