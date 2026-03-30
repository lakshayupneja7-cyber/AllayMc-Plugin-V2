package com.allaymc.exile.service;

import com.allaymc.exile.data.RecoveryRequirement;
import com.allaymc.exile.util.ItemMatcher;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class ValidationService {

    public boolean validateRecovery(Inventory inventory, int depositStart, int depositSize, List<RecoveryRequirement> requirements) {
        return ItemMatcher.matchesExactlyInRange(inventory, depositStart, depositSize, requirements);
    }
}
