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
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InviteCommand implements CommandExecutor {
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

                        if (args.length == 1){

                            Player t = Bukkit.getPlayer(args[0]);
                            if (t != null && t.isOnline()){

                                Resident target = ResidentManager.getResident(t);
                                if (target != null){

                                    if (target.getTown() == null){

                                        if (!target.getTownInvites().containsKey(town.getUuid())){
                                            target.getTownInvites().put(town.getUuid(), resident);
                                            town.sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " invited " + target.getName() + " to join the town!");
                                            target.sendMessage(Msg.INFO + "You were invited to join " + town.getName() + town.getLevel().getSuffix() + " by " + resident.getName() + "!");
                                            target.sendMessage(Msg.INFO + "To accept this invitation, type §f/join " + town.getName() + "§b.");
                                        }
                                        else{
                                            target.getTownInvites().remove(town.getUuid());
                                            town.sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " revoked " + target.getName() + "'s invitation to town.");
                                        }

                                    }
                                    else{
                                        player.sendMessage(Msg.ERR + target.getName() + " already belongs to a town.");
                                    }

                                }
                                else {
                                    player.sendMessage(Msg.ERR + "Target has no resident data.");
                                }

                            }
                            else{
                                player.sendMessage(Msg.ERR + "To invite a player, they must be online.");
                            }

                        }
                        else{
                            player.sendMessage(Msg.WARN + "Like this: §f/invite <Player>");
                        }

                    }
                    else{
                        player.sendMessage(Msg.ERR + "Only Managers can invite players to town.");
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
