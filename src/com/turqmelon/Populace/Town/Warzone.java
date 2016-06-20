package com.turqmelon.Populace.Town;

import com.turqmelon.Populace.Plot.PlotChunk;

import java.util.UUID;

/**
 * Creator: Devon
 * Project: Populace
 */
public class Warzone extends Town {

    public Warzone(UUID uuid) {
        super(uuid, "Warzone", TownLevel.METROPOLIS);
    }

    public Warzone() {
        super(UUID.randomUUID(), "Warzone", TownLevel.METROPOLIS);
    }

    public boolean claimLand(PlotChunk chunk) {
        return super.buyChunk(null, chunk, false);
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public long getMaxLand() {
        return Long.MAX_VALUE;
    }

    @Override
    public double getDailyUpkeep() {
        return 0;
    }
}
