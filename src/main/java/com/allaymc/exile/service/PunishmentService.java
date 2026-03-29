package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PunishmentService {
    private final AllayMcPlugin plugin;

    public PunishmentService(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    public void failRecovery(Player player) {
        String action = plugin.getConfig().getString("recovery.fail-action", "ban").toLowerCase();
        switch (action) {
            case "kick" -> player.kickPlayer("Recovery failed.");
            case "ban" -> {
                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                        plugin.getConfig().getString("recovery.fail-ban-reason", "Failed exile recovery requirements"),
                        null, "AllayMc");
                player.kickPlayer(plugin.getConfig().getString("recovery.fail-ban-reason", "Failed exile recovery requirements"));
            }
            case "unwhitelist" -> {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(player.getUniqueId());
                offline.setWhitelisted(false);
                player.kickPlayer("Recovery failed.");
            }
            default -> player.kickPlayer("Recovery failed.");
        }
    }
}
