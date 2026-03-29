package com.allaymc.exile.listener;

import com.allaymc.exile.data.CaseDataManager;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class RecoveryProtectionListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final CaseDataManager caseDataManager;

    public RecoveryProtectionListener(PlayerDataManager playerDataManager, CaseDataManager caseDataManager) {
        this.playerDataManager = playerDataManager;
        this.caseDataManager = caseDataManager;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String caseId = playerDataManager.getData(player.getUniqueId()).getActiveCaseId();
        if (caseId == null || caseId.isEmpty()) return;
        ExileCase exileCase = caseDataManager.getCase(caseId);
        if (exileCase != null && exileCase.getStatus() == ExileCase.Status.RECOVERY_PENDING) {
            event.setCancelled(true);
        }
    }
}
