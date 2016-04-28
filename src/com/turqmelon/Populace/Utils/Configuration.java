package com.turqmelon.Populace.Utils;

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
public class Configuration {

    // The amount of seconds a player must wait before teleporting again
    public static int TELEPORT_COOLDOWN_TIME = 10;

    // The amount of seconds teleportations will take
    public static int TELEPORT_WARMUP_TIME = 10;

    // If a resident doesn't login for this amount of days, their data will be deleted
    // NOTE: Users part of a town are exempt.
    public static int PURGE_USER_DATA_DAYS = 180;

    // Allows PVP in the wilderness
    public static boolean WILDERNESS_PVP = true;

    // The minimum amount of blocks that residents must be from a town to make another
    public static int TOWN_MINIMUM_BUFFER = 250;

    // The cost for town creation
    public static double TOWN_CREATION_FEE = 1000;

    // The default bank balance for new towns
    public static double TOWN_BANK_DEFAULT = 100;

    // The default resident tax for new towns
    public static double TOWN_DEFAULT_TAX = 0;

    // The default salex tax for new towns (0 - 100)
    public static double TOWN_DEFAULT_SALES_TAX = 0;

    // The default plot tax for new towns
    public static double TOWN_DEFAULT_PLOT_TAX = 0;

    // The amount of hours a town grace period is
    // The grace period will never charge a town daily upkeep
    public static int TOWN_GRACE_PERIOD_HOURS = 24;

    // The daily upkeep cost of a residential plot
    public static double RESIDENTIAL_DAILY_COST = 10;

    // The daily upkeep cost of a merchant plot
    public static double MERCHANT_DAILY_COST = 20;

    // The daily upkeep cost of a battle plot
    public static double BATTLE_DAILY_COST = 10;

    // The daily upkeep cost of an outpost
    public static double OUTPOST_DAILY_COST = 50;

    // The daily upkeep cost of each individual bonus plot a town buys
    public static double BONUS_LAND_DAILY_COST = 10;

}
