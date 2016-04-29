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

import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.Msg;
import com.turqmelon.Populace.Utils.PopulaceTeleport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VisitCommand implements CommandExecutor {
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

                Town town;

                if (args.length == 0 && resident.getTown() != null){
                    town = resident.getTown();
                } else if (args.length >= 1 && sender.hasPermission("populace.commands.visit.other")) {

                    town = TownManager.getTown(args[0]);

                }
                else{
                    player.sendMessage(Msg.WARN + "Like this: §f/visit <Town>");
                    return true;
                }

                if (town != null){

                    if (town.canWarpToSpawn(resident, true)){
                        new PopulaceTeleport(player, town.getSpawn(), player.getLocation(), Configuration.TELEPORT_WARMUP_TIME, Configuration.TELEPORT_COOLDOWN_TIME, false);
                    }

                }
                else{
                    player.sendMessage(Msg.ERR + "Town could not be found.");
                }

            }
            else{
                player.sendMessage(Msg.ERR + "No resident data.");
            }

        }

        return true;
    }
}
