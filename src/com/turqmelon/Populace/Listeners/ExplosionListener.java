package com.turqmelon.Populace.Listeners;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

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
public class ExplosionListener implements Listener {

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        handleExplosion(event.getBlock().getWorld(), event.blockList());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        handleExplosion(event.getLocation().getWorld(), event.blockList());
    }

    private void handleExplosion(World world, List<Block> blocks) {

        boolean applySurfaceRules = world.getEnvironment() == World.Environment.NORMAL;

        //make a list of blocks which were allowed to explode
        List<Block> explodedBlocks = new ArrayList<>();


        for (Block block : blocks) {
            //always ignore air blocks
            if (block.getType() == Material.AIR) continue;

            Plot plot = PlotManager.getPlot(block.getChunk());

            if (plot == null) {
                if (!applySurfaceRules || block.getLocation().getBlockY() < block.getWorld().getSeaLevel()-7) {
                    explodedBlocks.add(block);
                }
            }
        }

        //clear original damage list and replace with allowed damage list
        blocks.clear();
        blocks.addAll(explodedBlocks);
    }

}
