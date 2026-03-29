package com.allaymc.exile.discord;

import com.allaymc.exile.AllayMcPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookService {
    private final AllayMcPlugin plugin;

    public DiscordWebhookService(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    public void post(String json) {
        if (!plugin.getConfig().getBoolean("discord.enabled", false)) return;
        String webhook = plugin.getConfig().getString("discord.webhook-url", "");
        if (webhook.isBlank()) return;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(webhook).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            }
            connection.getInputStream().close();
            connection.disconnect();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
        }
    }
}
