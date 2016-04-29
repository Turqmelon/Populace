package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.Events.Resident.ResidentRankChangedEvent;
import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagString;
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
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            return;
        }

        if (raw == 41) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            getResident().sendMessage(Msg.INFO + "Kicking out a resident will cause them to lose access to everything in the town.");
            getResident().setPendingAction(() -> getTown().kickOut(getTarget(), getResident(), null));
        } else if (raw == 39) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            player.chat("/jail");
        } else if (rank == TownRank.MAYOR) {

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR){
                NBTBase nbt = ItemUtil.getTag(clicked, "changerank");
                if (nbt != null){
                    TownRank newRank = TownRank.valueOf(nbt.toString().replace("\"", ""));
                    getTown().getResidents().put(getTarget(), newRank);
                    getTown().sendTownBroadcast(TownRank.RESIDENT, getTarget().getName() + "'s rank has been set to " + newRank.getPrefix() + "§dby the mayor.");
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
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
        inv.setItem(4, getIcon(getTarget(), false));

        TownRank rank = getTown().getRank(getTarget());

        inv.setItem(39, new ItemBuilder(Material.IRON_FENCE).withCustomName("§7§lJail Resident").build());
        inv.setItem(41, new ItemBuilder(Material.BARRIER).withCustomName("§c§lKick Resident").build());


        if (getTown().getRank(getResident()) == TownRank.MAYOR && rank != TownRank.MAYOR) {

            if (rank == TownRank.RESIDENT){
                inv.setItem(19, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.RESIDENT.getDyeColor())
                        .withCustomName(TownRank.RESIDENT.getPrefix()).withLore(Arrays.asList("§aCurrent Rank")).makeItGlow().build());
            }
            else{
                inv.setItem(19, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.RESIDENT.getDyeColor())
                        .withCustomName(TownRank.RESIDENT.getPrefix()).withLore("§7Grants no special abilities.").tagWith("changerank", new NBTTagString(TownRank.RESIDENT.name())).build());
            }

            if (rank == TownRank.ASSISTANT){
                inv.setItem(22, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.ASSISTANT.getDyeColor())
                        .withCustomName(TownRank.ASSISTANT.getPrefix()).withLore(Arrays.asList("§aCurrent Rank")).makeItGlow().build());
            }
            else{
                inv.setItem(22, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.ASSISTANT.getDyeColor())
                        .withCustomName(TownRank.ASSISTANT.getPrefix()).withLore("§7Can assist with plot management.").tagWith("changerank", new NBTTagString(TownRank.ASSISTANT.name())).build());
            }

            if (rank == TownRank.MANAGER){
                inv.setItem(25, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.MANAGER.getDyeColor())
                        .withCustomName(TownRank.MANAGER.getPrefix()).withLore(Arrays.asList("§aCurrent Rank")).makeItGlow().build());
            }
            else{
                inv.setItem(25, new ItemBuilder(Material.STAINED_GLASS_PANE).withData(TownRank.MANAGER.getDyeColor())
                        .withCustomName(TownRank.MANAGER.getPrefix()).withLore("§7Can assist with resident management.").tagWith("changerank", new NBTTagString(TownRank.MANAGER.name())).build());
            }


        }

    }

    public Resident getTarget() {
        return target;
    }
}
