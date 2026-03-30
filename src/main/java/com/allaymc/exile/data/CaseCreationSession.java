package com.allaymc.exile.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CaseCreationSession {

    public enum Step {
        TIME,
        REASON,
        ITEMS,
        REVIEW
    }

    private final UUID staffUuid;
    private final UUID targetUuid;
    private final String targetName;

    private long durationMillis;
    private String reason;
    private List<RecoveryRequirement> requirements = new ArrayList<>();
    private Step step = Step.TIME;

    public CaseCreationSession(UUID staffUuid, UUID targetUuid, String targetName) {
        this.staffUuid = staffUuid;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<RecoveryRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<RecoveryRequirement> requirements) {
        this.requirements = requirements;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }
}
