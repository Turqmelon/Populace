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

import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnjailCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if ((sender instanceof Player)) {
            Player player = (Player) sender;
            Resident resident = ResidentManager.getResident(player);
            if (resident != null) {

                if (resident.getTown() != null && resident.getTown().getRank(resident).isAtLeast(TownRank.MANAGER)) {


                    if (args.length == 1) {

                        Resident target = ResidentManager.getResident(args[0]);
                        if (target != null && target.getTown() != null && target.getTown().getUuid().equals(resident.getTown().getUuid())) {

                            if (target.isJailed()) {

                                target.setJailData(null);
                                if (resident.getTown().getSpawn() != null) {
                                    Player pl = Bukkit.getPlayer(resident.getUuid());
                                    if (pl != null && pl.isOnline()) {
                                        pl.teleport(resident.getTown().getSpawn());
                                    }
                                }
                                resident.getTown().sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " released " + target.getName() + " from jail.");

                            } else {
                                sender.sendMessage(Msg.ERR + "Target is not jailed.");
                            }

                        } else {
                            sender.sendMessage(Msg.ERR + "Town has no residents named " + args[0] + ".");
                        }

                    } else {
                        sender.sendMessage(Msg.WARN + "Like this: §f/unjail <Resident>");
                    }
                } else {
                    player.sendMessage(Msg.ERR + "You must be a manager of a town to use that.");
                }

            } else {
                player.sendMessage(Msg.ERR + "No resident data.");
            }

        }

        return true;
    }
}
