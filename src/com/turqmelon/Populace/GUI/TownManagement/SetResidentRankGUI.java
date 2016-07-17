package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.Events.Resident.ResidentRankChangedEvent;
import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

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
public class SetResidentRankGUI extends TownGUI {

    private Resident target;

    public SetResidentRankGUI(Resident resident, Town town, Resident target) {
        super(resident, town);
        this.target = target;
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        TownRank rank = getTown().getRank(getResident());
        if (!rank.isAtLeast(TownRank.MANAGER)) {
            player.closeInventory();
            return;
        }

        if (raw == 0){
            TownGUI gui = new TownGUI(getResident(), getTown());
            gui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() != Material.AIR) {
            NBTBase nbt = ItemUtil.getTag(clicked, "changerank");
            if (nbt != null) {
                TownRank newRank = TownRank.valueOf(nbt.toString().replace("\"", ""));
                if (newRank == TownRank.MAYOR) {
                    Player target = Bukkit.getPlayer(getTarget().getUuid());

                    if (target != null && target.isOnline()) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        player.closeInventory();
                        player.sendMessage(Msg.INFO + "Are you sure you want to give up " + getTown().getName() + getTown().getLevel().getSuffix() + "?");
                        player.sendMessage(Msg.INFO + "This action is not reversible, and you will be giving " + getTarget().getName() + " full control of it.");
                        player.sendMessage(Msg.INFO + "Upon confirmation, " + getTarget().getName() + " will be promoted and you will be made a Manager.");
                        getResident().setPendingAction(() -> {
                            getTown().transferOwnership(getTarget());
                        });
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                    }
                } else {
                    getTown().getResidents().put(getTarget(), newRank);
                    getTown().sendTownBroadcast(TownRank.RESIDENT, getTarget().getName() + "'s rank has been set to " + newRank.getPrefix() + "§dby the mayor.");
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    Bukkit.getPluginManager().callEvent(new ResidentRankChangedEvent(getTarget(), newRank));
                    repopulate();
                }
            }
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();
        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, getIcon(getTarget()));

        TownRank rank = getTown().getRank(getTarget());

        if (getTown().getRank(getResident()) == TownRank.MAYOR && rank != TownRank.MAYOR) {

            if (rank == TownRank.RESIDENT){
                inv.setItem(19, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.RESIDENT.getDyeColor())
                        .withCustomName(TownRank.RESIDENT.getPrefix()).withLore(Arrays.asList("§aCurrent Rank")).makeItGlow().build());
            }
            else{
                inv.setItem(19, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.RESIDENT.getDyeColor())
                        .withCustomName(TownRank.RESIDENT.getPrefix()).withLore(
                                Arrays.asList(
                                        "§fNo special permissions."
                                )
                        ).tagWith("changerank", new NBTTagString(TownRank.RESIDENT.name())).build());
            }

            if (rank == TownRank.ASSISTANT){
                inv.setItem(21, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.ASSISTANT.getDyeColor())
                        .withCustomName(TownRank.ASSISTANT.getPrefix()).withLore(Arrays.asList("§aCurrent Rank")).makeItGlow().build());
            }
            else{
                inv.setItem(21, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.ASSISTANT.getDyeColor())
                        .withCustomName(TownRank.ASSISTANT.getPrefix()).withLore(
                                Arrays.asList(
                                        "§fBasic Permissions:",
                                        "§8 - §eEverything from Resident",
                                        "§8 - §bCan receive town staff alerts",
                                        "§8 - §bCan mark plots for sale",
                                        "§8 - §bCan claim land for the town",
                                        "§8 - §bCan unclaim unowned town land"
                                )
                        ).tagWith("changerank", new NBTTagString(TownRank.ASSISTANT.name())).build());
            }

            if (rank == TownRank.MANAGER){
                inv.setItem(23, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.MANAGER.getDyeColor())
                        .withCustomName(TownRank.MANAGER.getPrefix()).withLore(Arrays.asList("§aCurrent Rank")).makeItGlow().build());
            }
            else{
                inv.setItem(23, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.MANAGER.getDyeColor())
                        .withCustomName(TownRank.MANAGER.getPrefix()).withLore(
                                Arrays.asList(
                                        "§fBasic Permissions:",
                                        "§8 - §bEverything from Assistant",
                                        "§8 - §6Can invite new residents to town",
                                        "§8 - §6Can give plots to specific residents",
                                        "§8 - §6Can jail and unjail residents",
                                        "§8 - §6Can claim outposts for the town",
                                        "§8 - §6Can revoke ownership of a plot"
                                )
                        ).tagWith("changerank", new NBTTagString(TownRank.MANAGER.name())).build());
            }

            Player target = Bukkit.getPlayer(getTarget().getUuid());
            if (target != null && target.isOnline()) {
                inv.setItem(25, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.MAYOR.getDyeColor())
                        .withCustomName(TownRank.MAYOR.getPrefix()).withLore(
                                Arrays.asList(
                                        "§cThere can only be one mayor.",
                                        "§7",
                                        "§aLeft Click§f to transfer your mayor status."
                                )
                        ).tagWith("changerank", new NBTTagString(TownRank.MAYOR.name())).build());
            } else {
                inv.setItem(25, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.MAYOR.getDyeColor())
                        .withCustomName(TownRank.MAYOR.getPrefix()).withLore(
                                Arrays.asList(
                                        "§7" + getTarget().getName() + " must be online to transfer",
                                        "§7mayor status to them."
                                )
                        ).build());
            }


        }

    }

    public Resident getTarget() {
        return target;
    }
}
