package com.turqmelon.Populace.Utils;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotType;
import com.turqmelon.PopulaceWar.listeners.CombatListener;
import com.turqmelon.PopulaceWar.utils.CombatTag;
import org.bukkit.entity.Player;

/**
 * Creator: Devon
 * Project: Populace
 */
public class CombatHelper {

    public static boolean shouldBounceBack(Player player, Plot to, Plot from) {

        CombatTag tag = CombatListener.getTag(player);
        if (tag != null) {

            boolean originPVP = from != null ? from.getType() == PlotType.BATTLE : Configuration.WILDERNESS_PVP;
            boolean targetPVP = to != null ? to.getType() == PlotType.BATTLE : Configuration.WILDERNESS_PVP;

            if (originPVP) {
                return !targetPVP;
            }

        }

        return false;
    }

}
