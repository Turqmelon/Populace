package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownAnnouncement;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

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
public class TownBoardGUI extends TownGUI {
    public TownBoardGUI(Resident resident, Town town) {
        super(resident, town);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        int raw = event.getRawSlot();
        if (raw == 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            TownGUI gui = new TownGUI(getResident(), getTown());
            gui.open(player);
        } else if (raw == 8 && getTown().getAnnouncements().size() < 21 && getTown().getRank(getResident()).isAtLeast(TownRank.MAYOR)) {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();

            player.sendMessage(Msg.INFO + "What's the title of your post?");
            player.sendMessage(Msg.INFO + "§fJust type it into chat. Max chars: 32 (\"cancel\" to cancel.)");
            getResident().setPendingAnnouncement(new TownAnnouncement(null, null, TownRank.RESIDENT, 0));

        } else if (event.isRightClick() && event.isShiftClick() && getTown().getRank(getResident()).isAtLeast(TownRank.MAYOR)) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() != Material.AIR) {
                NBTTagString nbt = (NBTTagString) ItemUtil.getTag(item, "announcementid");
                if (nbt != null) {
                    UUID announcementID = UUID.fromString(nbt.toString().replace("\"", ""));
                    TownAnnouncement announcement = null;
                    for (TownAnnouncement a : getTown().getAnnouncements()) {
                        if (a.getUuid().equals(announcementID)) {
                            announcement = a;
                            break;
                        }
                    }
                    if (announcement != null) {
                        getTown().getAnnouncements().remove(announcement);
                        repopulate();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        player.sendMessage(Msg.OK + "Announcement deleted!");
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                    }
                }
            }
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, getTown().getIcon(IconType.MSGBOARD, getResident()));

        int index = 19;
        TownRank rank = getTown().getRank(getResident());

        for (TownAnnouncement announcement : getTown().getAnnouncements()) {
            if (rank.isAtLeast(announcement.getRequiredRank())) {
                inv.setItem(index, announcement.toIcon(rank));
            }
            index++;
            if (index == 26) {
                index = 28;
            } else if (index == 35) {
                index = 37;
            } else if (index == 44) {
                break;
            }
        }

        if (rank.isAtLeast(TownRank.MAYOR) && getTown().getAnnouncements().size() < 21) {
            inv.setItem(8, new ItemBuilder(Material.FEATHER)
                    .withCustomName("§a§lCreate New Post")
                    .withLore(Arrays.asList("§aLeft Click§f to create a new post.")).build());
        }

    }
}
