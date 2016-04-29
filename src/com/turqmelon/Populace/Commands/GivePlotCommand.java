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
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GivePlotCommand implements CommandExecutor {
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

                    TownRank rank = town.getRank(resident);

                    if (rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()){

                        Plot plot = PlotManager.getPlot(player.getLocation().getChunk());

                        if (plot != null && plot.getTown().getUuid().equals(resident.getTown().getUuid())){

                            if (plot.getOwner() == null){

                                if (args.length == 1){

                                    Player t = Bukkit.getPlayer(args[0]);
                                    if (t != null && t.isOnline()){

                                        Resident target = ResidentManager.getResident(t);
                                        if (target != null && target.getTown() != null && target.getTown().getUuid().equals(resident.getTown().getUuid())){

                                            if (!plot.getInvited().contains(target.getUuid())){
                                                plot.getInvited().add(target.getUuid());
                                                player.sendMessage(Msg.OK + "You invited " + t.getName() + " to take this plot.");
                                                player.sendMessage(Msg.OK + "Have them come to the plot and type §f/claim§a to accept it.");
                                                target.sendMessage(Msg.INFO + player.getName() + " has invited you to claim a plot.");
                                                target.sendMessage(Msg.INFO + "When you get to it, type §f/claim§b to accept it.");

                                            }
                                            else{
                                                player.sendMessage(Msg.ERR + t.getName() + " has already been invited to take this plot.");
                                            }

                                        }
                                        else {
                                            player.sendMessage(Msg.ERR + t.getName() + " isn't in " + town.getName() + town.getLevel().getSuffix() + ".");
                                        }

                                    }
                                    else{
                                        player.sendMessage(Msg.ERR + "To give a plot to a player, they must be online.");
                                    }

                                }
                                else{
                                    player.sendMessage(Msg.WARN + "Like this: §f/giveplot <Player>");
                                }

                            }
                            else{
                                player.sendMessage(Msg.ERR + "This plot belongs to " + plot.getOwner().getName() + ".");
                                player.sendMessage(Msg.ERR + "To give it to someone else, ask them to §f/unclaim§c it, or kick them from the town.");
                            }

                        }
                        else{
                            player.sendMessage(Msg.ERR + "Stand inside the plot you want to give.");
                        }

                    }
                    else{
                        player.sendMessage(Msg.ERR + "Only Managers can give plots to players.");
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
