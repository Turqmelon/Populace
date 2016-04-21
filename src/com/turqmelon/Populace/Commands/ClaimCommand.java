package com.turqmelon.Populace.Commands;

import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
public class ClaimCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if ((sender instanceof Player)){
            Player player = (Player)sender;
            Resident resident = ResidentManager.getResident(player);
            if (resident != null){

                Town town = resident.getTown();
                if (town != null){

                    Chunk c = player.getLocation().getChunk();
                    PlotChunk pc = new PlotChunk(c.getWorld(), c.getX(), c.getZ());
                    if (town.claimLand(resident, pc)){
                        pc.visualize(player);
                    }

                }
                else{
                    player.sendMessage(Msg.ERR + "You must be part of a town to claim land!");
                }

            }
            else{
                player.sendMessage(Msg.ERR + "No resident data.");
            }

        }

        return true;
    }
}
