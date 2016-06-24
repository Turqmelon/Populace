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
import com.turqmelon.Populace.Utils.HUDUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/******************************************************************************
 *                                                                            *
 * CONFIDENTIAL                                                               *
 * __________________                                                         *
 *                                                                            *
 * [2012 - 2016] Devon "Turqmelon" Thome                                      *
 *  All Rights Reserved.                                                      *
 *                                                                            *
 * NOTICE:  All information contained herein is, and remains                  *
 * the property of Turqmelon and its suppliers,                               *
 * if any.  The intellectual and technical concepts contained                 *
 * herein are proprietary to Turqmelon and its suppliers and                  *
 * may be covered by U.S. and Foreign Patents,                                *
 * patents in process, and are protected by trade secret or copyright law.    *
 * Dissemination of this information or reproduction of this material         *
 * is strictly forbidden unless prior written permission is obtained          *
 * from Turqmelon.                                                            *
 *                                                                            *
 ******************************************************************************/
public class MoveListener implements Listener {

    private Map<UUID, Location> lastSafePoint = new HashMap<>();
    private Map<UUID, Location> asyncLocationManagement = new HashMap<>();

    public MoveListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Resident resident = ResidentManager.getResident(player);
                    if (resident == null) continue;
                    Location current = player.getLocation();

                    boolean different = false;
                    Location last = asyncLocationManagement.getOrDefault(player.getUniqueId(), null);

                    if (last == null) {
                        different = true;
                    } else if (!last.getWorld().getName().equals(current.getWorld().getName())) {
                        different = true;
                    } else if (last.getBlockX() != current.getBlockX() || last.getBlockY() != current.getBlockY() || last.getBlockZ() != current.getBlockZ()) {
                        different = true;
                    }

                    if (different) {

                        Plot plot = PlotManager.getPlot(player.getLocation().getChunk());

                        if (plot != null) {

                            Plot lastPlot = resident.getDisplayEntity().getPlot();
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    boolean denyEntry = false;
                                    if (lastPlot == null || !lastPlot.getUuid().equals(plot.getUuid())) {
                                        ResidentPlotEnterEvent enterEvent = new ResidentPlotEnterEvent(plot, resident);
                                        Bukkit.getPluginManager().callEvent(enterEvent);
                                        if (enterEvent.isCancelled()) {
                                            denyEntry = true;
                                        }
                                        if (lastPlot != null) {
                                            Bukkit.getPluginManager().callEvent(new ResidentPlotLeaveEvent(lastPlot, resident));
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
                                    }
                                }
                            }.runTask(Populace.getInstance());

                        } else {
                            Plot lastPlot = resident.getDisplayEntity().getPlot();
                            if (lastPlot != null) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Bukkit.getPluginManager().callEvent(new ResidentPlotLeaveEvent(lastPlot, resident));
                                    }
                                }.runTask(Populace.getInstance());
                            }
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
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.teleport(resident.getJailData().getJailLocation());
                                        resident.getJailData().sendExplanation(resident);
                                    }
                                }.runTask(Populace.getInstance());
                            }
                        }

                        asyncLocationManagement.put(player.getUniqueId(), player.getLocation());

                    }

                }
            }
        }.runTaskTimerAsynchronously(Populace.getInstance(), 5L, 5L);
    }

}
