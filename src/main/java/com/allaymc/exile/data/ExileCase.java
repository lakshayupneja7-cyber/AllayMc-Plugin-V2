package com.allaymc.exile.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExileCase {
    public enum Status {
        ACTIVE_EXILE,
        RECOVERY_PENDING,
        COMPLETED,
        FAILED_BANNED
    }

    private String caseId;
    private UUID playerUuid;
    private UUID staffUuid;
    private String reason;
    private long startTime;
    private long endTime;
    private long graceEndTime;
    private Status status = Status.ACTIVE_EXILE;
    private List<RecoveryRequirement> requiredItems = new ArrayList<>();

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }
    public UUID getStaffUuid() { return staffUuid; }
    public void setStaffUuid(UUID staffUuid) { this.staffUuid = staffUuid; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public long getGraceEndTime() { return graceEndTime; }
    public void setGraceEndTime(long graceEndTime) { this.graceEndTime = graceEndTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public List<RecoveryRequirement> getRequiredItems() { return requiredItems; }
    public void setRequiredItems(List<RecoveryRequirement> requiredItems) { this.requiredItems = requiredItems; }
}
