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
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
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
import java.util.UUID;

public class PlotAllowListGUI extends PlotGUI {

    public PlotAllowListGUI(Resident resident, Plot plot) {
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
                NBTBase uuidTag = ItemUtil.getTag(clicked, "clickeduuid");

                if (uuidTag != null){
                    Resident target = ResidentManager.getResident(UUID.fromString(uuidTag.toString().replace("\"", "")));
                    if (target != null){
                        getPlot().removeFromAllowList(target);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        repopulate();
                    }
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
        .withCustomName("§e§lAllow List")
        .withLore(Arrays.asList("§7Residents on your §bAllow List",
                "§7will override your plot permissions.",
                "§a",
                "§7Add players with §f/allow <Resident>§7.")).build());

        int index = 18;

        for(Resident resident : getPlot().getAllowList()){
            inv.setItem(index, getIcon(resident));
            index++;
        }

    }

    private ItemStack getIcon(Resident resident){
        return new ItemBuilder(Material.SKULL_ITEM).withData((byte)3).asHeadOwner(resident.getName())
                .withCustomName("§a§l"+resident.getName()).withLore(Arrays.asList("§aLeft Click§f to remove.")).tagWith("clickeduuid", new NBTTagString(resident.getUuid().toString())).build();
    }
}
