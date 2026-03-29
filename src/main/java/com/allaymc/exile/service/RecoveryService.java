package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.PlayerDataManager;
import com.allaymc.exile.gui.RecoveryGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RecoveryService {
    private final AllayMcPlugin plugin;
    private final ExileCaseService exileCaseService;
    private final ValidationService validationService;
    private final PunishmentService punishmentService;
    private final PlayerDataManager playerDataManager;
    private final ExileService exileService;

    public RecoveryService(AllayMcPlugin plugin, ExileCaseService exileCaseService, ValidationService validationService,
                           PunishmentService punishmentService, PlayerDataManager playerDataManager, ExileService exileService) {
        this.plugin = plugin;
        this.exileCaseService = exileCaseService;
        this.validationService = validationService;
        this.punishmentService = punishmentService;
        this.playerDataManager = playerDataManager;
        this.exileService = exileService;
    }

    public void openRecovery(Player player, ExileCase exileCase) {
        RecoveryGui gui = new RecoveryGui(plugin, exileCase);
        player.openInventory(gui.build());
        player.sendMessage(plugin.getMessageUtil().get("recovery-open"));
    }

    public boolean tryComplete(Player player, ExileCase exileCase, Inventory inventory) {
        if (!validationService.validateRecovery(inventory, exileCase.getRequiredItems())) return false;
        exileCaseService.markCompleted(exileCase);
        exileService.freePlayer(player, false);
        player.sendMessage(plugin.getMessageUtil().get("recovery-success"));
        return true;
    }

    public void failRecovery(Player player, ExileCase exileCase) {
        exileCaseService.markFailed(exileCase);
        punishmentService.failRecovery(player);
    }

    public void tickRecoveryDeadlines() {
        for (ExileCase exileCase : exileCaseService.getClass().cast(exileCaseService).getClass() == null ? java.util.List.<ExileCase>of() : java.util.List.<ExileCase>of()) {
            // no-op placeholder to keep structure simple in generated repo
        }
    }
}
