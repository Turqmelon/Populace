package com.turqmelon.Populace.Utils;

import com.turqmelon.Populace.Populace;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

// Allows handling of teleport warmups and cooldowns
public class PopulaceTeleport {

    private static Map<UUID, Long> nextTeleport = new HashMap<>();

    private Player player;
    private Location location;
    private Location origin;
    private int warmup = 0;
    private int cooldown = 0;
    private long start;
    private boolean allowMovement = true;

    public PopulaceTeleport(Player player, Location location, Location origin) {
        this.player = player;
        this.location = location;
        this.origin = origin;
        initialize();
    }

    public PopulaceTeleport(Player player, Location location, Location origin, int warmup) {
        this.player = player;
        this.location = location;
        this.origin = origin;
        this.warmup = warmup;
        initialize();
    }

    public PopulaceTeleport(Player player, Location location, Location origin, int warmup, int cooldown) {
        this.player = player;
        this.location = location;
        this.origin = origin;
        this.warmup = warmup;
        this.cooldown = cooldown;
        initialize();
    }

    public PopulaceTeleport(Player player, Location location, Location origin, int warmup, int cooldown, boolean allowMovement) {
        this.player = player;
        this.location = location;
        this.origin = origin;
        this.warmup = warmup;
        this.cooldown = cooldown;
        this.allowMovement = allowMovement;
        initialize();
    }

    public static Map<UUID, Long> getNextTeleport() {
        return nextTeleport;
    }

    private void initialize() {

        // If the user has permission to bypass the warmup, don't make them wait
        if (player.hasPermission("populace.teleportation.nowarmup")) {
            this.warmup = 0;
        }

        // If the user is on cooldown, and doesn't have permission to bypass it, make them wait
        if (nextTeleport.containsKey(getPlayer().getUniqueId()) && !player.hasPermission("populace.teleportation.nocooldown")) {
            long next = nextTeleport.get(getPlayer().getUniqueId());
            long now = System.currentTimeMillis();
            if (now < next) {
                getPlayer().sendMessage(Msg.ERR + "You can't teleport again for " + ClockUtil.formatDateDiff(next, true) + ".");
                return;
            }
        }

        // mark the start of the teleport
        this.start = System.currentTimeMillis();

        // If there's a warmup, inform them of the wait. If they can't move during it, tell them that to.
        if (getWarmup() > 0) {
            getPlayer().sendMessage(Msg.INFO + "You'll be teleported in " + ClockUtil.formatDateDiff(getStart() + (getWarmup() * 1000), true) +
                    (!isAllowMovement() ? ". §o(Don't move!)" : "."));

        }

        // Start our repeating task
        new BukkitRunnable() {
            @Override
            public void run() {

                // If the player left, this task is useless. Cancel it.
                if (getPlayer() == null || !getPlayer().isOnline()) {
                    this.cancel();
                } else {

                    // If the player can't move, and they're more than 1 block from where they
                    // requested the teleport, cancel the teleport.
                    if (!isAllowMovement()) {
                        if (getPlayer().getLocation().distanceSquared(getOrigin()) > 1) {
                            getPlayer().sendMessage(Msg.ERR + "Teleport cancelled. You moved!");
                            getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                            this.cancel();
                            return;
                        }
                    }

                    // If the required time has elapsed, teleport them.
                    long teleportTime = getStart() + (getWarmup() * 1000);
                    long now = System.currentTimeMillis();
                    if (now >= teleportTime) {

                        getPlayer().teleport(getLocation());
                        getPlayer().sendMessage(Msg.OK + "Teleporting!");

                        Location[] loc = {getOrigin(), getOrigin().clone().add(0, 1, 0), getLocation(), getLocation().clone().add(0, 1, 0)};
                        for (Location l : loc) {
                            l.getWorld().playSound(l, Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
                            l.getWorld().playEffect(l, Effect.STEP_SOUND, org.bukkit.Material.PORTAL);
                        }


                        if (getCooldown() > 0) {
                            nextTeleport.put(getPlayer().getUniqueId(), now + (getCooldown() * 1000));
                        }

                        this.cancel();
                    } else {
                        getPlayer().playSound(getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);
                        getPlayer().sendMessage(Msg.INFO + "Teleport in §l" + ClockUtil.formatDateDiff(teleportTime, true) + "§b...");
                    }
                }
            }
        }.runTaskTimer(Populace.getInstance(), 20L, 20L);
    }

    public boolean isAllowMovement() {
        return allowMovement;
    }

    public void setAllowMovement(boolean allowMovement) {
        this.allowMovement = allowMovement;
    }

    public long getStart() {
        return start;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public Location getOrigin() {
        return origin;
    }

    public int getWarmup() {
        return warmup;
    }

    public int getCooldown() {
        return cooldown;
    }
}
