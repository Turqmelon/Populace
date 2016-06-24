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
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidentPrefixUpdatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Resident resident;
    private String newPrefix;

    public ResidentPrefixUpdatedEvent(Resident resident, String newPrefix) {
        this.resident = resident;
        this.newPrefix = newPrefix;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Resident getResident() {
        return resident;
    }

    public String getNewPrefix() {
        return newPrefix;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
