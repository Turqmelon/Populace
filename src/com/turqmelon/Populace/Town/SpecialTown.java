package com.turqmelon.Populace.Town;

import com.turqmelon.Populace.Plot.PlotChunk;

import java.util.UUID;

/**
 * Creator: Devon
 * Project: Populace
 */
public class SpecialTown extends Town {

    public SpecialTown(UUID uuid, String name) {
        super(uuid, name, TownLevel.METROPOLIS);
    }

    public SpecialTown(String name) {
        super(UUID.randomUUID(), name, TownLevel.METROPOLIS);
    }

    public boolean claimLand(PlotChunk chunk) {
        return super.buyChunk(null, chunk, false);
    }

    @Override
    public boolean isSpecial() {
        return true;
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
