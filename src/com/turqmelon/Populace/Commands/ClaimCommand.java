package com.turqmelon.Populace.Commands;

import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Chunk;
import org.bukkit.World;
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

        if (!sender.hasPermission("populace.commands." + command.getName().toLowerCase())) {
            sender.sendMessage(Msg.ERR + "You don't have permission for that.");
            return true;
        }

        if ((sender instanceof Player)){
            Player player = (Player)sender;

            if (!Configuration.ALLOW_SUPERNATURAL_DIMENSIONS && player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                sender.sendMessage(Msg.ERR + "Claiming land isn't permitted in this world.");
                return true;
            }

            boolean warzone = false;
            boolean spawn = false;
            if (args.length > 0 && sender.hasPermission("populace.special.warzone.claim")) {
                warzone = args[args.length - 1].equalsIgnoreCase("warzone");
            }
            if (args.length > 0 && sender.hasPermission("populace.special.spawn.claim")) {
                spawn = args[args.length - 1].equalsIgnoreCase("spawn");
            }

            Resident resident = ResidentManager.getResident(player);
            if (resident != null){

                Town town = resident.getTown();

                if (warzone) {
                    town = TownManager.getWarzone();
                } else if (spawn) {
                    town = TownManager.getSpawn();
                }

                if (town != null){

                    if (args.length >= 1 && player.hasPermission("populace.commands.claim.radius")) {
                        try {
                            int radius = Integer.parseInt(args[0]);
                            if (radius < 1) {
                                throw new NumberFormatException();
                            }
                            if (radius > 5) {
                                player.sendMessage(Msg.ERR + "Radius can't be larger than 5.");
                                return true;
                            }
                            Chunk c = player.getLocation().getChunk();
                            int claimed = 0;
                            for (int i = 0; i < radius; i++) {
                                int minx = -1 * i;
                                int minz = -1 * i;
                                for (int x = minx; x <= i; x++) {
                                    for (int z = minz; z <= i; z++) {
                                        PlotChunk ch = new PlotChunk(c.getWorld(), c.getX() + x, c.getZ() + z);
                                        if (town.claimLand(warzone || spawn ? null : resident, ch, true)) {
                                            ch.visualize(player);
                                            claimed++;
                                        }
                                    }
                                }
                            }
                            if (claimed > 0) {
                                sender.sendMessage(Msg.OK + "Claimed " + claimed + (warzone || spawn ? " " + town.getName().toLowerCase() : "") + " chunk(s).");
                            } else {
                                sender.sendMessage(Msg.ERR + "Claimed no chunks.");
                            }
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(Msg.ERR + "Please enter a valid radius.");
                        }
                    } else {
                        Chunk c = player.getLocation().getChunk();
                        PlotChunk pc = new PlotChunk(c.getWorld(), c.getX(), c.getZ());
                        if (town.claimLand(warzone || spawn ? null : resident, pc, false)) {
                            pc.visualize(player);
                        }
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
