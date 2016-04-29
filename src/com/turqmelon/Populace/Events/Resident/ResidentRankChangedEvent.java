package com.turqmelon.Populace.Events.Resident;

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

import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.TownRank;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidentRankChangedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Resident resident;
    private TownRank newRank;

    public ResidentRankChangedEvent(Resident resident, TownRank newRank) {
        this.resident = resident;
        this.newRank = newRank;
    }

    public Resident getResident() {
        return resident;
    }

    public TownRank getNewRank() {
        return newRank;
    }


    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
