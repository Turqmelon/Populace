package com.turqmelon.Populace.Town;

import org.bukkit.ChatColor;

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
public enum TownRank {

    // Players who aren't part of a town have this permission level
    GUEST("Guest", ChatColor.GRAY + "[Guest] ", 0, 0, (byte)8),

    // Default permission level for residents
    RESIDENT("Resident", ChatColor.YELLOW + "[Resident] ", 1, 5, (byte)4),

    // Can manage the land
    ASSISTANT("Assistant", ChatColor.AQUA + "[Assistant] ", 2, 10, (byte)3),

    // Can manage the users
    MANAGER("Manager", ChatColor.GOLD + "[Manager] ", 3, 20, (byte)1),

    // Surpreme overlord
    MAYOR("Mayor", ChatColor.RED + "[Mayor] ", 4, 0, (byte)14);

    private String name;
    private String prefix;
    private int permissionLevel;
    private double cost;
    private byte dyeColor;

    TownRank(String name, String prefix, int permissionLevel, double cost, byte dyeColor) {
        this.name = name;
        this.prefix = prefix;
        this.permissionLevel = permissionLevel;
        this.cost = cost;
        this.dyeColor = dyeColor;
    }

    public boolean isAtLeast(TownRank rank){
        return getPermissionLevel() >= rank.getPermissionLevel();
    }

    public byte getDyeColor() {
        return dyeColor;
    }

    public double getCost() {
        return cost;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }
}
