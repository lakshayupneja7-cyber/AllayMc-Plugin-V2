package com.allaymc.exile.listener;

import com.allaymc.exile.service.BorderService;
import com.allaymc.exile.service.ExileService;
import com.allaymc.exile.service.NetherBorderService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BorderEnforcementListener implements Listener {
    private final BorderService borderService;
    private final NetherBorderService netherBorderService;
    private final ExileService exileService;

    public BorderEnforcementListener(BorderService borderService, NetherBorderService netherBorderService, ExileService exileService) {
        this.borderService = borderService;
        this.netherBorderService = netherBorderService;
        this.exileService = exileService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() == null || to.getWorld() == null) return;
        if (!from.getWorld().equals(to.getWorld())) return;

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        boolean exiled = exileService.isExiled(player.getUniqueId());

        boolean fromInsideMain;
        boolean toInsideMain;
        boolean fromOutsideExile;
        boolean toOutsideExile;

        if (netherBorderService.isNether(from)) {
            fromInsideMain = netherBorderService.isInsideMainBorder(from);
            toInsideMain = netherBorderService.isInsideMainBorder(to);
            fromOutsideExile = netherBorderService.isOutsideExileBorder(from);
            toOutsideExile = netherBorderService.isOutsideExileBorder(to);
        } else if (borderService.isOverworld(from)) {
            fromInsideMain = borderService.isInsideMainBorder(from);
            toInsideMain = borderService.isInsideMainBorder(to);
            fromOutsideExile = borderService.isOutsideExileBorder(from);
            toOutsideExile = borderService.isOutsideExileBorder(to);
        } else {
            return;
        }

        if (!exiled && fromInsideMain && !toInsideMain) {
            event.setCancelled(true);
            borderService.showMainBorderHit(player, to);
            borderService.applyKnockback(player, from, to);
            return;
        }

        if (!exiled && !fromOutsideExile && toOutsideExile) {
            event.setCancelled(true);
            borderService.showExileBorderHit(player, to, false);
            borderService.applyKnockback(player, from, to);
            return;
        }

        if (exiled && fromOutsideExile && !toOutsideExile) {
            event.setCancelled(true);
            player.damage(1000.0);
            exileService.rerollExileLocation(player);
        }
    }
}
