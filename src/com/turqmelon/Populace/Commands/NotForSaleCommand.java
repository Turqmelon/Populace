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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NotForSaleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if ((sender instanceof Player)){
            Player player = (Player)sender;
            Resident resident = ResidentManager.getResident(player);
            if (resident != null){

                Town town = resident.getTown();
                if (town != null){

                    TownRank rank = town.getRank(resident);

                    if (rank.getPermissionLevel() >= TownRank.ASSISTANT.getPermissionLevel()){

                        Plot plot = PlotManager.getPlot(player.getLocation().getChunk());

                        if (plot != null && plot.getTown().getUuid().equals(resident.getTown().getUuid())){

                            if (plot.isForSale()){
                                plot.setPrice(0);
                                plot.setForSale(false);
                                resident.sendMessage(Msg.OK + "Plot no longer for sale.");
                            }
                            else{
                                player.sendMessage(Msg.ERR + "This plot is not for sale.");
                            }

                        }
                        else{
                            player.sendMessage(Msg.ERR + "Stand inside the plot you want to stop selling.");
                        }

                    }
                    else{
                        player.sendMessage(Msg.ERR + "Only Assistants can mark plots as not for sale.");
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
