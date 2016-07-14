package com.turqmelon.Populace.Plot;

import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Town.Town;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creator: Devon
 * Project: Populace
 */
public class RuinManager {

    private static List<PlotChunk> chunkList = new ArrayList<>();

    public static void blowup(PlotChunk plotChunk, int explosions) {
        Chunk chunk = plotChunk.getWorld().getChunkAt(plotChunk.getX(), plotChunk.getZ());

        List<Location> dropLocations = new ArrayList<>();
        Random r = new Random();

        for (int i = 0; i < explosions; i++) {
            int x = r.nextInt(16);
            int z = r.nextInt(16);
            Location location = chunk.getBlock(x, 255, z).getLocation();
            int highestY = location.getWorld().getHighestBlockYAt(location) + 1;
            location.setY(highestY);
            dropLocations.add(location);
        }

        for (Location location : dropLocations) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
                    tnt.setGlowing(true);
                    tnt.setIsIncendiary(true);
                    tnt.setYield(tnt.getYield() * 2);
                }
            }.runTaskLater(Populace.getInstance(), 20 + (r.nextInt(3) * 20));
        }
    }

    public static void markRuined(Town town) {
        for (Plot plot : town.getPlots()) {
            markRuined(plot.getPlotChunk());
        }
    }

    public static void markRuined(Chunk chunk) {
        markRuined(new PlotChunk(chunk.getWorld(), chunk.getX(), chunk.getZ()));
    }

    public static void markRuined(PlotChunk plotChunk) {
        if (!isMarkedForRuin(plotChunk)) {
            getChunkList().add(plotChunk);
        }
    }

    public static boolean isMarkedForRuin(PlotChunk chunk) {
        for (PlotChunk plotChunk : getChunkList()) {
            if (plotChunk.equals(chunk)) {
                return true;
            }
        }
        return false;
    }

    public static List<PlotChunk> getChunkList() {
        return chunkList;
    }
}
