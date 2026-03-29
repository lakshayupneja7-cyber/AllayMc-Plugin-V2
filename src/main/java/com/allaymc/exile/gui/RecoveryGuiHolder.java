package com.allaymc.exile.gui;

import com.allaymc.exile.data.ExileCase;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class RecoveryGuiHolder implements InventoryHolder {
    private final ExileCase exileCase;

    public RecoveryGuiHolder(ExileCase exileCase) {
        this.exileCase = exileCase;
    }

    public ExileCase getExileCase() { return exileCase; }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
