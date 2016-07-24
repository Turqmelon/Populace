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

import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownFlag;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ClockUtil;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GrantFlightCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("populace.commands." + command.getName().toLowerCase())) {
            sender.sendMessage(Msg.ERR + "You don't have permission for that.");
            return true;
        }
        if (args.length == 2) {
            Town town = TownManager.getTown(args[0]);
            if (town != null) {
                if (!town.isFlagActive(TownFlag.FLIGHT)) {
                    try {
                        long expire = ClockUtil.parseDateDiff(args[1], true);
                        town.getActiveFlags().put(TownFlag.FLIGHT, expire);
                        sender.sendMessage(Msg.OK + "Granted flight to " + town.getName() + town.getLevel().getSuffix() + ".");
                        town.sendTownBroadcast(TownRank.RESIDENT, "You can fly within town borders! Granted by " + sender.getName() + " for " + args[1] + ".");
                    } catch (Exception e) {
                        sender.sendMessage(Msg.ERR + "Incorrect time format.");
                    }

                } else {
                    sender.sendMessage(Msg.ERR + town.getName() + town.getLevel().getSuffix() + " already has the ability to fly.");
                }

            } else {
                sender.sendMessage(Msg.ERR + "Town could not be found.");
            }

        } else {
            sender.sendMessage(Msg.WARN + "Like this: §f/grantFlight <Town> <Time>");
        }

        return true;
    }
}
