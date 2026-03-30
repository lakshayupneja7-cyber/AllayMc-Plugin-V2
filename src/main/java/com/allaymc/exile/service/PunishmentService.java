package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import org.bukkit.entity.Player;

public class PunishmentService {
    private final AllayMcPlugin plugin;

    public PunishmentService(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    public void failRecovery(Player player) {
        player.banPlayer(plugin.getMessageUtil().get("recover-timeout"));
    }
}
