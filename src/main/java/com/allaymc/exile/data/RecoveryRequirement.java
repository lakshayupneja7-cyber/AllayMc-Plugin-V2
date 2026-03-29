package com.allaymc.exile.data;

import org.bukkit.Material;

public class RecoveryRequirement {
    private Material material;
    private int amount;

    public RecoveryRequirement() {}

    public RecoveryRequirement(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}
