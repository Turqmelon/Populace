package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.text.DecimalFormat;
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
public class TaxesGUI extends TownGUI {

    public TaxesGUI(Resident resident, Town town) {
        super(resident, town);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        TownRank rank = getTown().getRank(getResident());
        if (rank != TownRank.MAYOR){
            player.closeInventory();
            return;
        }

        int raw = event.getRawSlot();
        if (raw == 0){
            TownGUI gui = new TownGUI(getResident(), getTown(), 1);
            gui.open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        }
        else if (raw == 4){

            if (event.isRightClick()){
                BankGUI gui = new BankGUI(getResident(), getTown());
                gui.open(player);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            }

        }
        else if (raw == 19){

            int modifier = event.isShiftClick() ? 100 : 10;
            if (event.isLeftClick()){
                getTown().setTax(getTown().getTax() + modifier);
            }
            else if (event.isRightClick()){
                getTown().setTax(getTown().getTax() - modifier);
            }
            if (getTown().getTax() < 0){
                getTown().setTax(0);
            }

            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + getResident().getName() + " changed the §fResident Tax§d to " + Populace.getCurrency().format(getTown().getTax()) + ".");

            repopulate();
        }
        else if (raw == 22){
            int modifier = event.isShiftClick() ? 100 : 10;
            if (event.isLeftClick()){
                getTown().setPlotTax(getTown().getPlotTax() + modifier);
            }
            else if (event.isRightClick()){
                getTown().setPlotTax(getTown().getPlotTax() - modifier);
            }
            if (getTown().getPlotTax() < 0){
                getTown().setPlotTax(0);
            }

            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + getResident().getName() + " changed the §fPlot Tax§d to " + Populace.getCurrency().format(getTown().getPlotTax()) + ".");

            repopulate();
        }
        else if (raw == 25){
            int modifier = event.isShiftClick() ? 5 : 1;
            double newPerc = getTown().getSalesTax();

            if (event.isLeftClick()){
                newPerc = newPerc + modifier;
            }
            else if (event.isRightClick()){
                newPerc = newPerc - modifier;
            }

            if (newPerc > 100)newPerc = 100;
            if (newPerc < 0)newPerc = 0;

            if (newPerc == getTown().getSalesTax()){
                return;
            }

            getTown().setSalesTax(newPerc);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + getResident().getName() + " changed the §fSales Tax§d to " + new DecimalFormat("#.#").format(getTown().getSalesTax()) + "%.");

            repopulate();
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();
        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, getTown().getIcon(IconType.TREASURY, getResident()));

        inv.setItem(19, new ItemBuilder(Material.SKULL_ITEM).withData((byte)3).withCustomName("§b§lResident Tax")
                .withLore(Arrays.asList("" +
                        "§bResident Tax§7 is the minimum amount that",
                        "§7every resident of " + getTown().getName() + getTown().getLevel().getSuffix() + " must pay",
                        "§7to the town bank every day.",
                        "§7",
                        "§cIf a resident can't afford their taxes, they",
                        "§care kicked from the town.",
                        "§7",
                        "§fResident Tax §e" + Populace.getCurrency().format(getTown().getTax()),
                        "§7",
                        "§aLeft Click§f to increase by " + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)",
                        "§aRight Click§f to decrease by " + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)")).build());

        inv.setItem(22, new ItemBuilder(Material.GRASS).withCustomName("§b§lPlot Tax")
                .withLore(Arrays.asList("" +
                                "§bPlot Tax§7 is charged per plot that a resident",
                                "§7owns in your town. This tax is in addition to",
                                "§7their §bResident Tax§7.",
                                "§7",
                                "§cIf a resident can't afford their taxes, they",
                                "§care kicked from the town.",
                                "§7",
                                "§fPlot Tax §e" + Populace.getCurrency().format(getTown().getPlotTax()),
                                "§7",
                                "§aLeft Click§f to increase by " + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)",
                                "§aRight Click§f to decrease by " + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)")).build());

        inv.setItem(25, new ItemBuilder(Material.DIAMOND).withCustomName("§b§lSales Tax")
                .withLore(Arrays.asList("" +
                                "§bSales Tax§7 is taken out of shop purchases within",
                                "§7your town. Lower sales tax will encourage more merchants",
                                "§7to live here.",
                                "§7",
                                "§9§lExample:",
                                "§9An item is priced for " + Populace.getCurrency().format(200) + ".",
                                "§9Your §bSales Tax§9 is " + new DecimalFormat("#.#").format(getTown().getSalesTax()) + "%.",
                                "§9The town will receive " + (getTown().getSalesTax()==0?0:Populace.getCurrency().format((200*(getTown().getSalesTax()/100.0)))) + ".",
                                "§9The merchant will receive " + (getTown().getSalesTax()==0?Populace.getCurrency().format(200):Populace.getCurrency().format((200*((100.0-getTown().getSalesTax())/100.0)))) + ".",
                                "§7",
                                "§fSales Tax §e" + new DecimalFormat("#.#").format(getTown().getSalesTax()) + "%",
                                "§7",
                                "§aLeft Click§f to increase by 1%. (§aShift§f for 5%.)",
                                "§aRight Click§f to decrease by 1%. (§aShift§f for 5%.)")).build());

    }
}
