package com.allaymc.exile.listener;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.CaseCreationSession;
import com.allaymc.exile.data.RecoveryRequirement;
import com.allaymc.exile.gui.CaseReviewGui;
import com.allaymc.exile.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CaseCreationChatListener implements Listener {
    private final AllayMcPlugin plugin;

    public CaseCreationChatListener(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player staff = event.getPlayer();

        if (!plugin.getCaseCreationSessionManager().hasSession(staff.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        CaseCreationSession session = plugin.getCaseCreationSessionManager().getSession(staff.getUniqueId());
        String input = event.getMessage().trim();

        if (input.equalsIgnoreCase("cancel")) {
            plugin.getCaseCreationSessionManager().removeSession(staff.getUniqueId());
            staff.sendMessage(plugin.getMessageUtil().get("case-builder-cancelled"));
            return;
        }

        switch (session.getStep()) {
            case TIME -> {
                long duration = TimeUtil.parseTimeToMillis(input);
                if (duration <= 0) {
                    staff.sendMessage(plugin.getMessageUtil().get("case-builder-invalid-time"));
                    return;
                }

                session.setDurationMillis(duration);
                session.setStep(CaseCreationSession.Step.REASON);
                staff.sendMessage(plugin.getMessageUtil().get("case-builder-reason"));
            }

            case REASON -> {
                if (input.isBlank()) {
                    staff.sendMessage(plugin.getMessageUtil().color("&cReason cannot be empty."));
                    return;
                }

                session.setReason(input);
                session.setStep(CaseCreationSession.Step.ITEMS);
                staff.sendMessage(plugin.getMessageUtil().get("case-builder-items"));
            }

            case ITEMS -> {
                List<RecoveryRequirement> requirements = parseRequirements(input);
                if (requirements.isEmpty()) {
                    staff.sendMessage(plugin.getMessageUtil().get("case-builder-invalid-items"));
                    return;
                }

                session.setRequirements(requirements);
                session.setStep(CaseCreationSession.Step.REVIEW);

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    staff.openInventory(new CaseReviewGui(plugin, session).build());
                    staff.sendMessage(plugin.getMessageUtil().get("case-builder-review"));
                });
            }

            case REVIEW -> {
                staff.sendMessage(plugin.getMessageUtil().color("&eReview the case GUI or type &fcancel"));
            }
        }
    }

    private List<RecoveryRequirement> parseRequirements(String input) {
        List<RecoveryRequirement> list = new ArrayList<>();

        for (String token : input.split(",")) {
            String[] parts = token.trim().split(":");
            if (parts.length != 2) continue;

            try {
                Material material = Material.valueOf(parts[0].trim().toUpperCase(Locale.ROOT));
                int amount = Integer.parseInt(parts[1].trim());
                if (amount > 0) {
                    list.add(new RecoveryRequirement(material, amount));
                }
            } catch (Exception ignored) {
            }
        }

        return list;
    }
}
