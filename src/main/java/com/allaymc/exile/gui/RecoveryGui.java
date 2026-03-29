package com.allaymc.exile.gui;

import com.allaymc.exile.AllayMcPlugin;
import com.allaymc.exile.data.ExileCase;
import com.allaymc.exile.data.RecoveryRequirement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;

public class RecoveryGui {
    private final AllayMcPlugin plugin;
    private final ExileCase exileCase;
    private final YamlConfiguration guiConfig;

    public RecoveryGui(AllayMcPlugin plugin, ExileCase exileCase) {
        this.plugin = plugin;
        this.exileCase = exileCase;
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
    }

    public Inventory build() {
        int rows = guiConfig.getInt("recovery.rows", 6);
        Inventory inventory = Bukkit.createInventory(new RecoveryGuiHolder(exileCase), rows * 9,
                plugin.getMessageUtil().color(plugin.getConfig().getString("recovery.gui-title", "&cExile Recovery")));

        ItemStack filler = new ItemStack(Material.valueOf(guiConfig.getString("recovery.filler-material", "GRAY_STAINED_GLASS_PANE")));
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);

        int previewStart = guiConfig.getInt("recovery.preview-start", 9);
        int depositStart = guiConfig.getInt("recovery.deposit-start", 27);
        int depositSize = guiConfig.getInt("recovery.deposit-size", 18);

        for (int i = 0; i < depositSize && depositStart + i < inventory.getSize(); i++) {
            inventory.setItem(depositStart + i, null);
        }

        List<RecoveryRequirement> requirements = exileCase.getRequiredItems();
        for (int i = 0; i < requirements.size() && previewStart + i < inventory.getSize(); i++) {
            RecoveryRequirement requirement = requirements.get(i);
            ItemStack item = new ItemStack(requirement.getMaterial(), Math.max(1, requirement.getAmount()));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(plugin.getMessageUtil().color("&eRequired: &f" + requirement.getAmount() + "x " + requirement.getMaterial().name()));
            item.setItemMeta(meta);
            inventory.setItem(previewStart + i, item);
        }

        ItemStack info = new ItemStack(Material.valueOf(guiConfig.getString("recovery.info-material", "BOOK")));
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(plugin.getMessageUtil().color("&eCase " + exileCase.getCaseId()));
        info.setItemMeta(infoMeta);
        inventory.setItem(guiConfig.getInt("recovery.info-slot", 4), info);

        ItemStack confirm = new ItemStack(Material.valueOf(guiConfig.getString("recovery.confirm-material", "LIME_CONCRETE")));
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(plugin.getMessageUtil().color("&aConfirm Recovery"));
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(guiConfig.getInt("recovery.confirm-slot", 49), confirm);

        return inventory;
    }
}
