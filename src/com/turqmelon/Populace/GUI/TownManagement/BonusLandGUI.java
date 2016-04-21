package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagString;
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
public class BonusLandGUI extends TownGUI {

    public BonusLandGUI(Resident resident, Town town) {
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
            TownGUI gui = new TownGUI(getResident(), getTown());
            gui.open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        }
        else{
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR){
                NBTBase nbt = ItemUtil.getTag(clicked, "landaction");
                if (nbt != null){
                    LandAction action = LandAction.valueOf(nbt.toString().replace("\"", ""));
                    boolean success = true;
                    switch(action){
                        case BUY:
                            success = getTown().buyBonusBlocks(1);
                            break;
                        case SELL:
                            success = getTown().sellBonusBlocks(1);
                            break;
                    }
                    if (success){
                        getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + getResident().getName() + " " + action.getVerb() + " bonus land.");
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                        repopulate();
                    }
                    else{
                        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
                    }
                }
            }
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, getTown().getIcon(IconType.MAIN, getResident()));

        int[] buy = {11, 19, 20, 21, 27, 28, 30, 31, 37, 38, 39, 47};
        int[] sell = {25, 33, 35, 43};

        for(int i : buy){
            inv.setItem(i, getLandActionButton(LandAction.BUY, Material.GRASS));
        }

        inv.setItem(29, getLandActionButton(LandAction.BUY, Material.BED));

        for(int i : sell){
            inv.setItem(i, getLandActionButton(LandAction.SELL, Material.GRASS));
        }

        inv.setItem(34, getLandActionButton(LandAction.SELL, Material.BED));

    }

    private ItemStack getLandActionButton(LandAction action, Material material) {
        if(action == LandAction.BUY){

            if (getTown().getBonusLand() < getTown().getLevel().getMaxland()){
                return new ItemBuilder(material).withCustomName("§b§lBUY BONUS LAND")
                        .withLore(Arrays.asList("§bBonus Land§7 allows your town to expand",
                                "§7without leveling up, at the cost of a higher daily upkeep.",
                                "§7",
                                "§fCurrent Bonus Land §e" + getTown().getBonusLand(),
                                "§71 Bonus Land = 1 Chunk",
                                "§7",
                                "§aLeft Click§7 to buy bonus land for +" + Populace.getCurrency().format(Configuration.BONUS_LAND_DAILY_COST) + "/day.")).tagWith("landaction", new NBTTagString(action.name())).build();
            }
            else{
                return new ItemBuilder(material).withCustomName("§c§lMAXIMUM BONUS LAND REACHED")
                        .withLore(Arrays.asList("§cYou can't buy any more land until your",
                                "§ctown increases in level.")).build();
            }

        }
        else{
            if (getTown().getBonusLand() > 0){

                if (getTown().getPlots().size() < getTown().getMaxLand()){
                    return new ItemBuilder(material).withCustomName("§b§lSELL BONUS LAND")
                            .withLore(Arrays.asList("§bBonus Land§7 allows your town to expand",
                                    "§7without leveling up, at the cost of a higher daily upkeep.",
                                    "§7",
                                    "§fCurrent Bonus Land §e" + getTown().getBonusLand(),
                                    "§71 Bonus Land = 1 Chunk",
                                    "§7",
                                    "§aLeft Click§7 to sell bonus land for -" + Populace.getCurrency().format(Configuration.BONUS_LAND_DAILY_COST) + "/day.")).tagWith("landaction", new NBTTagString(action.name())).build();
                }
                else{
                    return new ItemBuilder(material).withCustomName("§c§lNOTHING TO SELL")
                            .withLore(Arrays.asList("§cAll of the available town land has",
                                    "§cbeen claimed. Abandon claims to be able to sell",
                                    "§cun-needed bonus land.")).build();
                }

            }
            else{
                return new ItemBuilder(material).withCustomName("§c§lNOTHING TO SELL")
                        .withLore(Arrays.asList("§cTown has no bonus land.")).build();
            }
        }
    }

    enum LandAction {
        BUY("bought"),
        SELL("sold");

        private String verb;

        LandAction(String verb) {
            this.verb = verb;
        }

        public String getVerb() {
            return verb;
        }
    }
}
