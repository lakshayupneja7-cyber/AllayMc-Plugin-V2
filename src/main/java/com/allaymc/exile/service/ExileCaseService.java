package com.allaymc.exile.service;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.*;

import java.util.List;
import java.util.UUID;

public class ExileCaseService {
    private final AllayMcPlugin plugin;
    private final CaseDataManager caseDataManager;
    private final PlayerDataManager playerDataManager;

    public ExileCaseService(AllayMcPlugin plugin, CaseDataManager caseDataManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.caseDataManager = caseDataManager;
        this.playerDataManager = playerDataManager;
    }

    public ExileCase createCase(UUID playerUuid, UUID staffUuid, String reason, long durationMillis, List<RecoveryRequirement> requirements) {
        ExileCase exileCase = new ExileCase();
        exileCase.setCaseId(caseDataManager.nextCaseId());
        exileCase.setPlayerUuid(playerUuid);
        exileCase.setStaffUuid(staffUuid);
        exileCase.setReason(reason);
        exileCase.setStartTime(System.currentTimeMillis());
        exileCase.setEndTime(System.currentTimeMillis() + durationMillis);
        exileCase.setRequiredItems(requirements);
        caseDataManager.putCase(exileCase);

        ExileData data = playerDataManager.getData(playerUuid);
        data.setActiveCaseId(exileCase.getCaseId());
        data.setReason(reason);
        data.setExileEndTime(exileCase.getEndTime());
        playerDataManager.save(playerUuid);

        return exileCase;
    }

    public ExileCase getCase(String caseId) {
        return caseDataManager.getCase(caseId);
    }

    public void markRecoveryPending(ExileCase exileCase) {
        exileCase.setStatus(ExileCase.Status.RECOVERY_PENDING);
        long graceMinutes = plugin.getConfig().getLong("recovery.grace-minutes", 10L);
        exileCase.setGraceEndTime(System.currentTimeMillis() + graceMinutes * 60_000L);
        caseDataManager.putCase(exileCase);
    }

    public void markCompleted(ExileCase exileCase) {
        exileCase.setStatus(ExileCase.Status.COMPLETED);
        caseDataManager.putCase(exileCase);
    }

    public void markFailed(ExileCase exileCase) {
        exileCase.setStatus(ExileCase.Status.FAILED_BANNED);
        caseDataManager.putCase(exileCase);
    }

    public void saveAll() {
        caseDataManager.saveAll();
    }
}
