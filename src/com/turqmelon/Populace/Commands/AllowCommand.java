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
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllowCommand implements CommandExecutor {
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

                Town town = resident.getTown();
                if (town != null){

                    Plot plot = PlotManager.getPlot(player.getLocation().getChunk());

                    if (plot != null && plot.getTown().getUuid().equals(resident.getTown().getUuid())){

                        if (plot.getOwner() == null || plot.getOwner().getUuid().equals(resident.getUuid())){

                            if (args.length == 1){

                                Player t = Bukkit.getPlayer(args[0]);
                                if (t != null && t.isOnline()){

                                    Resident target = ResidentManager.getResident(t);
                                    if (target != null){

                                        if (!target.getUuid().equals(resident.getUuid())){

                                            if (plot.getAllowList().size() < 36){

                                                if (!plot.isOnAllowList(target)){

                                                    plot.addToAllowList(target);
                                                    player.sendMessage(Msg.OK + target.getName() + " was added to the allow list for this plot.");

                                                }
                                                else{
                                                    player.sendMessage(Msg.ERR + target.getName() + " is already allowed here.");
                                                    player.sendMessage(Msg.ERR + "If you'd like to remove them, use the §f/plot§c menu.");
                                                }

                                            }
                                            else{
                                                player.sendMessage(Msg.ERR + "You can't allow more than 36 residents.");
                                            }

                                        }
                                        else{
                                            player.sendMessage(Msg.ERR + "You can't allow yourself.");
                                        }

                                    }
                                    else{
                                        player.sendMessage(Msg.ERR + "Target has no resident data.");
                                    }

                                }
                                else{
                                    player.sendMessage(Msg.ERR + "To add a player to the allow list, they must be online.");
                                }

                            }
                            else{
                                player.sendMessage(Msg.WARN + "Like this: §f/allow <Player>");
                            }

                        }
                        else{
                            player.sendMessage(Msg.ERR + "This plot belongs to " + plot.getOwner().getName() + ".");
                        }

                    }
                    else{
                        player.sendMessage(Msg.ERR + "Stand inside the plot you want to allow a player access to.");
                    }

                }
                else{
                    player.sendMessage(Msg.ERR + "You must be part of a town!");
                }

            }
            else{
                player.sendMessage(Msg.ERR + "No resident data.");
            }

        }

        return true;
    }
}
