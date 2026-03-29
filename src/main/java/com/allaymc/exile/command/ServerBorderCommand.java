package com.allaymc.exile.command;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.util.NumberUtil;
import org.bukkit.command.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ServerBorderCommand implements CommandExecutor, TabCompleter {
    private final AllayMcPlugin plugin;

    public ServerBorderCommand(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission("allaymc.serverborder.view")) {
                plugin.getMessageUtil().send(sender, "no-permission");
                return true;
            }
            double blocks = plugin.getBorderStateManager().getOverworldMainSize();
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("server-border-info")
                    .replace("%blocks%", NumberUtil.stripDecimal(blocks))
                    .replace("%chunks%", NumberUtil.stripDecimal(blocks / 16.0)));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("set")) {
            if (!sender.hasPermission("allaymc.serverborder.set")) {
                plugin.getMessageUtil().send(sender, "no-permission");
                return true;
            }
            if (args.length != 2) {
                plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&eUsage: /serverborder set <blocks>"));
                return true;
            }
            double value = parse(args[1]);
            if (value <= 0) {
                plugin.getMessageUtil().send(sender, "invalid-number");
                return true;
            }
            plugin.getBorderStateManager().setOverworldMainSize(value);
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("server-border-set").replace("%blocks%", NumberUtil.stripDecimal(value)));
            return true;
        }
        if (sub.equals("add")) {
            if (!sender.hasPermission("allaymc.serverborder.add")) {
                plugin.getMessageUtil().send(sender, "no-permission");
                return true;
            }
            if (args.length != 2) {
                plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().color("&eUsage: /serverborder add <blocks>"));
                return true;
            }
            double value = parse(args[1]);
            if (value <= 0) {
                plugin.getMessageUtil().send(sender, "invalid-number");
                return true;
            }
            double current = plugin.getBorderStateManager().getOverworldMainSize();
            plugin.getBorderStateManager().setOverworldMainSize(current + value);
            plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("server-border-added")
                    .replace("%blocks%", NumberUtil.stripDecimal(value))
                    .replace("%new_size%", NumberUtil.stripDecimal(current + value)));
            return true;
        }
        if (sub.equals("chunks")) {
            double blocks = plugin.getBorderStateManager().getOverworldMainSize();
            plugin.getMessageUtil().sendRaw(sender, "&eServer border: &6" + NumberUtil.stripDecimal(blocks / 16.0) + " chunks");
            return true;
        }
        plugin.getMessageUtil().sendRaw(sender, plugin.getMessageUtil().get("usage-serverborder"));
        return true;
    }

    private double parse(String in) {
        try { return Double.parseDouble(in); } catch (Exception e) { return -1; }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("set", "add", "chunks").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        if (args.length == 2 && List.of("set", "add").contains(args[0].toLowerCase(Locale.ROOT))) return List.of("6400", "8000", "16000", "32000");
        return List.of();
    }
}
