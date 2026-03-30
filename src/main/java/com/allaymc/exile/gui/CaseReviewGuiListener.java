package com.allaymc.exile.gui;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.CaseCreationSession;
import com.allaymc.exile.data.ExileCase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;

public class CaseReviewGuiListener implements Listener {
    private final AllayMcPlugin plugin;
    private final YamlConfiguration guiConfig;

    public CaseReviewGuiListener(AllayMcPlugin plugin) {
        this.plugin = plugin;
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CaseReviewGuiHolder reviewHolder)) return;
        if (!(event.getWhoClicked() instanceof Player staff)) return;

        int confirmSlot = guiConfig.getInt("case-review.confirm-slot", 49);
        int cancelSlot = guiConfig.getInt("case-review.cancel-slot", 45);

        event.setCancelled(true);

        CaseCreationSession session = reviewHolder.getSession();

        if (event.getRawSlot() == cancelSlot) {
            plugin.getCaseCreationSessionManager().removeSession(staff.getUniqueId());
            staff.closeInventory();
            staff.sendMessage(plugin.getMessageUtil().get("case-builder-cancelled"));
            return;
        }

        if (event.getRawSlot() == confirmSlot) {
            Player target = Bukkit.getPlayer(session.getTargetUuid());
            if (target == null) {
                staff.sendMessage(plugin.getMessageUtil().get("player-not-found"));
                staff.closeInventory();
                plugin.getCaseCreationSessionManager().removeSession(staff.getUniqueId());
                return;
            }

            ExileCase exileCase = plugin.getExileCaseService().createCase(
                    target.getUniqueId(),
                    staff.getUniqueId(),
                    session.getReason(),
                    session.getDurationMillis(),
                    session.getRequirements()
            );

            plugin.getCaseHistoryManager().appendCase(
                    target.getUniqueId(),
                    target.getName(),
                    exileCase.getCaseId(),
                    exileCase.getStatus().name()
            );

            plugin.getExileService().exilePlayer(target, session.getDurationMillis(), session.getReason());

            plugin.getDiscordWebhookService().post(
                    com.allaymc.exile.discord.DiscordPayloads.caseCreated(
                            exileCase,
                            target.getName(),
                            staff.getName()
                    )
            );

            staff.sendMessage(plugin.getMessageUtil().get("case-builder-confirmed").replace("%player%", target.getName()));
            plugin.getCaseCreationSessionManager().removeSession(staff.getUniqueId());
            staff.closeInventory();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CaseReviewGuiHolder) {
            event.setCancelled(true);
        }
    }
}
