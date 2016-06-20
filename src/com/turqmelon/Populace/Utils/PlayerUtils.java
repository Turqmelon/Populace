package com.turqmelon.Populace.Utils;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Town.Town;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

/**
 * Creator: Devon
 * Project: Populace
 */
public class PlayerUtils {

    public static void teleportPlayerRandomly(Player player, Town town) {
        Location target = getRandomTeleportLocationIn(town);
        if (target != null) {
            if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            }
            if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (20 * 20), 50));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (20 * 20), 1));
            player.teleport(target);
            player.sendMessage(Msg.OK + "Geronimo!");
        } else {
            player.sendMessage(Msg.ERR + "Couldn't find a safe destination.");
        }
    }

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
