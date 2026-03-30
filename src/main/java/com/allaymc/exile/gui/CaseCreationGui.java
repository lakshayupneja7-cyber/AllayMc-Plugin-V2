package com.allaymc.exile.gui;

import com.allaymc.exile.AllayMcPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.UUID;

public class CaseCreationGui {
    private final AllayMcPlugin plugin;
    private final UUID targetUuid;
    private final String targetName;
    private final YamlConfiguration guiConfig;

    public CaseCreationGui(AllayMcPlugin plugin, UUID targetUuid, String targetName) {
        this.plugin = plugin;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
    }

    public Inventory build() {
        int rows = guiConfig.getInt("case-creation.rows", 6);
        Inventory inventory = Bukkit.createInventory(
                new CaseCreationGuiHolder(targetUuid, targetName),
                rows * 9,
                plugin.getMessageUtil().color("&cCreate Exile Case")
        );

        ItemStack filler = new ItemStack(Material.valueOf(guiConfig.getString("case-creation.filler-material", "BLACK_STAINED_GLASS_PANE")));
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName(plugin.getMessageUtil().color("&eTarget: &f" + targetName));
        head.setItemMeta(skullMeta);
        inventory.setItem(guiConfig.getInt("case-creation.info-slot", 4), head);

        ItemStack time = new ItemStack(Material.valueOf(guiConfig.getString("case-creation.time-material", "CLOCK")));
        ItemMeta timeMeta = time.getItemMeta();
        timeMeta.setDisplayName(plugin.getMessageUtil().color("&eTime: &fClick to set"));
        time.setItemMeta(timeMeta);
        inventory.setItem(guiConfig.getInt("case-creation.time-slot", 20), time);

        ItemStack reason = new ItemStack(Material.valueOf(guiConfig.getString("case-creation.reason-material", "WRITABLE_BOOK")));
        ItemMeta reasonMeta = reason.getItemMeta();
        reasonMeta.setDisplayName(plugin.getMessageUtil().color("&eReason: &fClick to set"));
        reason.setItemMeta(reasonMeta);
        inventory.setItem(guiConfig.getInt("case-creation.reason-slot", 22), reason);

        int itemStart = guiConfig.getInt("case-creation.item-start", 28);
        int itemSize = guiConfig.getInt("case-creation.item-size", 18);
        for (int i = 0; i < itemSize; i++) {
            inventory.setItem(itemStart + i, null);
        }

        ItemStack confirm = new ItemStack(Material.valueOf(guiConfig.getString("case-creation.confirm-material", "LIME_CONCRETE")));
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(plugin.getMessageUtil().color("&aConfirm Exile Case"));
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(guiConfig.getInt("case-creation.confirm-slot", 49), confirm);

        return inventory;
    }
}
