package com.turqmelon.Populace.Plot;

import com.turqmelon.Populace.Utils.Configuration;

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
public enum PlotType {

    RESIDENTIAL("Residential", Configuration.RESIDENTIAL_DAILY_COST),
    MERCHANT("Merchant", Configuration.MERCHANT_DAILY_COST),
    BATTLE("Battle", Configuration.BATTLE_DAILY_COST),
    OUTPOST("Outpost", Configuration.OUTPOST_DAILY_COST);

    private String name;
    private double dailyCost;

    PlotType(String name, double dailyCost) {
        this.name = name;
        this.dailyCost = dailyCost;
    }

    public String getName() {
        return name;
    }

    public double getDailyCost() {
        return dailyCost;
    }
}
