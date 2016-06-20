package com.turqmelon.Populace.GUI.PlotManagement;

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

import com.turqmelon.Populace.GUI.PlotGUI;
import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.PermissionSet;
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

public class PlotPermissionsGUI extends PlotGUI {

    public PlotPermissionsGUI(Resident resident, Plot plot) {
        super(plot, resident);

    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 0){
            PlotGUI gui = new PlotGUI(getPlot(), getResident());
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

                    getPlot().requireRank(set, newRank);
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
        inv.setItem(4, getPlot().getIcon());

        inv.setItem(8, new ItemBuilder(Material.SIGN)
        .withCustomName("§e§lPlot Permissions")
        .withLore(Arrays.asList("§7Plot permissions govern who",
                "§7can do what within this land. They will override",
                "§7the permissions of the town.",
                "§a",
                "§7Players on the §bAllow List§7 will bypass these",
                "§7restrictions, regardless of their rank.")).build());

        int index = 10;

        for(PermissionSet set : PermissionSet.values()){
            if (!set.isApplicableTo(PermissionSet.PermissionScope.PLOT))continue;
            if (set == PermissionSet.SHOP && !Populace.isPopulaceMarketLoaded()) continue;

            TownRank setting = getPlot().getRequiredRank(set);

            inv.setItem(index, new ItemBuilder(set.getIcon()).withCustomName("§b§l" + set.getName())
            .withLore(Arrays.asList("§7" + set.getDescription(),"§7","§fRequires " + setting.getPrefix())).build());

            int column = index + 2;
            for(TownRank rank : TownRank.values()){
                if (rank.isAtLeast(TownRank.MANAGER))continue;
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
