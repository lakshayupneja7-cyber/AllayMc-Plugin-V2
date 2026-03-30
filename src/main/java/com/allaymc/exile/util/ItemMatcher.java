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

    public static boolean matchesExactly(Inventory inventory, List<RecoveryRequirement> requirements) {
        Map<Material, Integer> deposited = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;
            deposited.merge(item.getType(), item.getAmount(), Integer::sum);
        }

        Map<Material, Integer> required = new HashMap<>();
        for (RecoveryRequirement requirement : requirements) {
            required.merge(requirement.getMaterial(), requirement.getAmount(), Integer::sum);
        }

        if (deposited.size() != required.size()) {
            return false;
        }

        for (Map.Entry<Material, Integer> entry : required.entrySet()) {
            if (!deposited.containsKey(entry.getKey())) {
                return false;
            }
            if (!deposited.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }
}
