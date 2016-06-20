package com.turqmelon.Populace.Utils;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Town.Town;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.List;
import java.util.Random;

/**
 * Creator: Devon
 * Project: Populace
 */
public class PlayerUtils {

    public static Location getRandomTeleportLocationIn(Town town) {
        List<Plot> plots = town.getPlots();
        if (plots.isEmpty()) return null;
        Random r = new Random();
        Plot plot = plots.get(r.nextInt(plots.size()));

        Chunk chunk = plot.getPlotChunk().asBukkitChunk();
        int x = r.nextInt(16);
        int z = r.nextInt(16);

        return chunk.getBlock(x, 250, z).getLocation();
    }

}
