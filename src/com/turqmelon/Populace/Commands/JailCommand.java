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
import com.turqmelon.Populace.Utils.ClockUtil;
import com.turqmelon.Populace.Utils.JailData;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JailCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("populace.commands." + command.getName().toLowerCase())) {
            sender.sendMessage(Msg.ERR + "You don't have permission for that.");
            return true;
        }
        if ((sender instanceof Player)) {
            Player player = (Player) sender;
            Resident resident = ResidentManager.getResident(player);
            if (resident != null) {

                if (resident.getTown() != null && resident.getTown().getRank(resident).isAtLeast(TownRank.MANAGER)) {

                    if (resident.getTown().getTownJail() != null) {

                        if (args.length >= 1) {

                            Resident target = ResidentManager.getResident(args[0]);
                            if (target != null && target.getTown() != null && target.getTown().getUuid().equals(resident.getTown().getUuid())) {

                                if (target.getTown().getRank(target) == TownRank.RESIDENT) {

                                    if (!target.isJailed()) {

                                        long expiration = -1;
                                        if (args.length == 2) {
                                            try {
                                                expiration = ClockUtil.parseDateDiff(args[1], true);
                                            } catch (Exception e) {
                                                sender.sendMessage(Msg.ERR + "Invalid time.");
                                                return true;
                                            }
                                        }


                                        if (expiration != -1 || resident.getTown().getRank(resident) == TownRank.MAYOR) {
                                            target.setJailData(new JailData(resident.getTown().getTownJail(), 16, expiration, resident.getUuid()));
                                            target.getJailData().sendExplanation(target);
                                            resident.getTown().sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " jailed " + target.getName() + "!");
                                        } else {
                                            sender.sendMessage(Msg.ERR + "Only the mayor can jail forever.");
                                            sender.sendMessage(Msg.ERR + "Please specify a time, or ask the mayor to do it.");
                                        }

                                    } else {
                                        sender.sendMessage(Msg.ERR + "Target is already jailed. Perhaps use §f/unjail§c?");
                                    }

                                } else {
                                    sender.sendMessage(Msg.ERR + "Only Residents can be jailed.");
                                }

                            } else {
                                sender.sendMessage(Msg.ERR + "Town has no residents named " + args[0] + ".");
                            }

                        } else {
                            sender.sendMessage(Msg.WARN + "Like this: §f/jail <Resident> [Time]");
                            sender.sendMessage(Msg.WARN + "Leave §f[Time]§6 blank for forever.");
                            sender.sendMessage(Msg.WARN + "You can only jail town residents. If a resident leaves your town while jailed, they will be unjailed.");
                        }

                    } else {
                        player.sendMessage(Msg.ERR + "A town jail has not been set.");
                        if (resident.getTown().getRank(resident).isAtLeast(TownRank.MAYOR)) {
                            player.sendMessage(Msg.ERR + "Set it with §f/setjail§c.");
                        } else {
                            player.sendMessage(Msg.ERR + "Ask Mayor " + resident.getTown().getMayor().getName() + " to set the jail.");
                        }
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
