package com.turqmelon.Populace.Listeners;

import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Plot.RuinManager;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Utils.Configuration;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Creator: Devon
 * Project: Populace
 */
public class WorldListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!Configuration.DESTRUCTIVE_UNCLAIM) {
            return;
        }
        Chunk chunk = event.getChunk();
        PlotChunk plotChunk = new PlotChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
        if (RuinManager.isMarkedForRuin(plotChunk)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    RuinManager.blowup(plotChunk, Configuration.DESTRUCTIVE_UNCLAIM_EXPLOSIONS);
                }
            }.runTaskLater(Populace.getInstance(), 40L);
            RuinManager.getChunkList().remove(plotChunk);
        }
    }

}
