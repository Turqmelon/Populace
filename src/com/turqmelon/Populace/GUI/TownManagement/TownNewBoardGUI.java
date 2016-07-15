package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownAnnouncement;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Collections;

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
public class TownNewBoardGUI extends TownGUI {

    private TownAnnouncement pendingAnnouncement;

    public TownNewBoardGUI(Resident resident, Town town, TownAnnouncement pendingAnnouncement) {
        super(resident, town);
        this.pendingAnnouncement = pendingAnnouncement;
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        int raw = event.getRawSlot();
        if (raw == 0 || raw == 4) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            TownBoardGUI gui = new TownBoardGUI(getResident(), getTown());
            gui.open(player);
        } else if (raw == 8) {
            TownAnnouncement announcement = new TownAnnouncement(pendingAnnouncement.getTitle(), pendingAnnouncement.getText(), pendingAnnouncement.getRequiredRank(), System.currentTimeMillis());
            getTown().getAnnouncements().add(announcement);
            Collections.sort(getTown().getAnnouncements());
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.closeInventory();
            getTown().sendTownBroadcast(announcement.getRequiredRank(), "New Post: " + announcement.getTitle() + " (/board to view)");
        } else if (raw == 23) {
            TownRank nextRank = null;
            TownRank rank = pendingAnnouncement.getRequiredRank();
            switch (rank) {
                case RESIDENT:
                    nextRank = TownRank.ASSISTANT;
                    break;
                case ASSISTANT:
                    nextRank = TownRank.MANAGER;
                    break;
                case MANAGER:
                    nextRank = TownRank.RESIDENT;
                    break;
            }

            if (nextRank != null) {
                pendingAnnouncement.setRequiredRank(nextRank);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                repopulate();
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
            }
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< CANCEL").build());
        inv.setItem(4, getTown().getIcon(IconType.MSGBOARD, getResident()));
        inv.setItem(8, new ItemBuilder(Material.EMERALD_BLOCK).withCustomName("§a§lCreate Post").build());

        inv.setItem(21, pendingAnnouncement.toIcon(getTown().getRank(getResident()), true));
        inv.setItem(23, new ItemBuilder(Material.STAINED_GLASS_PANE).withCustomName("§fWill be viewable by: " + pendingAnnouncement.getRequiredRank().getPrefix())
                .withLore(Arrays.asList("§aLeft Click§f to change.")).withData(pendingAnnouncement.getRequiredRank().getDyeColor()).build());


    }
}
