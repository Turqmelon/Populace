package com.turqmelon.Populace.Plot;

import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownManager;
import org.bukkit.Chunk;
import org.bukkit.World;

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
public class PlotManager {
    public static Plot getPlot(PlotChunk chunk){
        return getPlot(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public static Plot getPlot(Chunk chunk){
        return getPlot(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public static Plot getPlot(World world, long x, long z){

        for(Town town : TownManager.getTowns()){
            Plot plot = town.getPlot(world, x, z);
            if (plot != null){
                return plot;
            }
        }

        return null;
    }
}
