package com.turqmelon.Populace.Utils;

import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONObject;

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
@SuppressWarnings("unchecked")
public class JailData {

    private Location jailLocation;
    private double range;
    private long expiration;
    private UUID jailer;

    public JailData(Location jailLocation, double range, long expiration, UUID jailer) {
        this.jailLocation = jailLocation;
        this.range = range;
        this.expiration = expiration;
        this.jailer = jailer;
    }

    public JailData(JSONObject object) {
        this.range = (double) object.get("range");
        this.expiration = (long) object.get("expiration");
        this.jailer = UUID.fromString((String) object.get("jailer"));
        JSONObject location = (JSONObject) object.get("location");
        World world = Bukkit.getWorld((String) location.get("world"));
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }
        int x = (int) ((long) location.get("x"));
        int y = (int) ((long) location.get("y"));
        int z = (int) ((long) location.get("z"));
        this.jailLocation = new Location(world, x, y, z);
    }

    public void sendExplanation(Resident resident) {
        Resident jailor = getJailer() != null ? ResidentManager.getResident(getJailer()) : null;
        resident.sendMessage(Msg.WARN + "You've been jailed by " + (jailor != null ? jailor.getName() : "your town") + " " + (getExpiration() == -1 ? "forever" : "for " + ClockUtil.formatDateDiff(getExpiration(), false)) + ".");
        resident.sendMessage(Msg.WARN + "Wait out the sentence, or leave the town from your §f/town§6 menu.");
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        JSONObject location = new JSONObject();
        location.put("world", jailLocation.getWorld().getName());
        location.put("x", jailLocation.getBlockX());
        location.put("y", jailLocation.getBlockY());
        location.put("z", jailLocation.getBlockZ());
        object.put("location", location);
        object.put("range", getRange());
        object.put("expiration", getExpiration());
        object.put("jailer", getJailer().toString());
        return object;
    }

    public Location getJailLocation() {
        return jailLocation;
    }

    public double getRange() {
        return range;
    }

    public long getExpiration() {
        return expiration;
    }

    public UUID getJailer() {
        return jailer;
    }
}
