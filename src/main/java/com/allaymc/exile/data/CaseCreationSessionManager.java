package com.allaymc.exile.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CaseCreationSessionManager {
    private final Map<UUID, CaseCreationSession> sessions = new HashMap<>();

    public void startSession(CaseCreationSession session) {
        sessions.put(session.getStaffUuid(), session);
    }

    public CaseCreationSession getSession(UUID staffUuid) {
        return sessions.get(staffUuid);
    }

    public boolean hasSession(UUID staffUuid) {
        return sessions.containsKey(staffUuid);
    }

    public void removeSession(UUID staffUuid) {
        sessions.remove(staffUuid);
    }
}
