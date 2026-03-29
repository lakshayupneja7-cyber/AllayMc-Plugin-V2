package com.allaymc.exile.command;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import org.bukkit.command.*;

import java.util.List;
import java.util.Locale;

public class CaseAdminCommand implements CommandExecutor, TabCompleter {
    private final AllayMcPlugin plugin;

    public CaseAdminCommand(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("allaymc.case.admin")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("/caseadmin <info|fail|complete> <caseId>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        ExileCase exileCase = plugin.getCaseDataManager().getCase(args[1]);
        if (exileCase == null) {
            plugin.getMessageUtil().send(sender, "case-not-found");
            return true;
        }
        switch (sub) {
            case "info" -> plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("case-info")
                    .replace("%case%", exileCase.getCaseId())
                    .replace("%status%", exileCase.getStatus().name()));
            case "fail" -> {
                plugin.getExileCaseService().markFailed(exileCase);
                plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("case-failed").replace("%case%", exileCase.getCaseId()));
            }
            case "complete" -> {
                plugin.getExileCaseService().markCompleted(exileCase);
                plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("case-completed").replace("%case%", exileCase.getCaseId()));
            }
            default -> sender.sendMessage("/caseadmin <info|fail|complete> <caseId>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("info", "fail", "complete");
        return List.of();
    }
}
