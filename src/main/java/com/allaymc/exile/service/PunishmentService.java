package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PunishmentService {
    private final AllayMcPlugin plugin;

    public PunishmentService(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    public void failRecovery(Player player) {
        String reason = plugin.getConfig().getString(
                "recovery.fail-ban-reason",
                "Failed exile recovery requirements"
        );

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                reason,
                null,
                "AllayMc"
        );

        player.kickPlayer(plugin.getMessageUtil().get("recover-timeout"));
    }
}
