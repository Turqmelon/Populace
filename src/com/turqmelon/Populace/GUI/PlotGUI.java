package com.turqmelon.Populace.GUI;

import com.turqmelon.Populace.GUI.PlotManagement.PlotAllowListGUI;
import com.turqmelon.Populace.GUI.PlotManagement.PlotPermissionsGUI;
import com.turqmelon.Populace.GUI.PlotManagement.PlotTypeSelectGUI;
import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.PermissionSet;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class PlotGUI extends GUI {

    private Plot plot;
    private Resident resident;

    public PlotGUI(Plot plot, Resident resident) {
        super("Plot in " + plot.getTown().getName(), 54);
        this.plot = plot;
        this.resident = resident;
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        TownRank rank = getPlot().getTown().getRank(getResident());

        if (raw == 19 && rank.isAtLeast(TownRank.MANAGER)){
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            player.closeInventory();
            player.chat("/giveplot");
        }
        else if (raw == 21){
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            if (rank.isAtLeast(TownRank.ASSISTANT)){
                if (getPlot().isForSale()){
                    player.chat("/notforsale");
                    repopulate();
                }
                else{
                    player.closeInventory();
                    player.chat("/forsale");
                }
            }
            else{
                player.closeInventory();
                player.chat("/claim");
            }
        }
        else if (raw == 23 && ((plot.getOwner() != null && plot.getOwner().getUuid().equals(getResident().getUuid())) || rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel())){
            PlotPermissionsGUI gui = new PlotPermissionsGUI(getResident(), getPlot());
            gui.open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        }
        else if (raw == 25 && ((plot.getOwner() != null && plot.getOwner().getUuid().equals(getResident().getUuid())) || rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel())){
            if (event.isRightClick()){
                player.playSound(player.getLocation(), Sound.EXPLODE, 1, 1);
                getPlot().getAllowList().clear();
                repopulate();
            }
            else{
                PlotAllowListGUI gui = new PlotAllowListGUI(getResident(), getPlot());
                gui.open(player);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            }
        }
        else if (raw == 37 && rank.isAtLeast(TownRank.MANAGER)){
            PlotTypeSelectGUI gui = new PlotTypeSelectGUI(getResident(), getPlot());
            gui.open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        }
        else if (raw == 43 && ((plot.getOwner() != null && plot.getOwner().getUuid().equals(getResident().getUuid())) || rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel())){
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            player.closeInventory();
            player.chat("/unclaim");
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(4, getPlot().getIcon());

        TownRank rank = getPlot().getTown().getRank(getResident());

        List<String> ownerInfo = new ArrayList<>();

        if (rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()){
            ownerInfo.add("§7You can invite residents to");
            ownerInfo.add("§7claim this plot. Simply stand inside");
            ownerInfo.add("§7it, and type §f/givePlot <Resident>§7.");
        }

        inv.setItem(19, new ItemBuilder(Material.SKULL_ITEM).withData((byte)3)
                .withCustomName("§b§l"+(getPlot().getOwner()!=null?getPlot().getOwner().getName():getPlot().getTown().getName() + getPlot().getTown().getLevel().getSuffix()))
                .withLore(ownerInfo).build());

        List<String> saleInfo = new ArrayList<>();
        if (rank.getPermissionLevel() >= TownRank.ASSISTANT.getPermissionLevel()){
            saleInfo.add("§7");
            saleInfo.add("§6§lSell a Plot");
            saleInfo.add("§f/forSale <Price>");
            saleInfo.add("§7");
            saleInfo.add("§6§lStop Selling a Plot");
            saleInfo.add("§f/notForSale");
            saleInfo.add("§7");
            saleInfo.add("§7Sold plot profits go to the bank.");
        }
        else{
            saleInfo.add("§7Purchase plots for sale by standing");
            saleInfo.add("§7inside them and typing §f/claim§7.");
        }

        inv.setItem(21, new ItemBuilder(Material.DIAMOND)
                .withCustomName("§b§l" + (getPlot().isForSale()?"For Sale: " + Populace.getCurrency().format(getPlot().getPrice())
                        :"Not for Sale")).withLore(saleInfo).build());

        List<String> list = new ArrayList<>();

        list.add("§7These permissions control who can do what");
        list.add("§7inside this plot.");
        list.add("§7");
        list.add("§7Residents on the §bAllow List§7 bypass");
        list.add("§7these settings.");
        list.add("§7");

        for(PermissionSet set : PermissionSet.values()){
            if (!set.isApplicableTo(PermissionSet.PermissionScope.PLOT))continue;
            list.add("§fWho can " + set.getLoredescription() + "? " + getPlot().getRequiredRank(set).getPrefix());
        }
        if ((plot.getOwner() != null && plot.getOwner().getUuid().equals(getResident().getUuid())) || rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()){
            list.add("§7");
            list.add("§aLeft Click§f to change these permissions.");
        }

        inv.setItem(23, new ItemBuilder(Material.EYE_OF_ENDER)
                .withCustomName("§b§lPlot Permissions").withLore(list).build());

        List<String> allowList = new ArrayList<>();
        allowList.add("§7Players on your §bAllow List§7 bypass");
        allowList.add("§7the permission restrictions of the plot.");
        allowList.add("§7");
        allowList.add("§fAllow List §e" + getPlot().getAllowList().size());
        if ((plot.getOwner() != null && plot.getOwner().getUuid().equals(getResident().getUuid())) || rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()){
            allowList.add("§7");
            allowList.add("§6§lAdd a Resident to Allow List");
            allowList.add("§f/allow <Resident>");
            allowList.add("§7");
            allowList.add("§aLeft Click§f to view the allow list.");
            allowList.add("§aRight Click§f to clear the allow list.");
        }

        inv.setItem(25, new ItemBuilder(Material.BOOK_AND_QUILL).withCustomName("§b§lAllow List").withLore(allowList).build());

        if (rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()){
            inv.setItem(37, new ItemBuilder(Material.WORKBENCH).withCustomName("§b§lPlot Type: §f§l" + getPlot().getType().getName()).withLore(Arrays.asList("§aLeft Click§f to change.")).build());
        }

        if ((plot.getOwner() != null && plot.getOwner().getUuid().equals(getResident().getUuid())) || rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()) {
            inv.setItem(43, new ItemBuilder(Material.BARRIER).withCustomName("§c§lUnclaim Plot").build());

        }

    }

    public Plot getPlot() {
        return plot;
    }

    public Resident getResident() {
        return resident;
    }
}
