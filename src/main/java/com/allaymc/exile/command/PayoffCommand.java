package com.allaymc.exile.command;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.RecoveryRequirement;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PayoffCommand implements CommandExecutor, TabCompleter {
    private final AllayMcPlugin plugin;

    public PayoffCommand(AllayMcPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return true;
        }

        if (args.length == 0) {
            if (!plugin.getExileService().isExiled(player.getUniqueId())) {
                plugin.getMessageUtil().send(player, "payoff-not-exiled");
                return true;
            }

            String caseId = plugin.getPlayerDataManager().getData(player.getUniqueId()).getActiveCaseId();
            if (caseId == null || caseId.isBlank()) {
                plugin.getMessageUtil().send(player, "payoff-no-case");
                return true;
            }

            ExileCase exileCase = plugin.getCaseDataManager().getCase(caseId);
            if (exileCase == null) {
                plugin.getMessageUtil().send(player, "payoff-no-case");
                return true;
            }

            plugin.getRecoveryService().openRecovery(player, exileCase);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("claim")) {
            if (!player.hasPermission("allaymc.payoff.claim")) {
                plugin.getMessageUtil().send(player, "no-permission");
                return true;
            }

            ExileCase exileCase = plugin.getCaseDataManager().getCase(args[1]);
            if (exileCase == null) {
                plugin.getMessageUtil().send(player, "case-not-found");
                return true;
            }

            if (exileCase.getPaidItems().isEmpty()) {
                plugin.getMessageUtil().send(player, "payoff-no-items");
                return true;
            }

            if (exileCase.isPaidItemsClaimed()) {
                plugin.getMessageUtil().send(player, "payoff-already-claimed");
                return true;
            }

            for (RecoveryRequirement req : exileCase.getPaidItems()) {
                ItemStack item = new ItemStack(req.getMaterial(), req.getAmount());
                player.getInventory().addItem(item).values().forEach(overflow ->
                        player.getWorld().dropItemNaturally(player.getLocation(), overflow)
                );
            }

            exileCase.setPaidItemsClaimed(true);
            plugin.getCaseDataManager().putCase(exileCase);

            plugin.getMessageUtil().sendRaw(player, plugin.getMessageUtil().get("payoff-claimed").replace("%case%", exileCase.getCaseId()));
            return true;
        }

        plugin.getMessageUtil().send(player, "payoff-usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("claim");
        }
        return List.of();
    }
}
