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
import com.turqmelon.Populace.Plot.PlotType;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.ItemBuilder;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class PlotTypeSelectGUI extends PlotGUI {

    public PlotTypeSelectGUI(Resident resident, Plot plot) {
        super(plot, resident);

    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 0){
            PlotGUI gui = new PlotGUI(getPlot(), getResident());
            gui.open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        }
        else if (raw == 19){

            getPlot().setType(PlotType.RESIDENTIAL);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            repopulate();

        }
        else if (raw == 22){

            getPlot().setType(PlotType.MERCHANT);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            repopulate();

        }
        else if (raw == 25){

            getPlot().setType(PlotType.BATTLE);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            repopulate();

        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, getPlot().getIcon());

        if (getPlot().getType() == PlotType.RESIDENTIAL){
            inv.setItem(19, new ItemBuilder(Material.BED).withCustomName("§a§lResidential Plot").withLore(Arrays.asList("§7Selected!")).makeItGlow().build());
        }
        else{
            inv.setItem(19, new ItemBuilder(Material.BED).withCustomName("§b§lResidential Plot").withLore(Arrays.asList(
                    "§bResidential Plots§7 are the default plots. They",
                    "§7provide protection from outside players, offer",
                    "§cPVP§7 protection, and are perfect for general use.",
                    "§7",
                    "§fDaily Cost §e" + Populace.getCurrency().format(PlotType.RESIDENTIAL.getDailyCost()),
                    "§a",
                    "§aLeft Click§f to change plot type."
            )).build());
        }

        if (getPlot().getType() == PlotType.MERCHANT){
            inv.setItem(22, new ItemBuilder(Material.DIAMOND).withCustomName("§a§lMerchant Plot").withLore(Arrays.asList("§7Selected!")).makeItGlow().build());
        }
        else{
            inv.setItem(22, new ItemBuilder(Material.DIAMOND).withCustomName("§b§lMerchant Plot").withLore(Arrays.asList(
                    "§bMerchant Plots§7 offer the same protections",
                    "§7as §bResidential Plots§7, with the extra ability",
                    "§7to create shops within the plot.",
                    "§7",
                    "§fDaily Cost §e" + Populace.getCurrency().format(PlotType.MERCHANT.getDailyCost()),
                    "§a",
                    "§aLeft Click§f to change plot type."
            )).build());
        }

        if (getPlot().getType() == PlotType.BATTLE){
            inv.setItem(25, new ItemBuilder(Material.IRON_CHESTPLATE).withCustomName("§a§lBattle Plot").withLore(Arrays.asList("§7Selected!")).makeItGlow().build());
        }
        else{
            inv.setItem(25, new ItemBuilder(Material.IRON_CHESTPLATE).withCustomName("§b§lBattle Plot").withLore(Arrays.asList(
                    "§bBattle Plots§7 offer the land protections of",
                    "§bResidential Plots§7, with the added ability of allowing",
                    "§cPVP§7 within the area.",
                    "§7",
                    "§fDaily Cost §e" + Populace.getCurrency().format(PlotType.BATTLE.getDailyCost()),
                    "§a",
                    "§aLeft Click§f to change plot type."
            )).build());
        }

    }

    private ItemStack getIcon(Resident resident){
        return new ItemBuilder(Material.SKULL_ITEM).withData((byte)3).asHeadOwner(resident.getName())
                .withCustomName("§a§l"+resident.getName()).withLore(Arrays.asList("§aLeft Click§f to remove.")).tagWith("clickeduuid", new NBTTagString(resident.getUuid().toString())).build();
    }
}
