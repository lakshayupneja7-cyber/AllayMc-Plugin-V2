package com.allaymc.exile.discord;

import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.RecoveryRequirement;

public final class DiscordPayloads {
    private DiscordPayloads() {}

    public static String caseCreated(ExileCase exileCase, String playerName, String staffName) {
        StringBuilder req = new StringBuilder();
        for (RecoveryRequirement requirement : exileCase.getRequiredItems()) {
            if (!req.isEmpty()) req.append("\\n");
            req.append(requirement.getAmount()).append("x ").append(requirement.getMaterial().name());
        }
        return "{"
                + "\"embeds\":[{"
                + "\"title\":\"Exile Case " + escape(exileCase.getCaseId()) + "\","
                + "\"description\":\"Player: " + escape(playerName) + "\\nStaff: " + escape(staffName) + "\\nReason: " + escape(exileCase.getReason()) + "\\nRequirements:\\n" + escape(req.toString()) + "\""
                + "}]"
                + "}";
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
