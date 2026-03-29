package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.CaseDataManager;
import com.allaymc.exile.data.ExileCase;
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
                    Player player = Bukkit.getPlayer(exileCase.getPlayerUuid());
                    if (player != null && plugin.getConfig().getBoolean("recovery.auto-open-gui", true)) {
                        recoveryService.openRecovery(player, exileCase);
                        player.sendMessage(plugin.getMessageUtil().get("player-time-ended"));
                    }
                } else if (exileCase.getStatus() == ExileCase.Status.RECOVERY_PENDING && now >= exileCase.getGraceEndTime()) {
                    Player player = Bukkit.getPlayer(exileCase.getPlayerUuid());
                    if (player != null) recoveryService.failRecovery(player, exileCase);
                    else exileCaseService.markFailed(exileCase);
                }
            }
        }, 20L, 20L);
    }
}
