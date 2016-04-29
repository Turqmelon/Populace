package com.turqmelon.Populace.Commands;

/******************************************************************************
 * *
 * CONFIDENTIAL                                                               *
 * __________________                                                         *
 * *
 * [2012 - 2016] Devon "Turqmelon" Thome                                      *
 * All Rights Reserved.                                                      *
 * *
 * NOTICE:  All information contained herein is, and remains                  *
 * the property of Turqmelon and its suppliers,                               *
 * if any.  The intellectual and technical concepts contained                 *
 * herein are proprietary to Turqmelon and its suppliers and                  *
 * may be covered by U.S. and Foreign Patents,                                *
 * patents in process, and are protected by trade secret or copyright law.    *
 * Dissemination of this information or reproduction of this material         *
 * is strictly forbidden unless prior written permission is obtained          *
 * from Turqmelon.                                                            *
 * *
 ******************************************************************************/

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetJailCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("populace.commands." + command.getName().toLowerCase())) {
            sender.sendMessage(Msg.ERR + "You don't have permission for that.");
            return true;
        }
        if ((sender instanceof Player)) {
            Player player = (Player) sender;
            Resident resident = ResidentManager.getResident(player);
            if (resident != null) {

                if (resident.getTown() != null && resident.getTown().getRank(resident).isAtLeast(TownRank.MAYOR)) {

                    Plot plot = PlotManager.getPlot(player.getLocation().getChunk());

                    if (plot != null && plot.getTown().getUuid().equals(resident.getTown().getUuid())) {
                        resident.getTown().setTownJail(player.getLocation());
                        player.sendMessage(Msg.OK + "Town jail set!");
                    } else {
                        player.sendMessage(Msg.ERR + "Town jails must be placed in your own land.");
                    }

                } else {
                    player.sendMessage(Msg.ERR + "You must be the mayor of a town to use that.");
                }

            } else {
                player.sendMessage(Msg.ERR + "No resident data.");
            }

        }

        return true;
    }
}
