package com.turqmelon.Populace.GUI;

import com.turqmelon.Populace.Events.Town.TownSpawnSetEvent;
import com.turqmelon.Populace.GUI.TownManagement.*;
import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownLevel;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.*;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
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
public class TownGUI extends GUI {

    private Resident resident;
    private Town town;
    private int page = 1;

    public TownGUI(Resident resident, Town town) {
        super(town.getName(), 54);
        this.resident = resident;
        this.town = town;
        setUpdateTicks(20, false);
    }

    public TownGUI(Resident resident, Town town, int page) {
        this(resident, town);
        this.page = page;
        setUpdateTicks(20, false);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        TownRank rank = getTown().getRank(getResident());

        int raw = event.getRawSlot();
        if (raw == 45 && getPage() > 1){
            setPage(getPage()-1);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();
            return;
        }
        else if (raw == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR){
            setPage(getPage()+1);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();
            return;
        } else if (raw == 7 && rank == TownRank.MAYOR) {
            if (getTown().getResidents().size() >= TownLevel.HAMLET.getResidents()) {
                if (event.isRightClick()) {
                    Plot plot = PlotManager.getPlot(player.getLocation().getChunk());
                    if (plot != null && plot.getTown() != null && plot.getTown().getUuid().equals(getTown().getUuid())) {

                        TownSpawnSetEvent e = new TownSpawnSetEvent(getTown(), getResident(), player.getLocation());
                        Bukkit.getPluginManager().callEvent(e);
                        if (!e.isCancelled()) {
                            getTown().setSpawn(player.getLocation());
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.closeInventory();
                            getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + getResident().getName() + " has updated the town spawn!");
                        }
                    } else {
                        player.sendMessage(Msg.ERR + "Town spawn can only be set within your land.");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                    }
                } else {
                    TownPermissionsGUI gui = new TownPermissionsGUI(getResident(), getTown());
                    gui.open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }

            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
            }

        } else if (raw == 46 && rank.isAtLeast(TownRank.RESIDENT) && player.hasPermission("populace.commands.board")) {
            if (getTown().getLevel().getResidents() >= TownLevel.VILLAGE.getResidents()) {
                TownBoardGUI gui = new TownBoardGUI(getResident(), getTown());
                gui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
            }
        } else if (raw == 50 && getTown().canWarpToSpawn(getResident(), false) && player.hasPermission("populace.commands.visit")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            new PopulaceTeleport(player, town.getSpawn(), player.getLocation(), Configuration.TELEPORT_WARMUP_TIME, Configuration.TELEPORT_COOLDOWN_TIME, false);
        } else if (raw == 49 && rank.isAtLeast(TownRank.RESIDENT) && getTown().getMapView() != null && player.hasPermission("populace.commands.map")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            player.chat("/map");
        } else if (raw == 48 && rank.isAtLeast(TownRank.MANAGER) && player.hasPermission("populace.commands.invite")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            player.chat("/invite");
        } else if (raw == 1 && rank.isAtLeast(TownRank.RESIDENT)) {
            if (event.isRightClick() || rank != TownRank.MAYOR) {
                BankGUI gui = new BankGUI(getResident(), getTown());
                gui.open(player);
            }
            else{
                TaxesGUI gui = new TaxesGUI(getResident(), getTown());
                gui.open(player);
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
        else if (raw == 4){
            if (event.isLeftClick() && rank == TownRank.MAYOR){
                BonusLandGUI gui = new BonusLandGUI(getResident(), getTown());
                gui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            else if (event.isRightClick() && event.isShiftClick() && rank == TownRank.MAYOR){
                if (getTown().getResidents().size() < TownLevel.VILLAGE.getResidents()) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    player.closeInventory();
                    player.sendMessage(Msg.WARN + "If you destroy your town, everything will be lost.");
                    if (getTown().isVulnerableToDestruction()) {
                        player.sendMessage(Msg.ERR + "Your town will §lLITERALLY§c be destroyed! Take any items and things you want to keep before confirming!");
                    }
                    getResident().setPendingAction(() -> getTown().destroy(false));
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                }
            }
            else if (event.isRightClick() && rank == TownRank.MAYOR){
                getTown().toggleStatus();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }

            else if (event.isLeftClick() && rank == TownRank.GUEST){
                if (player.hasPermission("populace.commands.join")) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    player.closeInventory();
                    if (getTown().isOpen()) {
                        getTown().joinByPublic(getResident());
                    } else {
                        getTown().joinByInvite(getResident());
                    }
                }
                else{
                    player.closeInventory();
                    player.sendMessage(Msg.ERR + "You don't have permission.");
                }
            }
            else if (event.isLeftClick() && rank.getPermissionLevel() < TownRank.MAYOR.getPermissionLevel()){
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.closeInventory();
                getResident().sendMessage(Msg.INFO + "If you leave the town, you will lose access to everything you own.");
                getResident().sendMessage(Msg.INFO + "You will not be able to join or create another town for 8 hours.");
                getResident().setPendingAction(() -> getTown().leaveOnBehalf(getResident()));
            }
        }

        if (rank.getPermissionLevel()>=TownRank.MANAGER.getPermissionLevel()){

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR){

                NBTBase nbt = ItemUtil.getTag(event.getCurrentItem(), "residentid");

                if (nbt != null){
                    Resident clicked = ResidentManager.getResident(UUID.fromString(nbt.toString().replace("\"", "")));
                    if (clicked != null){

                        if (clicked.getTown().getRank(clicked) != TownRank.MAYOR) {
                            SetResidentRankGUI gui = new SetResidentRankGUI(getResident(), getTown(), clicked);
                            gui.open(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        }

                    }
                    else{
                        player.sendMessage(Msg.ERR + "Resident could not be identified.");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                    }
                }



            }

        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();
        inv.setItem(1, getTown().getIcon(IconType.TREASURY, getResident()));
        inv.setItem(4, getTown().getIcon(IconType.MAIN, getResident()));
        inv.setItem(7, getTown().getIcon(IconType.TOURISM, getResident()));

        if (getPage() > 1){
            inv.setItem(45, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());
        }

        boolean nextPage = true;

        int startIndex = 0;
        int endIndex = 26;

        for(int i = 0; i < (getPage()-1); i++){
            startIndex=startIndex+27;
            endIndex=endIndex+27;
        }

        TownRank rank = getTown().getRank(getResident());

        if (rank.isAtLeast(TownRank.RESIDENT) && player.hasPermission("populace.commands.board")) {
            inv.setItem(46, getTown().getIcon(IconType.MSGBOARD, getResident()));
        }

        if (rank.isAtLeast(TownRank.MANAGER) && !getTown().isOpen() && player.hasPermission("populace.commands.invite")) {
            inv.setItem(48, new ItemBuilder(Material.NAME_TAG)
            .withCustomName("§b§lInviting Residents")
            .withLore(Arrays.asList("§7Invite new residents to the town",
                    "§7using §f/invite <Their Name>§7. They can accept",
                    "§7the invite by simply clicking on the bed in your",
                    "§7town menu.")).build());
        }

        if (getTown().canWarpToSpawn(getResident(), false) && player.hasPermission("populace.commands.visit")) {
            inv.setItem(50, new ItemBuilder(Material.ENDER_PEARL)
            .withCustomName("§a§lTeleport to Spawn").build());
        }

        if (rank.isAtLeast(TownRank.RESIDENT) && getTown().getMapView() != null && player.hasPermission("populace.commands.map")) {
            inv.setItem(49, new ItemBuilder(Material.MAP)
            .withCustomName("§a§lMap of " + getTown().getName() + getTown().getLevel().getSuffix())
            .withLore(Arrays.asList("§aLeft Click§f to receive it!")).build());
        }

        List<Resident> residents = getTown().getSortedResidents();
        List<Resident> display = new ArrayList<>();
        for(int i = startIndex; i <= endIndex; i++){
            if (i < residents.size()){
                display.add(residents.get(i));
            }
            else{
                nextPage = false;
            }
        }

        int index = 18;
        for(Resident resident : display){
            inv.setItem(index, getIcon(resident));
            index++;
        }

        if (nextPage){
            inv.setItem(53, new ItemBuilder(Material.ARROW).withCustomName("§e§lNEXT >").build());
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

    public Town getTown() {
        return town;
    }

    protected ItemStack getIcon(Resident resident) {
        List<String> list = new ArrayList<>();
        if (resident.isJailed()) {
            list.add("§7§oJailed");
        }
        if (getTown().getRank(resident) != TownRank.MAYOR && getTown().getRank(getResident()).isAtLeast(TownRank.MANAGER)) {
            list.add("§aLeft Click§f to change rank.");
        }
        ItemStack item = resident.getIcon();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (list.size() > 0) {
            lore.add("§8");
            lore.addAll(list);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        return ItemUtil.addTag(item, "residentid", new NBTTagString(resident.getUuid().toString()));
    }

    public enum IconType {

        MAIN,
        TREASURY,
        TOURISM,
        MSGBOARD

    }
}
