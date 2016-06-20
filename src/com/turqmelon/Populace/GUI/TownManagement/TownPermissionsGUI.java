package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.PermissionSet;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTTagString;
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
public class TownPermissionsGUI extends TownGUI {
    public TownPermissionsGUI(Resident resident, Town town) {
        super(resident, town);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        TownRank rank = getTown().getRank(getResident());
        if (rank != TownRank.MAYOR){
            player.closeInventory();
            return;
        }

        if (raw == 0){
            TownGUI gui = new TownGUI(getResident(), getTown());
            gui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
        else{

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR){
                NBTBase setTag = ItemUtil.getTag(clicked, "clickedperm");
                NBTBase rankTag = ItemUtil.getTag(clicked, "clickedrank");

                if (setTag != null && rankTag != null){
                    PermissionSet set = PermissionSet.valueOf(setTag.toString().replace("\"", ""));
                    TownRank newRank = TownRank.valueOf(rankTag.toString().replace("\"", ""));

                    getTown().setRequiredRank(set, newRank);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    repopulate();
                }

            }

        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, getTown().getIcon(IconType.TOURISM, getResident()));

        inv.setItem(8, new ItemBuilder(Material.SIGN)
        .withCustomName("§e§lTown Permissions")
        .withLore(Arrays.asList("§7Town permissions can be overrided by",
                "§bPlots§7. To give someone access to just a single plot,",
                "§7just make them the owner.",
                "§a",
                "§eTown Permissions§7 govern who can do what in",
                "§7undefined plots, and also act as defaults for",
                "§7each §bPlot§7.")).build());

        int index = 10;

        for(PermissionSet set : PermissionSet.values()){
            if (!set.isApplicableTo(PermissionSet.PermissionScope.TOWN))continue;
            if (set == PermissionSet.SHOP && !Populace.isPopulaceMarketLoaded()) continue;

            TownRank setting = getTown().getRequiredRank(set);

            inv.setItem(index, new ItemBuilder(set.getIcon()).withCustomName("§b§l" + set.getName())
            .withLore(Arrays.asList("§7" + set.getDescription(),"§7","§fRequires " + setting.getPrefix())).build());

            int column = index + 2;
            for(TownRank rank : TownRank.values()){
                if (rank == setting){
                    inv.setItem(column, new ItemBuilder(Material.GOLD_BLOCK)
                            .withCustomName("§aCurrently set to " + rank.getPrefix()).makeItGlow().build());
                }
                else{
                    inv.setItem(column, new ItemBuilder(Material.STAINED_GLASS).withData(rank.getDyeColor())
                            .withCustomName("§fSet to " + rank.getPrefix()).tagWith("clickedperm", new NBTTagString(set.name())).tagWith("clickedrank", new NBTTagString(rank.name())).build());
                }
                column++;
            }

            index = index + 9;
        }


    }
}
