package com.allaymc.exile.gui;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.ExileData;
import com.allaymc.exile.service.ExileCaseService;
import com.allaymc.exile.service.RecoveryService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryHolder;

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
        int depositSize = guiConfig.getInt("recovery.deposit-size", 18);
        int confirmSlot = guiConfig.getInt("recovery.confirm-slot", 49);

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

            if (recoveryService.tryComplete(player, exileCase, event.getInventory())) {
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof RecoveryGuiHolder)) return;

        int depositStart = guiConfig.getInt("recovery.deposit-start", 27);
        int depositSize = guiConfig.getInt("recovery.deposit-size", 18);

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

        ExileData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        if (!data.isExiled()) return;

        ExileCase exileCase = recoveryGuiHolder.getExileCase();
        if (exileCase.getStatus() != ExileCase.Status.RECOVERY_PENDING) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getPlayerDataManager().getData(player.getUniqueId()).isExiled()) {
                player.openInventory(new RecoveryGui(plugin, exileCase).build());
            }
        }, 1L);
    }
}
