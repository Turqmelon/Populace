package com.turqmelon.Populace.GUI;

import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
@SuppressWarnings("unchecked")
public class TownsGUI extends GUI{

    private int page;
    private Resident resident;
    private boolean hasNextPage = false;

    public TownsGUI(int page, Resident resident) {
        super("Towns", 54);
        this.page = page;
        this.resident = resident;
        setUpdateTicks(20, true);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();

        if (raw == 45 && getPage() > 1){
            this.page = getPage()-1;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();
        }
        else if (raw == 53 && hasNextPage){
            this.page = getPage()+1;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();
        }
        else if (clicked != null && clicked.getType() != Material.AIR){
            NBTBase nbt = ItemUtil.getTag(clicked, "clickedtownid");
            if (nbt != null){
                NBTTagString str = (NBTTagString)nbt;

                UUID townid = UUID.fromString(str.toString().replace("\"", ""));
                Town town = TownManager.getTown(townid);
                if (town != null){
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    TownGUI gui = new TownGUI(getResident(), town);
                    gui.open(player);
                }
                else{
                    player.sendMessage(Msg.ERR + "Town could not be found.");
                }
            }
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(4, new ItemBuilder(Material.SIGN).withCustomName("§e§lThere are " + TownManager.getTowns(false).size() + " towns.").build());

        int townsPerPage = 36;
        int startIndex = 0;
        int endIndex = startIndex+(townsPerPage-1);

        List<Town> towns = new ArrayList<>();
        towns.addAll(TownManager.getTowns(false));
        Collections.sort(towns);

        List<Town> display = new ArrayList<>();

        boolean nextPage = true;
        for(int i = startIndex; i <= endIndex; i++){
            if (i < towns.size()){
                display.add(towns.get(i));
            }
            else{
                nextPage = false;
                break;
            }
        }

        this.hasNextPage = nextPage;

        if (getPage() > 1){
            inv.setItem(45, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());
        }
        if (nextPage){
            inv.setItem(53, new ItemBuilder(Material.ARROW).withCustomName("§e§lNEXT >").build());
        }

        int index = 9;
        for(Town town : display){
            inv.setItem(index, town.getIcon(TownGUI.IconType.MAIN, getResident(), false));
            index++;
        }

    }

    public Resident getResident() {
        return resident;
    }

    public int getPage() {
        return page;
    }
}
