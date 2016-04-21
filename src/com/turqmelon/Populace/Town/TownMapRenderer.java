package com.turqmelon.Populace.Town;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

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
public class TownMapRenderer extends MapRenderer {

    private Town town;
    private static final MapView.Scale USED_SCALE = MapView.Scale.FARTHEST;
    public static int REGION_DIVIDE = 1;
    public static int PLAYER_MULTIPLY = 2;

    public TownMapRenderer(Town town) {
        this.town = town;
    }

    @Override
    public void render(MapView mv, MapCanvas mc, Player player) {

        if (getTown() == null) {
            return;
        }

        if (getTown().getSpawn() == null){
            return;
        }

        Resident resident = ResidentManager.getResident(player);
        if (resident == null){
            return;
        }

        mv.setScale(USED_SCALE);

        Location spawn = getTown().getSpawn();
        mv.setCenterX(Integer.MIN_VALUE);
        mv.setCenterZ(Integer.MIN_VALUE);
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                mc.setPixel(x, y, MapPalette.GRAY_1);
            }
        }
        MapCursorCollection cursors = mc.getCursors();
        while (cursors.size() > 0)
        {
            cursors.getCursor(0).setVisible(false);
            cursors.removeCursor(cursors.getCursor(0));
        }
        MapCursor spawnCursor = new MapCursor(new Byte("0"), new Byte("0"), new Byte("0"), MapCursor.Type.WHITE_CROSS.getValue(), true);
        cursors.addCursor(spawnCursor);

        MapCursor playerCursor = new MapCursor(new Byte("0"), new Byte("0"), new Byte("0"), MapCursor.Type.GREEN_POINTER.getValue(), true);
        cursors.addCursor(playerCursor);

        byte yaw = translate(player.getLocation().getYaw());

        playerCursor.setDirection(yaw);
        renderPlayer(player.getLocation(), playerCursor, spawn);
        for (Plot plot : getTown().getPlots()) {
            drawRegion(plot, spawn, mc, resident, getTown());
        }

        TownManager.getTowns().stream().filter(town -> !town.getUuid().equals(getTown().getUuid())).forEach(town -> {
            for (Plot plot : town.getPlots()) {
                drawRegion(plot, spawn, mc, resident, town);
            }
        });
        mc.setCursors(cursors);

        mc.drawText(2, 2, MinecraftFont.Font, getTown().getName());

    }

    private void drawRegion(Plot plot, Location l, MapCanvas mc, Resident resident, Town town)
    {

        PlotChunk ch = plot.getPlotChunk();
        Chunk chunk = ch.getWorld().getChunkAt(ch.getX(), ch.getZ());

        int endX = translateX(l, chunk.getBlock(0, 100, 0).getLocation().getBlockX()) / REGION_DIVIDE;
        int endZ = translateZ(l, chunk.getBlock(0, 100, 0).getLocation().getBlockZ()) / REGION_DIVIDE;
        int startX = translateX(l, chunk.getBlock(15, 100, 15).getLocation().getBlockX()) / REGION_DIVIDE;
        int startZ = translateZ(l, chunk.getBlock(15, 100, 15).getLocation().getBlockZ()) / REGION_DIVIDE;

        byte color;

        if (town.getUuid().equals(getTown().getUuid())){

            if (plot.getOwner() != null && plot.getOwner().getUuid().equals(resident.getUuid())){
                color = MapPalette.LIGHT_GREEN;
            }
            else if (plot.isForSale()){
                color = MapPalette.BLUE;
            }
            else{
                color = MapPalette.DARK_GREEN;
            }

        }
        else{
            color = MapPalette.BROWN;
        }

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                mc.setPixel(64 + x, 64 + z, color);
            }
        }
    }

    private byte translate(float yaw)
    {
        while (yaw < 0.0F) {
            yaw = 360.0F + yaw;
        }
        while (yaw >= 360.0F) {
            yaw -= 360.0F;
        }
        yaw = -yaw;
        yaw /= 22.5F;
        yaw -= 8.0F;
        while (yaw < 0.0F) {
            yaw = 15.0F - yaw;
        }
        while (yaw > 15.0F) {
            yaw -= 15.0F;
        }
        return (byte)(int)yaw;
    }

    private void renderPlayer(Location location, MapCursor cursor, Location spawn)
    {
        int x = translateX(spawn, location) * PLAYER_MULTIPLY;
        int z = translateZ(spawn, location) * PLAYER_MULTIPLY;
        if ((!isByte(x)) || (!isByte(z)))
        {
            cursor.setVisible(false);
            return;
        }
        cursor.setVisible(true);

        cursor.setX((byte)x);
        cursor.setY((byte)z);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean isByte(Object o)
    {
        try
        {
            Byte.parseByte(o.toString());
            return true;
        }
        catch (Exception ignored) {}
        return false;
    }

    private int translateX(Location spawn, Location toTranslate)
    {
        return translateX(spawn, toTranslate.getX());
    }

    private int translateZ(Location spawn, Location toTranslate)
    {
        return translateZ(spawn, toTranslate.getZ());
    }

    private int translateX(Location spawn, double to)
    {
        return translate(spawn.getX(), (int)to);
    }

    private int translateZ(Location spawn, double to)
    {
        return translate(spawn.getZ(), (int)to);
    }

    private int translateX(Location spawn, int to)
    {
        return translate(spawn.getX(), to);
    }

    private int translateZ(Location spawn, int to)
    {
        return translate(spawn.getZ(), to);
    }

    private int translate(double spawn, int to)
    {
        double diff = spawn - to;
        diff = Math.floor(diff / USED_SCALE.getValue());
        return (int)diff;
    }

    public Town getTown() {
        return town;
    }
}
