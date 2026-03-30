package com.allaymc.exile.gui;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.RecoveryRequirement;
import com.allaymc.exile.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CaseCreationListener implements Listener {
    private final AllayMcPlugin plugin;
    private final YamlConfiguration guiConfig;

    public CaseCreationListener(AllayMcPlugin plugin) {
        this.plugin = plugin;
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CaseCreationGuiHolder caseHolder)) return;
        if (!(event.getWhoClicked() instanceof Player staff)) return;

        int itemStart = guiConfig.getInt("case-creation.item-start", 28);
        int itemSize = guiConfig.getInt("case-creation.item-size", 18);
        int confirmSlot = guiConfig.getInt("case-creation.confirm-slot", 49);
        int timeSlot = guiConfig.getInt("case-creation.time-slot", 20);
        int reasonSlot = guiConfig.getInt("case-creation.reason-slot", 22);

        int slot = event.getRawSlot();

        if (slot == timeSlot) {
            event.setCancelled(true);
            staff.sendMessage(plugin.getMessageUtil().color("&eUse placeholder time: default 1h for now. You can extend later."));
            return;
        }

        if (slot == reasonSlot) {
            event.setCancelled(true);
            staff.sendMessage(plugin.getMessageUtil().color("&eReason placeholder used: theft. You can improve this with chat-input later."));
            return;
        }

        if (slot == confirmSlot) {
            event.setCancelled(true);

            Player target = Bukkit.getPlayer(caseHolder.getTargetUuid());
            if (target == null) {
                staff.sendMessage(plugin.getMessageUtil().get("player-not-found"));
                staff.closeInventory();
                return;
            }

            List<RecoveryRequirement> requirements = new ArrayList<>();
            for (int i = 0; i < itemSize; i++) {
                ItemStack item = event.getInventory().getItem(itemStart + i);
                if (item == null || item.getType().isAir()) continue;
                requirements.add(new RecoveryRequirement(item.getType(), item.getAmount()));
            }

            if (requirements.isEmpty()) {
                staff.sendMessage(plugin.getMessageUtil().color("&cAdd at least one required item in the case GUI."));
                return;
            }

            long duration = TimeUtil.parseTimeToMillis("1h");
            String reason = "theft";

            ExileCase exileCase = plugin.getExileCaseService().createCase(
                    target.getUniqueId(),
                    staff.getUniqueId(),
                    reason,
                    duration,
                    requirements
            );

            plugin.getCaseHistoryManager().appendCase(target.getUniqueId(), target.getName(), exileCase.getCaseId(), exileCase.getStatus().name());
            plugin.getExileService().exilePlayer(target, duration, reason);

            staff.sendMessage(plugin.getMessageUtil().get("case-created")
                    .replace("%case%", exileCase.getCaseId())
                    .replace("%player%", target.getName()));

            staff.closeInventory();
            return;
        }

        if (slot < event.getInventory().getSize()) {
            boolean isItemArea = slot >= itemStart && slot < itemStart + itemSize;
            if (!isItemArea) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof CaseCreationGuiHolder)) return;

        int itemStart = guiConfig.getInt("case-creation.item-start", 28);
        int itemSize = guiConfig.getInt("case-creation.item-size", 18);

        for (int slot : event.getRawSlots()) {
            if (slot < event.getInventory().getSize()) {
                boolean isItemArea = slot >= itemStart && slot < itemStart + itemSize;
                if (!isItemArea) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
