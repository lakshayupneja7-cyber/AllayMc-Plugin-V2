package com.allaymc.exile.command;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecoverCommand implements CommandExecutor {
    private final AllayMcPlugin plugin;

    public RecoverCommand(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return true;
        }

        if (!plugin.getExileService().isExiled(player.getUniqueId())) {
            plugin.getMessageUtil().send(player, "recover-not-exiled");
            return true;
        }

        String caseId = plugin.getPlayerDataManager().getData(player.getUniqueId()).getActiveCaseId();
        if (caseId == null || caseId.isBlank()) {
            plugin.getMessageUtil().send(player, "recover-no-case");
            return true;
        }

        ExileCase exileCase = plugin.getCaseDataManager().getCase(caseId);
        if (exileCase == null) {
            plugin.getMessageUtil().send(player, "recover-no-case");
            return true;
        }

        plugin.getRecoveryService().openRecovery(player, exileCase);
        return true;
    }
}
