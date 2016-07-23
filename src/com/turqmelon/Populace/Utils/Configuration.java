package com.turqmelon.Populace.Utils;

import com.turqmelon.Populace.Populace;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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

    public static double TOWN_MINIMUM_UPKEEP = 25;

    // The amount of hours a town grace period is
    // The grace period will never charge a town daily upkeep
    public static int TOWN_GRACE_PERIOD_HOURS = 24;

    // The hour (on a 24 hour clock) that the new day code runs
    public static int NEW_DAY_TIME = 12;

    // The daily upkeep cost of a residential plot
    public static double RESIDENTIAL_DAILY_COST = 10;

    // The daily upkeep cost of a merchant plot
    public static double MERCHANT_DAILY_COST = 20;

    // The daily upkeep cost of a battle plot
    public static double BATTLE_DAILY_COST = 10;

    // The daily upkeep cost of an outpost
    public static double OUTPOST_DAILY_COST = 50;

    public static double PLOT_CLAIM_COST = 50;
    public static double OUTPOST_CLAIM_COST = 100;

    // The daily upkeep cost of each individual bonus plot a town buys
    public static double BONUS_LAND_DAILY_COST = 10;

    // Towns that are at least an hour old (to prevent abuse) will be explosively destroyed
    public static boolean DESTRUCTIVE_UNCLAIM = true;

    // The amount of explosions to spawn in ruined chunks
    public static int DESTRUCTIVE_UNCLAIM_EXPLOSIONS = 2;

    // Allows prevention of staking land claims in non-vanilla dimensions
    public static boolean ALLOW_SUPERNATURAL_DIMENSIONS = false;

    // Caps how high players can fly
    public static int FLIGHT_MAXIMUM_OFFSET = 50;

    public void load() throws IOException, ParseException, NoSuchFieldException, IllegalAccessException {
        File dir = Populace.getInstance().getDataFolder();
        if (!dir.exists()) {
            return;
        }
        File file = new File(dir, "config.json");
        if (!file.exists()) {
            return;
        }
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(new FileReader(file));
        JSONArray prefs = (JSONArray) data.getOrDefault("settings", new JSONArray());
        for (Object o : prefs) {
            JSONObject pref = (JSONObject) o;
            adjust((String) pref.get("key"), (String) pref.get("val"));
        }
    }

    public void save() throws IOException, IllegalAccessException {
        File dir = Populace.getInstance().getDataFolder();
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, "config.json");
        if (!file.exists()) {
            file.createNewFile();
        }
        JSONObject data = new JSONObject();
        JSONArray settings = new JSONArray();
        Map<String, Object> prefs = getAllSettings();
        for (String setting : prefs.keySet()) {
            Object val = prefs.get(setting);
            JSONObject pref = new JSONObject();
            pref.put("key", setting);
            pref.put("val", val.toString());
            settings.add(pref);
        }
        data.put("settings", settings);

        FileWriter fw = new FileWriter(file);
        fw.write(data.toJSONString());
        fw.flush();
        fw.close();
    }

    public Map<String, Object> getAllSettings() throws IllegalAccessException {
        Map<String, Object> settings = new HashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            settings.put(field.getName(), field.get(this));
        }
        return settings;
    }

    public void adjust(String setting, String newValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getDeclaredField(setting);
        Object data = newValue;
        if (newValue.equalsIgnoreCase("true")) {
            data = true;
        } else if (newValue.equalsIgnoreCase("false")) {
            data = false;
        } else if (isNumeric(newValue)) {
            data = Integer.parseInt(newValue);
        } else if (isDecimal(newValue)) {
            data = Double.parseDouble(newValue);
        }
        field.set(this, data);
    }

    private boolean isDecimal(String val) {
        try {
            Double.parseDouble(val);
            return true;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    private boolean isNumeric(String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

}
