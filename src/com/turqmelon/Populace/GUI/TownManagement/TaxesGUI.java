package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

        boolean tooSoon = System.currentTimeMillis() - Populace.getLastNewDay() > TimeUnit.HOURS.toMillis(20);

        int raw = event.getRawSlot();
        if (raw == 0){
            TownGUI gui = new TownGUI(getResident(), getTown(), 1);
            gui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
        else if (raw == 4){

            if (event.isRightClick()){
                BankGUI gui = new BankGUI(getResident(), getTown());
                gui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }

        }
        else if (raw == 19){

            if (tooSoon) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                player.closeInventory();
                player.sendMessage(Msg.ERR + "You can't change resident tax within 4 hours of the next \"new day\".");
                return;
            }

            int modifier = event.isShiftClick() ? 100 : 10;

            double pendingAmount = getTown().getPendingTax();
            if (pendingAmount == -1) {
                pendingAmount = getTown().getTax();
            }

            double newAmount = 0;

            if (event.isLeftClick()){
                newAmount = pendingAmount + modifier;
            }
            else if (event.isRightClick()){
                newAmount = pendingAmount - modifier;
            }

            if (newAmount < 0) {
                newAmount = 0;
            }

            if (newAmount == getTown().getTax()) {
                newAmount = -1;
            }

            getTown().setPendingTax(newAmount);

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();
        }
        else if (raw == 22){

            if (tooSoon) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                player.closeInventory();
                player.sendMessage(Msg.ERR + "You can't change plot tax within 4 hours of the next \"new day\".");
                return;
            }

            int modifier = event.isShiftClick() ? 100 : 10;

            double pendingAmount = getTown().getPendingPlotTax();
            if (pendingAmount == -1) {
                pendingAmount = getTown().getPlotTax();
            }

            double newAmount = 0;

            if (event.isLeftClick()){
                newAmount = pendingAmount + modifier;
            }
            else if (event.isRightClick()){
                newAmount = pendingAmount - modifier;
            }

            if (newAmount < 0) {
                newAmount = 0;
            }

            if (newAmount == getTown().getPlotTax()) {
                newAmount = -1;
            }

            getTown().setPendingPlotTax(newAmount);

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();

        } else if (raw == 25 && Populace.isPopulaceMarketLoaded()) {
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
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + getResident().getName() + " changed the §fSales Tax§d to " + new DecimalFormat("#.#").format(getTown().getSalesTax()) + "%.");

            repopulate();
        }

    }

    @Override
    protected void populate() {
        Town town = getTown();
        Inventory inv = getProxyInventory();
        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, town.getIcon(IconType.TREASURY, getResident()));

        String resChange = town.renderTaxChange(town.getTax(), town.getPendingTax());

        inv.setItem(19, new ItemBuilder(Material.SKULL_ITEM).withData((byte)3).withCustomName("§b§lResident Tax")
                .withLore(Arrays.asList("" +
                        "§bResident Tax§7 is the minimum amount that",
                        "§7every resident of " + town.getName() + town.getLevel().getSuffix() + " must pay",
                        "§7to the town bank every day.",
                        "§7",
                        "§cIf a resident can't afford their taxes, they",
                        "§care kicked from the town.",
                        "§7",
                        "§fResident Tax §e" + Populace.getCurrency().format(town.getTax()) + resChange,
                        "§7",
                        "§aLeft Click§f for +" + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)",
                        "§aRight Click§f for -" + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)",
                        "§6Changes take effect on the next new day.")).build());

        String plotChange = town.renderTaxChange(town.getPlotTax(), town.getPendingPlotTax());

        inv.setItem(22, new ItemBuilder(Material.GRASS).withCustomName("§b§lPlot Tax")
                .withLore(Arrays.asList("" +
                                "§bPlot Tax§7 is charged per plot that a resident",
                                "§7owns in your town. This tax is in addition to",
                                "§7their §bResident Tax§7.",
                                "§7",
                                "§cIf a resident can't afford their taxes, they",
                                "§care kicked from the town.",
                                "§7",
                        "§fPlot Tax §e" + Populace.getCurrency().format(town.getPlotTax()) + plotChange,
                                "§7",
                        "§aLeft Click§f for +" + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)",
                        "§aRight Click§f for -" + Populace.getCurrency().format(10) + ". (§aShift§f for " + Populace.getCurrency().format(100) + ".)",
                        "§6Changes take effect on the next new day.")).build());

        if (Populace.isPopulaceMarketLoaded()) {
            inv.setItem(25, new ItemBuilder(Material.DIAMOND).withCustomName("§b§lSales Tax")
                    .withLore(Arrays.asList("" +
                                    "§bSales Tax§7 is taken out of shop purchases within",
                            "§7your town. Lower sales tax will encourage more merchants",
                            "§7to live here.",
                            "§7",
                            "§9§lExample:",
                            "§9An item is priced for " + Populace.getCurrency().format(200) + ".",
                            "§9Your §bSales Tax§9 is " + new DecimalFormat("#.#").format(town.getSalesTax()) + "%.",
                            "§9The town will receive " + (town.getSalesTax() == 0 ? 0 : Populace.getCurrency().format((200 * (town.getSalesTax() / 100.0)))) + ".",
                            "§9The merchant will receive " + (town.getSalesTax() == 0 ? Populace.getCurrency().format(200) : Populace.getCurrency().format((200 * ((100.0 - town.getSalesTax()) / 100.0)))) + ".",
                            "§7",
                            "§fSales Tax §e" + new DecimalFormat("#.#").format(town.getSalesTax()) + "%",
                            "§7",
                            "§aLeft Click§f for +1%. (§aShift§f for 5%.)",
                            "§aRight Click§f for -1%. (§aShift§f for 5%.)")).build());
        }


    }
}
