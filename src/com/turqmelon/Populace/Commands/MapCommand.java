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
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand implements CommandExecutor {
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

                if (resident.getTown() != null){

                    if (resident.getTown().getMapView() != null){

                        player.getInventory().addItem(new ItemBuilder(Material.MAP).withCustomName("§e§lMap of " + resident.getTown().getName()).withData(resident.getTown().getMapView().getId()).tagWith("townname", new NBTTagString(resident.getTown().getUuid().toString())).build());

                        player.sendMessage(Msg.OK + "Here's a map of " + resident.getTown().getName() + resident.getTown().getLevel().getSuffix() + "!");

                    }
                    else{
                        player.sendMessage(Msg.ERR + "No map available! Claim some land first!");
                    }

                }
                else{
                    player.sendMessage(Msg.ERR + "You must be in a town!");
                }

            }
            else{
                player.sendMessage(Msg.ERR + "No resident data.");
            }

        }

        return true;
    }
}
