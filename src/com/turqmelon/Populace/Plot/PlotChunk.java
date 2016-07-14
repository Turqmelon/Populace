package com.turqmelon.Populace.Plot;

import com.turqmelon.Populace.Populace;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

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
@SuppressWarnings("unchecked")
public class PlotChunk {

    private World world;
    private long x;
    private long z;

    public PlotChunk(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public PlotChunk(JSONObject obj){
        this.world = Bukkit.getWorld((String) obj.get("world"));
        this.x = (long) obj.get("x");
        this.z = (long) obj.get("z");
    }

    public void visualize(Player player){
        visualize(player, false);
    }

    public void visualize(Player player, boolean error){

        Chunk chunk = getWorld().getChunkAt(getX(), getZ());

        List<Location> changed = new ArrayList<>();

        int highestY = player.getLocation().getBlockY()+10;


        Location corner1;
        Location corner2;
        Location corner3;
        Location corner4;
        int i;
        for (i = getWorld().getSeaLevel(); i <= highestY; i++) {
            corner1 = chunk.getBlock(0, i, 0).getLocation();
            corner2 = chunk.getBlock(0, i, 15).getLocation();
            corner3 = chunk.getBlock(15, i, 0).getLocation();
            corner4 = chunk.getBlock(15, i, 15).getLocation();
            Material mat = error?(i==highestY?Material.REDSTONE_BLOCK:Material.STAINED_GLASS_PANE):(i==highestY?Material.GLOWSTONE:Material.STAINED_GLASS_PANE);
            if (corner1.getBlock().getType() == Material.AIR) {
                player.sendBlockChange(corner1, mat,  error?(byte)14:(byte)0);
                changed.add(corner1);
            }
            if (corner2.getBlock().getType() == Material.AIR) {
                player.sendBlockChange(corner2, mat, error?(byte)14:(byte)0);
                changed.add(corner2);
            }
            if (corner3.getBlock().getType() == Material.AIR) {
                player.sendBlockChange(corner3, mat, error?(byte)14:(byte)0);
                changed.add(corner3);
            }
            if (corner4.getBlock().getType() == Material.AIR) {
                player.sendBlockChange(corner4, mat, error?(byte)14:(byte)0);
                changed.add(corner4);
            }
            if (error){
                for(int x = 1; x <= 14; x++){
                    Location loc = chunk.getBlock(x, i, 0).getLocation();
                    if (loc.getBlock().getType()!=Material.AIR)continue;
                    player.sendBlockChange(loc, i==highestY?Material.STAINED_GLASS_PANE:Material.BARRIER, (byte)14);
                    changed.add(loc);
                }
                for(int z = 1; z <= 14; z++){
                    Location loc = chunk.getBlock(0, i, z).getLocation();
                    if (loc.getBlock().getType()!=Material.AIR)continue;
                    player.sendBlockChange(loc, i==highestY?Material.STAINED_GLASS_PANE:Material.BARRIER, (byte)14);
                    changed.add(loc);
                }
                for(int x = 1; x <= 14; x++){
                    Location loc = chunk.getBlock(x, i, 15).getLocation();
                    if (loc.getBlock().getType()!=Material.AIR)continue;
                    player.sendBlockChange(loc, i==highestY?Material.STAINED_GLASS_PANE:Material.BARRIER, (byte)14);
                    changed.add(loc);
                }
                for(int z = 1; z <= 14; z++){
                    Location loc = chunk.getBlock(15, i, z).getLocation();
                    if (loc.getBlock().getType()!=Material.AIR)continue;
                    player.sendBlockChange(loc, i==highestY?Material.STAINED_GLASS_PANE:Material.BARRIER, (byte)14);
                    changed.add(loc);
                }
            }
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if (player.isOnline()){
                    for(Location location : changed){
                        if (location.getWorld().getName().equals(player.getWorld().getName())){
                            player.sendBlockChange(location, location.getBlock().getType(), location.getBlock().getData());
                        }
                    }
                }

            }
        }.runTaskLater(Populace.getInstance(), 200L);

    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof PlotChunk)) {
            PlotChunk chunk = (PlotChunk) obj;
            return getWorld().getName().equals(chunk.getWorld().getName()) &&
                    getX() == chunk.getX() &&
                    getZ() == chunk.getZ();
        }
        return false;
    }

    public Chunk asBukkitChunk() {
        return getWorld().getChunkAt(getX(), getZ());
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("world", getWorld().getName());
        obj.put("x", getX());
        obj.put("z", getZ());
        return obj;
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return (int) x;
    }

    public int getZ() {
        return (int) z;
    }
}
