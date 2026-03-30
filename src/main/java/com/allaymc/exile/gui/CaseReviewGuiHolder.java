package com.allaymc.exile.gui;

import com.allaymc.exile.data.CaseCreationSession;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CaseReviewGuiHolder implements InventoryHolder {
    private final CaseCreationSession session;

    public CaseReviewGuiHolder(CaseCreationSession session) {
        this.session = session;
    }

    public CaseCreationSession getSession() {
        return session;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
