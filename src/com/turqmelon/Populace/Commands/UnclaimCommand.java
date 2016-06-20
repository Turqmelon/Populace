package com.turqmelon.Populace.Commands;

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

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnclaimCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("populace.commands." + command.getName().toLowerCase())) {
            sender.sendMessage(Msg.ERR + "You don't have permission for that.");
            return true;
        }
        if ((sender instanceof Player)){
            Player player = (Player)sender;
            Resident resident = ResidentManager.getResident(player);
            if (resident != null){

                boolean warzone = false;
                boolean spawn = false;
                if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("warzone") && sender.hasPermission("populace.special.warzone.unclaim")) {
                    warzone = true;
                } else if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("spawn") && sender.hasPermission("populace.special.spawn.unclaim")) {
                    spawn = true;
                }

                Town town = resident.getTown();

                if (warzone) {
                    town = TownManager.getWarzone();
                } else if (spawn) {
                    town = TownManager.getSpawn();
                }

                if (town != null){

                    Chunk c = player.getLocation().getChunk();
                    Plot plot = PlotManager.getPlot(c);
                    if (town.unclaimLand(warzone || spawn ? null : resident, plot) && (warzone || spawn)) {
                        sender.sendMessage(Msg.OK + "Unclaimed " + town.getName().toLowerCase() + " plot.");
                    }

                }
                else{
                    player.sendMessage(Msg.ERR + "You must be part of a town to unclaim land!");
                }

            }
            else{
                player.sendMessage(Msg.ERR + "No resident data.");
            }

        }

        return true;
    }
}
