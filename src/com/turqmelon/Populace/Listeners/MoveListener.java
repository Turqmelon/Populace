package com.turqmelon.Populace.Listeners;

import com.turqmelon.Populace.Events.Resident.ResidentPlotEnterEvent;
import com.turqmelon.Populace.Events.Resident.ResidentPlotLeaveEvent;
import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.PermissionSet;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Utils.CombatHelper;
import com.turqmelon.Populace.Utils.HUDUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/******************************************************************************
 * *
 * CONFIDENTIAL                                                               *
 * __________________                                                         *
 * *
 * [2012 - 2016] Devon "Turqmelon" Thome                                      *
 * All Rights Reserved.                                                      *
 * *
 * NOTICE:  All information contained herein is, and remains                  *
 * the property of Turqmelon and its suppliers,                               *
 * if any.  The intellectual and technical concepts contained                 *
 * herein are proprietary to Turqmelon and its suppliers and                  *
 * may be covered by U.S. and Foreign Patents,                                *
 * patents in process, and are protected by trade secret or copyright law.    *
 * Dissemination of this information or reproduction of this material         *
 * is strictly forbidden unless prior written permission is obtained          *
 * from Turqmelon.                                                            *
 * *
 ******************************************************************************/
public class MoveListener implements Listener {

    private Map<UUID, Location> lastSafePoint = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        if (resident == null) return;

        Plot plot = PlotManager.getPlot(player.getLocation().getChunk());
        Plot lastPlot;

        boolean denyExit = false;

        if (plot != null) {

            lastPlot = resident.getDisplayEntity().getPlot();
            boolean denyEntry = false;
            if (lastPlot == null || !lastPlot.getUuid().equals(plot.getUuid())) {
                if (lastPlot != null) {
                    ResidentPlotLeaveEvent exitEvent = new ResidentPlotLeaveEvent(lastPlot, resident);
                    Bukkit.getPluginManager().callEvent(exitEvent);
                    denyExit = exitEvent.isCancelled();
                }
                if (!denyExit) {
                    ResidentPlotEnterEvent enterEvent = new ResidentPlotEnterEvent(plot, resident);
                    Bukkit.getPluginManager().callEvent(enterEvent);
                    denyEntry = enterEvent.isCancelled();
                }
            }
            if (denyEntry || !plot.can(resident, PermissionSet.ENTRY)) {
                if (lastSafePoint.containsKey(player.getUniqueId())) {
                    player.teleport(lastSafePoint.get(player.getUniqueId()));
                } else {
                    player.teleport(player.getLocation().clone().add(16, 0, 0));
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                resident.setFallImmunity(System.currentTimeMillis());
                plot.getPlotChunk().visualize(player, true);
                HUDUtil.sendActionBar(player, "§c§lYou can't enter that area.");
                return;
            }

        } else {
            lastPlot = resident.getDisplayEntity().getPlot();
            if (lastPlot != null) {

                ResidentPlotLeaveEvent exitEvent = new ResidentPlotLeaveEvent(lastPlot, resident);
                Bukkit.getPluginManager().callEvent(exitEvent);

                denyExit = exitEvent.isCancelled();

            }
        }

        if (denyExit && lastSafePoint.containsKey(player.getUniqueId())) {
            player.setVelocity(calculateVelocity(lastSafePoint.get(player.getUniqueId()), player.getLocation()));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
            resident.setFallImmunity(System.currentTimeMillis());
            lastPlot.getPlotChunk().visualize(player, true);
            HUDUtil.sendActionBar(player, "§c§lYou can't exit this area.");
            return;
        }

        if (Populace.isPopulaceWarzoneLoaded() && CombatHelper.shouldBounceBack(player, plot, lastPlot) && lastSafePoint.containsKey(player.getUniqueId())) {
            player.setVelocity(calculateVelocity(lastSafePoint.get(player.getUniqueId()), player.getLocation()));
            HUDUtil.sendActionBar(player, "§c§lYou can't enter no-PVP areas while combat tagged.");
            return;
        }

        lastSafePoint.put(player.getUniqueId(), player.getLocation());

        Town town = plot != null ? plot.getTown() : null;
        String msg = resident.getUpdateMessage(town, plot);
        if (!msg.equals(resident.getDisplayEntity().getLastSentMessage())) {
            resident.getDisplayEntity().setLastSentMessage(msg);
            if (msg.length() > 0) {
                HUDUtil.sendActionBar(player, msg);
            }
        }

        if (resident.isJailed()) {
            double range = resident.getJailData().getRange();
            range = range * range;
            if (player.getLocation().distanceSquared(resident.getJailData().getJailLocation()) > range) {
                player.teleport(resident.getJailData().getJailLocation());
                resident.getJailData().sendExplanation(resident);
            }
        }

    }

    private Vector calculateVelocity(Location loc1, Location loc2) {
        return loc1.toVector().subtract(loc2.toVector()).normalize().multiply(1.3);
    }

}
