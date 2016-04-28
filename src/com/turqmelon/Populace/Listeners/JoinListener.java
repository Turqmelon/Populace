package com.turqmelon.Populace.Listeners;

import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
public class JoinListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        if (!Populace.isFullyEnabled()){
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Msg.ERR + "Not everything is loaded yet!");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        if (resident == null){
            resident = new Resident(player.getUniqueId(), player.getName());
            resident.setJoined(System.currentTimeMillis());
            ResidentManager.getResidents().add(resident);
        }
        else if (!resident.getName().equals(player.getName())){
            resident.setName(player.getName());
        }

        resident.setSeen(System.currentTimeMillis());

        final Resident finalResident = resident;
        new BukkitRunnable(){

            @Override
            public void run() {
                player.sendMessage(Msg.INFO + "This server runs §lPopulace§b by Turqmelon!");
                if (finalResident.isJailed()) {
                    finalResident.getJailData().sendExplanation(finalResident);
                }
            }
        }.runTaskLater(Populace.getInstance(), 20L);

    }

}
