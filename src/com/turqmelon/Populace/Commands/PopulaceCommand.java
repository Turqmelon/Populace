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
import com.turqmelon.Populace.Utils.Msg;
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
                player.sendMessage(Msg.INFO + "Populace Suite by (§3§nhttp://turqmelon.com§b)");
                for (Plugin plugin : Populace.getInstance().getServer().getPluginManager().getPlugins()) {
                    if (plugin.getName().startsWith("Populace") && plugin.isEnabled()) {
                        player.sendMessage(Msg.INFO + plugin.getName() + " version " + plugin.getDescription().getVersion());
                    }
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
