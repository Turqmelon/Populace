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

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Resident.Resident;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidentPlotLeaveEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private Plot plot;
    private Resident resident;

    private boolean cancelled = false;

    public ResidentPlotLeaveEvent(Plot plot, Resident resident) {
        this.plot = plot;
        this.resident = resident;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Resident getResident() {
        return resident;
    }

    public Plot getPlot() {
        return plot;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
