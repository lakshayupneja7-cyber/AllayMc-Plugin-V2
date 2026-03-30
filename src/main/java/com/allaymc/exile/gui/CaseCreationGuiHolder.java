package com.allaymc.exile.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class CaseCreationGuiHolder implements InventoryHolder {
    private final UUID targetUuid;
    private final String targetName;

    public CaseCreationGuiHolder(UUID targetUuid, String targetName) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
