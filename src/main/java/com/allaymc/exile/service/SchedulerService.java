package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.CaseDataManager;
import com.allaymc.exile.data.ExileCase;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SchedulerService {
    private final AllayMcPlugin plugin;
    private final CaseDataManager caseDataManager;
    private final ExileCaseService exileCaseService;
    private final RecoveryService recoveryService;

    public SchedulerService(AllayMcPlugin plugin, CaseDataManager caseDataManager,
                            ExileCaseService exileCaseService, RecoveryService recoveryService) {
        this.plugin = plugin;
        this.caseDataManager = caseDataManager;
        this.exileCaseService = exileCaseService;
        this.recoveryService = recoveryService;
    }

    public void startRecoveryWatcher() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            for (ExileCase exileCase : caseDataManager.getAllCases()) {
                if (exileCase.getStatus() == ExileCase.Status.ACTIVE_EXILE && now >= exileCase.getEndTime()) {
                    exileCaseService.markRecoveryPending(exileCase);
                    exileCase.setGraceEndTime(now + plugin.getConfig().getLong("recovery.grace-seconds", 60L) * 1000L);
                    caseDataManager.putCase(exileCase);

                    Player player = Bukkit.getPlayer(exileCase.getPlayerUuid());
                    if (player != null) {
                        recoveryService.openRecovery(player, exileCase);
                        player.sendMessage(plugin.getMessageUtil().get("player-time-ended"));
                    }
                } else if (exileCase.getStatus() == ExileCase.Status.RECOVERY_PENDING) {
                    Player player = Bukkit.getPlayer(exileCase.getPlayerUuid());
                    long left = Math.max(0L, exileCase.getGraceEndTime() - now);

                    if (player != null) {
                        player.sendActionBar(Component.text(
                                plugin.getMessageUtil().color(
                                        plugin.getMessageUtil().raw("payoff-actionbar")
                                                .replace("%time%", com.allaymc.exile.util.TimeUtil.formatDuration(left))
                                )
                        ));
                    }

                    if (now >= exileCase.getGraceEndTime()) {
                        if (player != null) {
                            recoveryService.failRecovery(player, exileCase);
                        } else {
                            exileCaseService.markFailed(exileCase);
                        }
                    }
                }
            }
        }, 20L, 20L);
    }
}
