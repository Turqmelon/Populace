package com.turqmelon.Populace.Utils;

import org.bukkit.Location;
import org.bukkit.Material;
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
public class CheckForPortalTrapTask implements Runnable
{
    //player who recently teleported via nether portal
    private Player player;

    //where to send the player back to if he hasn't left the portal frame
    private Location returnLocation;

    public CheckForPortalTrapTask(Player player, Location location)
    {
        this.player = player;
        this.returnLocation = location;
    }

    @Override
    public void run()
    {
        //if player has logged out, do nothing
        if(!this.player.isOnline()) return;

        //otherwise if still standing in a portal frame, teleport him back through
        if(this.player.getLocation().getBlock().getType() == Material.PORTAL)
        {
            this.player.teleport(this.returnLocation);
        }
    }
}