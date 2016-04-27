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
import com.turqmelon.Populace.Town.Town;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidentLeaveTownEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private Town town;
    private Resident resident;
    private ResidentKickedFromTownEvent kickEvent = null;
    private boolean cancelled;

    public ResidentLeaveTownEvent(Town town, Resident resident) {
        this.town = town;
        this.resident = resident;
    }

    public ResidentLeaveTownEvent(Town town, Resident resident, ResidentKickedFromTownEvent kickEvent) {
        this.town = town;
        this.resident = resident;
        this.kickEvent = kickEvent;
    }

    public boolean wasKicked() {
        return kickEvent != null;
    }

    public ResidentKickedFromTownEvent getKickEvent() {
        return kickEvent;
    }

    public Town getTown() {
        return town;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Resident getResident() {
        return resident;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
