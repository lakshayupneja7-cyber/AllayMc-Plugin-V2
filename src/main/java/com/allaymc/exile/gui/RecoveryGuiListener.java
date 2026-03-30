package com.allaymc.exile.gui;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.service.ExileCaseService;
import com.allaymc.exile.service.RecoveryService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class RecoveryGuiListener implements Listener {
    private final AllayMcPlugin plugin;
    private final RecoveryService recoveryService;
    private final ExileCaseService exileCaseService;
    private final YamlConfiguration guiConfig;

    public RecoveryGuiListener(AllayMcPlugin plugin, RecoveryService recoveryService, ExileCaseService exileCaseService) {
        this.plugin = plugin;
        this.recoveryService = recoveryService;
        this.exileCaseService = exileCaseService;
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof RecoveryGuiHolder recoveryGuiHolder)) return;

        int depositStart = guiConfig.getInt("recovery.deposit-start", 27);
        int depositSize = guiConfig.getInt("recovery.deposit-size", 20);
        int confirmSlot = guiConfig.getInt("recovery.confirm-slot", 50);

        int slot = event.getRawSlot();

        if (slot < event.getInventory().getSize()) {
            boolean isDepositSlot = slot >= depositStart && slot < depositStart + depositSize;
            boolean isConfirm = slot == confirmSlot;

            if (!isDepositSlot && !isConfirm) {
                event.setCancelled(true);
                return;
            }
        }

        if (slot == confirmSlot && event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);
            ExileCase exileCase = recoveryGuiHolder.getExileCase();

            boolean early = exileCase.getStatus() == ExileCase.Status.ACTIVE_EXILE;
            boolean success = recoveryService.tryComplete(player, exileCase, event.getInventory(), early);

            if (success) {
                plugin.getCaseHistoryManager().updateCase(player.getUniqueId(), exileCase.getCaseId(), exileCase.getStatus().name());
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof RecoveryGuiHolder)) return;

        int depositStart = guiConfig.getInt("recovery.deposit-start", 27);
        int depositSize = guiConfig.getInt("recovery.deposit-size", 20);

        for (int slot : event.getRawSlots()) {
            if (slot < event.getInventory().getSize()) {
                boolean isDepositSlot = slot >= depositStart && slot < depositStart + depositSize;
                if (!isDepositSlot) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof RecoveryGuiHolder recoveryGuiHolder)) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        ExileCase exileCase = recoveryGuiHolder.getExileCase();
        if (!plugin.getPlayerDataManager().getData(player.getUniqueId()).isExiled()) return;

        boolean early = exileCase.getStatus() == ExileCase.Status.ACTIVE_EXILE
                || exileCase.getStatus() == ExileCase.Status.RECOVERY_PENDING;

        boolean success = recoveryService.tryComplete(player, exileCase, event.getInventory(), early);
        if (success) {
            plugin.getCaseHistoryManager().updateCase(player.getUniqueId(), exileCase.getCaseId(), exileCase.getStatus().name());
            return;
        }

        refundDepositItems(player, event.getInventory());
    }

    private void refundDepositItems(Player player, org.bukkit.inventory.Inventory inventory) {
        int depositStart = guiConfig.getInt("recovery.deposit-start", 27);
        int depositSize = guiConfig.getInt("recovery.deposit-size", 20);

        for (int i = 0; i < depositSize; i++) {
            int slot = depositStart + i;
            if (slot >= inventory.getSize()) break;

            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            inventory.setItem(slot, null);
            player.getInventory().addItem(item).values().forEach(overflow ->
                    player.getWorld().dropItemNaturally(player.getLocation(), overflow)
            );
        }
    }
}
