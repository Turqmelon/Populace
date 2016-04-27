package com.turqmelon.Populace.Events.Town;

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

import com.turqmelon.Populace.Town.Town;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TownDestructionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Town town;
    private boolean forced;

    public TownDestructionEvent(Town town, boolean forced) {
        this.town = town;
        this.forced = forced;
    }

    public boolean isForced() {
        return forced;
    }

    public Town getTown() {
        return town;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
