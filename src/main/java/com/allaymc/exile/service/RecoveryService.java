package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.RecoveryRequirement;
import com.allaymc.exile.gui.RecoveryGui;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecoveryService {
    private final AllayMcPlugin plugin;
    private final ExileCaseService exileCaseService;
    private final ValidationService validationService;
    private final PunishmentService punishmentService;
    private final ExileService exileService;
    private final YamlConfiguration guiConfig;

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
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
    }

    public void openRecovery(Player player, ExileCase exileCase) {
        player.openInventory(new RecoveryGui(plugin, exileCase).build());
        player.sendMessage(plugin.getMessageUtil().get("payoff-open"));
    }

    public boolean tryComplete(Player player, ExileCase exileCase, Inventory inventory, boolean earlyRecovery) {
        int depositStart = guiConfig.getInt("recovery.deposit-start", 27);
        int depositSize = guiConfig.getInt("recovery.deposit-size", 20);

        boolean valid = validationService.validateRecovery(inventory, depositStart, depositSize, exileCase.getRequiredItems());
        if (!valid) {
            player.sendMessage(plugin.getMessageUtil().get("payoff-incomplete"));
            return false;
        }

        exileCaseService.markCompleted(exileCase);

        List<RecoveryRequirement> paid = new ArrayList<>();
        for (RecoveryRequirement req : exileCase.getRequiredItems()) {
            paid.add(new RecoveryRequirement(req.getMaterial(), req.getAmount()));
        }
        exileCase.setPaidItems(paid);
        exileCase.setPaidItemsClaimed(false);
        plugin.getCaseDataManager().putCase(exileCase);

        exileService.freePlayer(player, false);

        player.sendMessage(plugin.getMessageUtil().get("payoff-early-success"));
        return true;
    }

    public void failRecovery(Player player, ExileCase exileCase) {
        exileCaseService.markFailed(exileCase);
        punishmentService.failRecovery(player);
    }
}
