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
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForSaleCommand implements CommandExecutor {
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

                            if (plot.getOwner() == null){

                                if (args.length == 1){

                                    try {

                                        double price;

                                        if (Populace.getCurrency().isDecimalSupported()){
                                            price = Double.parseDouble(args[0]);
                                        }
                                        else{
                                            price = Integer.parseInt(args[0]);
                                        }

                                        if (price <= 0){
                                            throw new NumberFormatException();
                                        }

                                        plot.setForSale(true);
                                        plot.setPrice(price);
                                        town.sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " put a plot up for sale for " + Populace.getCurrency().format(price) + ".");

                                    }catch(NumberFormatException ex){
                                        player.sendMessage(Msg.ERR + "Please supply a valid price.");
                                    }

                                }
                                else{
                                    player.sendMessage(Msg.WARN + "Like this: §f/forsale <Price>");
                                }

                            }
                            else{
                                player.sendMessage(Msg.ERR + "This plot belongs to " + plot.getOwner().getName() + ".");
                                player.sendMessage(Msg.ERR + "To sell it, ask them to §f/unclaim§c it, or kick them from the town.");
                            }

                        }
                        else{
                            player.sendMessage(Msg.ERR + "Stand inside the plot you want to sell.");
                        }

                    }
                    else{
                        player.sendMessage(Msg.ERR + "Only Assistants can mark plots as for sale.");
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
