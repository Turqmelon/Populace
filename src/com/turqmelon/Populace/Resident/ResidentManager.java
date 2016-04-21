package com.turqmelon.Populace.Resident;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
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
public class ResidentManager {

    private static List<Resident> residents = new ArrayList<>();

    public static Resident getResident(Player player){
        return player != null ? getResident(player.getUniqueId()) : null;
    }

    public static Resident getResident(String name){
        for(Resident resident : getResidents()){
            if (resident.getName().equalsIgnoreCase(name)){
                return resident;
            }
        }
        return null;
    }

    public static Resident getResident(UUID uuid){
        for(Resident resident : getResidents()){
            if (resident.getUuid().equals(uuid)){
                return resident;
            }
        }
        return null;
    }

    public static List<Resident> getResidents() {
        return residents;
    }
}
