package com.turqmelon.Populace.GUI;

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

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class ResidentGUI extends GUI {

    private Resident resident;
    private int page = 1;

    private boolean hasNextPage = false;

    public ResidentGUI(Resident resident) {
        super(resident.getName(), 54);
        this.resident = resident;
         setUpdateTicks(20, true);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 45 && getPage() > 1 && player.hasPermission("populace.commands.resident.viewplots")) {
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            setPage(getPage()-1);
            repopulate();
        } else if (raw == 53 && hasNextPage && player.hasPermission("populace.commands.resident.viewplots")) {
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            setPage(getPage()+1);
            repopulate();
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(4, getResident().getIcon());

        if (player.hasPermission("populace.commands.resident.viewplots")) {
            List<PlotChunk> land = getResident().getPlotChunks();

            int itemsPerPage = 27;
            int startIndex = 0;
            int endIndex = (itemsPerPage - 1);

            boolean nextPage = true;

            List<Plot> display = new ArrayList<>();

            for (int i = startIndex; i <= endIndex; i++) {
                if (i < land.size()) {
                    Plot plot = PlotManager.getPlot(land.get(i));
                    if (plot != null) {
                        display.add(plot);
                    }
                } else {
                    nextPage = false;
                    break;
                }
            }

            hasNextPage = nextPage;

            int index = 18;
            for (Plot plot : display) {
                inv.setItem(index, plot.getIcon());
                index++;
            }

            if (nextPage) {
                inv.setItem(53, new ItemBuilder(Material.ARROW).withCustomName("§e§lNEXT >").build());
            }

            if (getPage() > 1) {
                inv.setItem(45, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());
            }
        }


    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Resident getResident() {
        return resident;
    }
}
