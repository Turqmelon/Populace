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
public enum TownLevel {

    SETTLEMENT("Settlement", " Settlement", ChatColor.DARK_GRAY, 1, 1, 16),
    HAMLET("Hamlet", " Hamlet", ChatColor.GRAY, 2, 1, 32),
    VILLAGE("Village", " Village", ChatColor.BLUE, 6, 1, 96),
    TOWN("Town", " Town", ChatColor.DARK_GREEN, 10, 1, 160),
    LARGE_TOWN("Large Town", " Town", ChatColor.GREEN, 15, 1.1, 224),
    CITY("City", " City", ChatColor.YELLOW, 25, 1.2, 320),
    LARGE_CITY("Large City", " City", ChatColor.GOLD, 30, 1.3, 384),
    METROPOLIS("Metropolis", " Metropolis", ChatColor.RED, 50, 1.5, 448);

    private String name;
    private String suffix;
    private ChatColor color;
    private int residents;
    private double upkeepModifier;
    private int maxland;

    TownLevel(String name, String suffix, ChatColor color, int residents, double upkeepModifier, int maxland) {
        this.name = name;
        this.suffix = suffix;
        this.color = color;
        this.residents = residents;
        this.upkeepModifier = upkeepModifier;
        this.maxland = maxland;
    }

    public ChatColor getColor() {
        return color;
    }

    public static TownLevel getAppropriateLevel(int residents){
        TownLevel level = SETTLEMENT;
        for(TownLevel t : TownLevel.values()){
            if (residents >= t.getResidents()){
                level = t;
            }
        }
        return level;
    }

    public boolean isHighest(){
        return this == METROPOLIS;
    }

    public String getName() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getResidents() {
        return residents;
    }

    public double getUpkeepModifier() {
        return upkeepModifier;
    }

    public int getMaxland() {
        return maxland;
    }
}
