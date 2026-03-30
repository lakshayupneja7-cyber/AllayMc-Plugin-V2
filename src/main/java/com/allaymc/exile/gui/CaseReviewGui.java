package com.allaymc.exile.gui;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.CaseCreationSession;
import com.allaymc.exile.data.RecoveryRequirement;
import com.allaymc.exile.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;

public class CaseReviewGui {
    private final AllayMcPlugin plugin;
    private final CaseCreationSession session;
    private final YamlConfiguration guiConfig;

    public CaseReviewGui(AllayMcPlugin plugin, CaseCreationSession session) {
        this.plugin = plugin;
        this.session = session;
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
    }

    public Inventory build() {
        int rows = guiConfig.getInt("case-review.rows", 6);
        Inventory inventory = Bukkit.createInventory(
                new CaseReviewGuiHolder(session),
                rows * 9,
                plugin.getMessageUtil().color("&cReview Exile Case")
        );

        ItemStack filler = new ItemStack(Material.valueOf(guiConfig.getString("case-review.filler-material", "BLACK_STAINED_GLASS_PANE")));
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        OfflinePlayer target = Bukkit.getOfflinePlayer(session.getTargetUuid());
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName(plugin.getMessageUtil().color("&eTarget: &f" + session.getTargetName()));
        head.setItemMeta(skullMeta);
        inventory.setItem(guiConfig.getInt("case-review.target-slot", 4), head);

        ItemStack time = new ItemStack(Material.valueOf(guiConfig.getString("case-review.time-material", "CLOCK")));
        ItemMeta timeMeta = time.getItemMeta();
        timeMeta.setDisplayName(plugin.getMessageUtil().color("&eTime: &f" + TimeUtil.formatDuration(session.getDurationMillis())));
        time.setItemMeta(timeMeta);
        inventory.setItem(guiConfig.getInt("case-review.time-slot", 19), time);

        ItemStack reason = new ItemStack(Material.valueOf(guiConfig.getString("case-review.reason-material", "WRITABLE_BOOK")));
        ItemMeta reasonMeta = reason.getItemMeta();
        reasonMeta.setDisplayName(plugin.getMessageUtil().color("&eReason: &f" + session.getReason()));
        reason.setItemMeta(reasonMeta);
        inventory.setItem(guiConfig.getInt("case-review.reason-slot", 22), reason);

        int itemsStart = guiConfig.getInt("case-review.items-start", 28);
        for (int i = 0; i < session.getRequirements().size() && itemsStart + i < inventory.getSize(); i++) {
            RecoveryRequirement req = session.getRequirements().get(i);
            ItemStack item = new ItemStack(req.getMaterial(), Math.max(1, req.getAmount()));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(plugin.getMessageUtil().color("&eRequired: &f" + req.getAmount() + "x " + req.getMaterial().name()));
            item.setItemMeta(meta);
            inventory.setItem(itemsStart + i, item);
        }

        ItemStack confirm = new ItemStack(Material.valueOf(guiConfig.getString("case-review.confirm-material", "LIME_CONCRETE")));
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(plugin.getMessageUtil().color("&aConfirm Exile Case"));
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(guiConfig.getInt("case-review.confirm-slot", 49), confirm);

        ItemStack cancel = new ItemStack(Material.valueOf(guiConfig.getString("case-review.cancel-material", "RED_CONCRETE")));
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(plugin.getMessageUtil().color("&cCancel"));
        cancel.setItemMeta(cancelMeta);
        inventory.setItem(guiConfig.getInt("case-review.cancel-slot", 45), cancel);

        return inventory;
    }
}
