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
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class PopulaceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if ((sender instanceof Player)){
            Player player = (Player)sender;

            if (args.length == 0){
                player.sendMessage(Msg.INFO + "Populace version " + Populace.getInstance().getDescription().getVersion() + " by Turqmelon");
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
            }


        }

        return true;
    }
}
