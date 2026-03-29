package com.allaymc.exile.util;

import com.allaymc.exile.data.RecoveryRequirement;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemMatcher {
    private ItemMatcher() {}

    public static boolean matches(Inventory inventory, List<RecoveryRequirement> requirements) {
        Map<Material, Integer> counts = new HashMap<>();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;
            counts.merge(item.getType(), item.getAmount(), Integer::sum);
        }
        for (RecoveryRequirement req : requirements) {
            if (counts.getOrDefault(req.getMaterial(), 0) < req.getAmount()) return false;
        }
        return true;
    }
}
