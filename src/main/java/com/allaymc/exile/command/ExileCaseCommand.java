package com.allaymc.exile.command;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.CaseCreationSession;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ExileCaseCommand implements CommandExecutor, TabCompleter {
    private final AllayMcPlugin plugin;

    public ExileCaseCommand(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("allaymc.exile.add")) {
            plugin.getMessageUtil().send(player, "no-permission");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            plugin.getCaseCreationSessionManager().removeSession(player.getUniqueId());
            player.sendMessage(plugin.getMessageUtil().get("case-builder-cancelled"));
            return true;
        }

        if (args.length != 2 || !args[0].equalsIgnoreCase("create")) {
            player.sendMessage(plugin.getMessageUtil().color("&eUsage: /exilecase create <player>"));
            player.sendMessage(plugin.getMessageUtil().color("&eType &fcancel &ein chat to cancel."));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessageUtil().send(player, "player-not-found");
            return true;
        }

        CaseCreationSession session = new CaseCreationSession(player.getUniqueId(), target.getUniqueId(), target.getName());
        plugin.getCaseCreationSessionManager().startSession(session);

        player.sendMessage(plugin.getMessageUtil().get("case-builder-start").replace("%player%", target.getName()));
        player.sendMessage(plugin.getMessageUtil().get("case-builder-time"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("create", "cancel").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
