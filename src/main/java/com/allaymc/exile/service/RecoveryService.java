package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RecoveryService {
    private final AllayMcPlugin plugin;
    private final ExileCaseService exileCaseService;
    private final ValidationService validationService;
    private final PunishmentService punishmentService;
    private final ExileService exileService;

    public RecoveryService(AllayMcPlugin plugin,
                           ExileCaseService exileCaseService,
                           ValidationService validationService,
                           PunishmentService punishmentService,
                           ExileService exileService) {
        this.plugin = plugin;
        this.exileCaseService = exileCaseService;
        this.validationService = validationService;
        this.punishmentService = punishmentService;
        this.exileService = exileService;
    }

    public void openRecovery(Player player, ExileCase exileCase) {
        player.openInventory(new com.allaymc.exile.gui.RecoveryGui(plugin, exileCase).build());
        player.sendMessage(plugin.getMessageUtil().get("recovery-open"));
    }

    public boolean tryComplete(Player player, ExileCase exileCase, Inventory inventory) {
        boolean valid = validationService.validateRecovery(inventory, exileCase.getRequiredItems());
        if (!valid) {
            player.sendMessage(plugin.getMessageUtil().color("&cExact required items not deposited."));
            return false;
        }

        exileCaseService.markCompleted(exileCase);
        exileService.freePlayer(player, false);
        player.sendMessage(plugin.getMessageUtil().get("recovery-success"));
        return true;
    }

    public void failRecovery(Player player, ExileCase exileCase) {
        exileCaseService.markFailed(exileCase);
        punishmentService.failRecovery(player);
    }
}
