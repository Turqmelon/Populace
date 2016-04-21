package com.turqmelon.Populace.Utils;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Town.Town;

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
public class DisplayEntity {

    private Town town = null;
    private Plot plot = null;
    private String lastSentMessage = "";

    public Town getTown() {
        return town;
    }

    public String getLastSentMessage() {
        return lastSentMessage;
    }

    public void setLastSentMessage(String lastSentMessage) {
        this.lastSentMessage = lastSentMessage;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public Plot getPlot() {
        return plot;
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
    }
}
