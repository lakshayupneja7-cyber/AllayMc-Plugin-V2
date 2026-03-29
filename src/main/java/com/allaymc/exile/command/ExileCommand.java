package com.allaymc.exile.command;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.config.Messages;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.ExileData;
import com.allaymc.exile.data.RecoveryRequirement;
import com.allaymc.exile.util.NumberUtil;
import com.allaymc.exile.util.TimeUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ExileCommand implements CommandExecutor, TabCompleter {
    private final AllayMcPlugin plugin;

    public ExileCommand(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "exile" -> handleExile(sender, args);
            case "exileadd" -> handleExileAdd(sender, args);
            case "exilefree" -> handleExileFree(sender, args);
            case "exileextend" -> handleExileExtend(sender, args);
            case "exileremove" -> handleExileRemove(sender, args);
            case "exilecount" -> handleExileCount(sender, args);
            default -> false;
        };
    }

    private boolean handleExile(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                plugin.getMessageUtil().send(sender, Messages.PLAYER_ONLY);
                return true;
            }
            ExileData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
            if (!data.isExiled()) {
                plugin.getMessageUtil().send(player, "self-status-inactive");
                plugin.getMessageUtil().sendRaw(player, plugin.getMessageUtil().get("self-exile-count").replace("%count%", String.valueOf(data.getExileCount())));
                return true;
            }
            plugin.getMessageUtil().send(player, "self-status-active");
            plugin.getMessageUtil().sendRaw(player, plugin.getMessageUtil().get("self-time-left").replace("%time%", TimeUtil.formatDuration(plugin.getExileService().getRemaining(player.getUniqueId()))));
            plugin.getMessageUtil().sendRaw(player, plugin.getMessageUtil().get("self-exile-count").replace("%count%", String.valueOf(data.getExileCount())));
            plugin.getMessageUtil().sendRaw(player, plugin.getMessageUtil().get("self-reason").replace("%reason%", data.getReason()));
            plugin.getMessageUtil().sendRaw(player, plugin.getMessageUtil().get("self-case").replace("%case%", data.getActiveCaseId()));
            Location loc = plugin.getExileService().getSavedExileLocation(player.getUniqueId());
            if (loc != null) plugin.getMessageUtil().sendRaw(player, plugin.getMessageUtil().get("self-center")
                    .replace("%x%", String.valueOf(loc.getBlockX())).replace("%z%", String.valueOf(loc.getBlockZ())));
            return true;
        }

        if (!sender.hasPermission("allaymc.exile.others")) {
            plugin.getMessageUtil().send(sender, Messages.NO_PERMISSION);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        ExileData data = plugin.getPlayerDataManager().getData(target.getUniqueId());
        String targetName = target.getName() == null ? args[0] : target.getName();

        if (!data.isExiled()) {
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("others-status-inactive").replace("%player%", targetName));
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("others-exile-count")
                    .replace("%player%", targetName).replace("%count%", String.valueOf(data.getExileCount())));
            return true;
        }

        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("others-status-active").replace("%player%", targetName));
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("others-time-left")
                .replace("%player%", targetName).replace("%time%", TimeUtil.formatDuration(plugin.getExileService().getRemaining(target.getUniqueId()))));
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("others-exile-count")
                .replace("%player%", targetName).replace("%count%", String.valueOf(data.getExileCount())));
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("others-reason")
                .replace("%player%", targetName).replace("%reason%", data.getReason()));
        return true;
    }

    private boolean handleExileAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("allaymc.exile.add")) {
            plugin.getMessageUtil().send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length < 4) {
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&eUsage: /exileadd <player> <time> <reason> <item:amount,item:amount>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.getMessageUtil().send(sender, Messages.PLAYER_NOT_FOUND);
            return true;
        }
        if (plugin.getExileService().isExiled(target.getUniqueId())) {
            plugin.getMessageUtil().send(sender, Messages.ALREADY_EXILED);
            return true;
        }

        long duration = TimeUtil.parseTimeToMillis(args[1]);
        if (duration <= 0) {
            plugin.getMessageUtil().send(sender, Messages.INVALID_TIME);
            return true;
        }

        String reason = args[2];
        List<RecoveryRequirement> requirements = parseRequirements(args[3]);
        if (requirements.isEmpty()) {
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&cRequirements format invalid. Example: DIAMOND:32,IRON_INGOT:64"));
            return true;
        }

        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : new UUID(0, 0);
        ExileCase exileCase = plugin.getExileCaseService().createCase(target.getUniqueId(), staffUuid, reason, duration, requirements);
        plugin.getExileService().exilePlayer(target, duration, reason);

        plugin.getDiscordWebhookService().post(
                com.allaymc.exile.discord.DiscordPayloads.caseCreated(
                        exileCase,
                        target.getName(),
                        sender.getName()
                )
        );

        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("staff-exiled-player")
                .replace("%player%", target.getName())
                .replace("%time%", TimeUtil.formatDuration(duration)));
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("case-created")
                .replace("%case%", exileCase.getCaseId())
                .replace("%player%", target.getName()));
        return true;
    }

    private List<RecoveryRequirement> parseRequirements(String input) {
        List<RecoveryRequirement> list = new ArrayList<>();
        for (String token : input.split(",")) {
            String[] parts = token.split(":");
            if (parts.length != 2) continue;
            try {
                Material material = Material.valueOf(parts[0].toUpperCase(Locale.ROOT));
                int amount = Integer.parseInt(parts[1]);
                if (amount > 0) list.add(new RecoveryRequirement(material, amount));
            } catch (Exception ignored) {}
        }
        return list;
    }

    private boolean handleExileFree(CommandSender sender, String[] args) {
        if (!sender.hasPermission("allaymc.exile.free")) {
            plugin.getMessageUtil().send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length != 1) {
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&eUsage: /exilefree <player>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.getMessageUtil().send(sender, Messages.PLAYER_NOT_FOUND);
            return true;
        }
        if (!plugin.getExileService().isExiled(target.getUniqueId())) {
            plugin.getMessageUtil().send(sender, Messages.NOT_EXILED);
            return true;
        }
        plugin.getExileService().freePlayer(target, false);
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("staff-freed-player").replace("%player%", target.getName()));
        return true;
    }

    private boolean handleExileExtend(CommandSender sender, String[] args) {
        if (!sender.hasPermission("allaymc.exile.extend")) {
            plugin.getMessageUtil().send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length != 2) {
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&eUsage: /exileextend <player> <time>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.getMessageUtil().send(sender, Messages.PLAYER_NOT_FOUND);
            return true;
        }
        if (!plugin.getExileService().isExiled(target.getUniqueId())) {
            plugin.getMessageUtil().send(sender, Messages.NOT_EXILED);
            return true;
        }
        long extra = TimeUtil.parseTimeToMillis(args[1]);
        if (extra <= 0) {
            plugin.getMessageUtil().send(sender, Messages.INVALID_TIME);
            return true;
        }
        plugin.getExileService().extendPlayer(target, extra);
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("staff-exile-extended")
                .replace("%player%", target.getName()).replace("%time%", TimeUtil.formatDuration(extra)));
        return true;
    }

    private boolean handleExileRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("allaymc.exile.remove")) {
            plugin.getMessageUtil().send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length != 1) {
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&eUsage: /exileremove <player>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target != null) {
            plugin.getExileService().removePlayerPermanently(target);
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("staff-removed-player").replace("%player%", target.getName()));
            return true;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
        Bukkit.getBanList(BanList.Type.NAME).addBan(offline.getName(),
                plugin.getConfig().getString("punishments.exileremove.ban-reason", "Removed from server by exile punishment."),
                null, "AllayMc");
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("staff-removed-player").replace("%player%", offline.getName() == null ? args[0] : offline.getName()));
        return true;
    }

    private boolean handleExileCount(CommandSender sender, String[] args) {
        if (!sender.hasPermission("allaymc.exile.count")) {
            plugin.getMessageUtil().send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length != 1) {
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&eUsage: /exilecount <player>"));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        ExileData data = plugin.getPlayerDataManager().getData(target.getUniqueId());
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("staff-count-message")
                .replace("%player%", target.getName() == null ? args[0] : target.getName())
                .replace("%count%", String.valueOf(data.getExileCount())));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        if (args.length == 1 && List.of("exile", "exileadd", "exilefree", "exileextend", "exileremove", "exilecount").contains(cmd)) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if ((cmd.equals("exileadd") || cmd.equals("exileextend")) && args.length == 2) return List.of("30m", "1h", "12h", "1d", "7d");
        return List.of();
    }
}
