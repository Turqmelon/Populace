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

import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;

public class PopulaceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if ((sender instanceof Player)){
            Player player = (Player)sender;

            if (args.length == 0){
                player.sendMessage(Msg.INFO + "Populace Suite by (§3http://turqmelon.com§b)");
                player.sendMessage(Msg.INFO + "Loaded Modules:");
                player.sendMessage(Msg.INFO + " - " + ChatColor.WHITE + "PopulaceCore §c(Required)");
                for (Plugin plugin : Populace.getInstance().getServer().getPluginManager().getPlugins()) {
                    if (plugin.getName().startsWith("Populace") && plugin.isEnabled()) {
                        if (plugin.getName().equalsIgnoreCase("populace")) continue;
                        player.sendMessage(Msg.INFO + " - " + ChatColor.WHITE + plugin.getName());
                    }
                }
                player.sendMessage(Msg.INFO + "Stats:");
                player.sendMessage(Msg.INFO + " - Residents: " + ChatColor.WHITE + ResidentManager.getResidents().size());
                player.sendMessage(Msg.INFO + " - Towns: " + ChatColor.WHITE + TownManager.getTowns(false).size());

            } else if (args[0].equalsIgnoreCase("prefix") && player.hasPermission("populace.meta.prefix")) {
                if (args.length == 3) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        Resident resident = ResidentManager.getResident(target);
                        String prefix = args[2];
                        if (prefix.length() <= 13) {
                            if (prefix.equalsIgnoreCase("off")) {
                                resident.setPrefix(null);
                                player.sendMessage(Msg.OK + "Prefix removed.");
                            } else {
                                resident.setPrefix(prefix);
                                player.sendMessage(Msg.OK + "Prefix set to \"" + prefix + "\".");
                            }
                        } else {
                            player.sendMessage(Msg.ERR + "Prefix can't exceed 13 chars.");
                        }
                    } else {
                        player.sendMessage(Msg.ERR + "Target not found.");
                    }
                } else {
                    player.sendMessage(Msg.WARN + "Like this: §f/populace prefix <resident> <prefix|\"off\">");
                }
            }
            else if (args[0].equalsIgnoreCase("save") && player.hasPermission("populace.save")){
                player.sendMessage(Msg.OK + "Saving...");
                try {
                    Populace.saveData();
                    player.sendMessage(Msg.OK + "Save successful!");
                } catch (IOException e) {
                    player.sendMessage(Msg.ERR + e.getMessage() + " (Details in console.)");
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("bypass") && player.hasPermission("populace.bypass")) {
                Resident resident = ResidentManager.getResident(player);
                resident.setBypassMode(!resident.isBypassMode());
                resident.sendMessage(Msg.OK + "Bypass Mode: " + ChatColor.WHITE + (resident.isBypassMode() ? "Enabled. Be polite." : "Disabled"));
            }


        }

        return true;
    }
}
