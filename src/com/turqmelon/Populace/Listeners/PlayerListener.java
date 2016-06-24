package com.turqmelon.Populace.Listeners;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.PermissionSet;
import com.turqmelon.Populace.Utils.CheckForPortalTrapTask;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentHashMap;

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
public class PlayerListener implements Listener {

    //determines whether a block type is an inventory holder.
    private ConcurrentHashMap<Integer, Boolean> inventoryHolderCache = new ConcurrentHashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event)
    {
        // for interacting with an entity in general
        if(event.getRightClicked().getType() == EntityType.ARMOR_STAND)
        {
            onPlayerInteractEntity(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        Plot plot = PlotManager.getPlot(entity.getLocation().getChunk());
        Resident resident = ResidentManager.getResident(player);

        if (resident != null && resident.isJailed()) {
            resident.getJailData().sendExplanation(resident);
            event.setCancelled(true);
            return;
        }

        //don't allow interaction with item frames or armor stands in plots without build permission
        if(entity.getType() == EntityType.ARMOR_STAND || entity instanceof Hanging) {
            if (plot != null && (resident == null || !plot.can(resident, PermissionSet.BUILD))){
                event.setCancelled(true);
                return;
            }
        }

        //if the entity is a vehicle and we're preventing theft in plots
        if(entity instanceof Vehicle) {
            //if the entity is in a plot
            if(plot != null)
            {
                //for storage entities, apply container rules (this is a potential theft!)
                if(entity instanceof InventoryHolder) {
                    if (resident == null || !plot.can(resident, PermissionSet.CONTAINER)){
                        player.sendMessage(Msg.ERR + "You can't open that here.");
                        event.setCancelled(true);
                        return;
                    }
                }
                //for boats, apply access rules
                else if(entity instanceof Boat)  {
                    if (resident == null || !plot.can(resident, PermissionSet.ACCESS)){
                        player.sendMessage(Msg.ERR + "You can't use that here.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // if the entity is an animal, apply container rules
        if((entity instanceof Animals) || (entity.getType() == EntityType.VILLAGER))  {
            //if the entity is in a plot
            if(plot != null)  {
                if (resident == null || !plot.can(resident, PermissionSet.CONTAINER)){
                    player.sendMessage(Msg.ERR + "You can't do that here.");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // prevent leashing claimed creatures
        if(entity instanceof Creature && player.getItemInHand() != null && player.getItemInHand().getType() == Material.LEASH)   {
            if(plot != null)  {
                if (resident == null || !plot.can(resident, PermissionSet.CONTAINER)){
                    player.sendMessage(Msg.ERR + "You can't do that here.");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR) return;
        if (action == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);

        Block clickedBlock = event.getClickedBlock();
        Material clickedBlockType = clickedBlock != null ? clickedBlock.getType() : Material.AIR;

        if (action == Action.LEFT_CLICK_BLOCK && clickedBlock != null) {
            if (resident != null && resident.isJailed()) {
                resident.getJailData().sendExplanation(resident);
                event.setCancelled(true);
                return;
            }
            Plot plot = PlotManager.getPlot(clickedBlock.getChunk());
            Block adjacentBlock = clickedBlock.getRelative(event.getBlockFace());
            byte lightLevel = adjacentBlock.getLightFromBlocks();
            if (lightLevel == 15 && adjacentBlock.getType() == Material.FIRE) {
                if (plot != null) {
                    if (resident == null || !plot.can(resident, PermissionSet.BUILD)) {
                        player.sendMessage(Msg.ERR + "You can't put out fire here.");
                        event.setCancelled(true);
                        player.sendBlockChange(adjacentBlock.getLocation(), adjacentBlock.getTypeId(), adjacentBlock.getData());
                        return;
                    }
                }
            }
        }

        if (clickedBlock != null && (event.getAction() == Action.RIGHT_CLICK_BLOCK && (
                this.isInventoryHolder(clickedBlock) ||
                        clickedBlockType == Material.CAULDRON ||
                        clickedBlockType == Material.JUKEBOX ||
                        clickedBlockType == Material.BEACON ||
                        clickedBlockType == Material.ANVIL ||
                        clickedBlockType == Material.CAKE_BLOCK))) {
            if (resident != null && resident.isJailed()) {
                resident.getJailData().sendExplanation(resident);
                event.setCancelled(true);
                return;
            }
            Plot plot = PlotManager.getPlot(clickedBlock.getChunk());
            if(plot != null)
            {
                if (resident == null || !plot.can(resident, PermissionSet.CONTAINER)){
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "You can't open that here.");
                }
            }
        }
        else if( clickedBlock != null &&
                ((clickedBlockType == Material.WOODEN_DOOR   ||
                        clickedBlockType == Material.ACACIA_DOOR   ||
                        clickedBlockType == Material.BIRCH_DOOR    ||
                        clickedBlockType == Material.JUNGLE_DOOR   ||
                        clickedBlockType == Material.SPRUCE_DOOR   ||
                        clickedBlockType == Material.DARK_OAK_DOOR ||
                        clickedBlockType == Material.BED_BLOCK ||
                        clickedBlockType == Material.TRAP_DOOR ||
                        clickedBlockType == Material.FENCE_GATE          ||
                        clickedBlockType == Material.ACACIA_FENCE_GATE   ||
                        clickedBlockType == Material.BIRCH_FENCE_GATE    ||
                        clickedBlockType == Material.JUNGLE_FENCE_GATE   ||
                        clickedBlockType == Material.SPRUCE_FENCE_GATE   ||
                        clickedBlockType == Material.DARK_OAK_FENCE_GATE))) {
            if (resident != null && resident.isJailed()) {
                resident.getJailData().sendExplanation(resident);
                event.setCancelled(true);
                return;
            }
            Plot plot = PlotManager.getPlot(clickedBlock.getChunk());
            if(plot != null)  {
                if (resident == null || !plot.can(resident, PermissionSet.ACCESS)){
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "You can't use that here.");
                }
            }

        }
        else if(clickedBlock != null && (clickedBlockType == null || clickedBlockType == Material.STONE_BUTTON || clickedBlockType == Material.WOOD_BUTTON || clickedBlockType == Material.LEVER)) {
            if (resident != null && resident.isJailed()) {
                resident.getJailData().sendExplanation(resident);
                event.setCancelled(true);
                return;
            }
            Plot plot = PlotManager.getPlot(clickedBlock.getChunk());
            if(plot != null)  {
                if (resident == null || !plot.can(resident, PermissionSet.ACCESS)){
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "You can't use that here.");
                }
            }
        }
        else if(clickedBlock != null && (
                clickedBlockType == Material.NOTE_BLOCK ||
                        clickedBlockType == Material.DIODE_BLOCK_ON ||
                        clickedBlockType == Material.DIODE_BLOCK_OFF ||
                        clickedBlockType == Material.DRAGON_EGG ||
                        clickedBlockType == Material.DAYLIGHT_DETECTOR ||
                        clickedBlockType == Material.DAYLIGHT_DETECTOR_INVERTED ||
                        clickedBlockType == Material.REDSTONE_COMPARATOR_ON ||
                        clickedBlockType == Material.REDSTONE_COMPARATOR_OFF )) {
            if (resident != null && resident.isJailed()) {
                resident.getJailData().sendExplanation(resident);
                event.setCancelled(true);
                return;
            }
            Plot plot = PlotManager.getPlot(clickedBlock.getChunk());
            if(plot != null)  {
                if (resident == null || !plot.can(resident, PermissionSet.BUILD)){
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "You can't change that here.");
                }
            }
        }

        else {
            //ignore all actions except right-click on a block or in the air
            if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

            if (resident != null && resident.isJailed()) {
                event.setCancelled(true);
                return;
            }

            //what's the player holding?
            ItemStack itemInHand = player.getItemInHand();
            Material materialInHand = itemInHand != null ? itemInHand.getType() : Material.AIR;

            //if it's bonemeal or armor stand or spawn egg, check for build permission (ink sac == bone meal, must be a Bukkit bug?)
            if (clickedBlock != null && (materialInHand == Material.INK_SACK || materialInHand == Material.ARMOR_STAND || materialInHand == Material.MONSTER_EGG)) {
                Plot plot = PlotManager.getPlot(clickedBlock.getChunk());
                if (plot != null) {
                    if (resident == null || !plot.can(resident, PermissionSet.BUILD)) {
                        event.setCancelled(true);
                        player.sendMessage(Msg.ERR + "You can't use that here.");
                    }
                }
            } else if (clickedBlock != null && materialInHand == Material.BOAT) {
                Plot plot = PlotManager.getPlot(clickedBlock.getChunk());
                if (plot != null) {
                    if (resident == null || !plot.can(resident, PermissionSet.ACCESS)) {
                        event.setCancelled(true);
                        player.sendMessage(Msg.ERR + "You can't use that here.");
                    }
                }

            }
        }


    }

    private boolean isInventoryHolder(Block clickedBlock)
    {
        @SuppressWarnings("deprecation")
        Integer cacheKey = clickedBlock.getTypeId();
        Boolean cachedValue = this.inventoryHolderCache.get(cacheKey);
        if(cachedValue != null)
        {
            return cachedValue;

        }
        else
        {
            boolean isHolder = clickedBlock.getState() instanceof InventoryHolder;
            this.inventoryHolderCache.put(cacheKey, isHolder);
            return isHolder;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);

        if (resident != null && resident.isJailed()) {
            Location to = event.getTo();
            if (!to.getWorld().getName().equals(resident.getJailData().getJailLocation().getWorld().getName()) ||
                    to.distanceSquared(resident.getJailData().getJailLocation()) >= (resident.getJailData().getRange() * resident.getJailData().getRange())) {
                event.setTo(resident.getJailData().getJailLocation());
                resident.getJailData().sendExplanation(resident);
                return;
            }
        }

        // prevent players from using ender pearls to gain access to secured plots
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if(cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)  {
            Plot toPlot = PlotManager.getPlot(event.getTo().getChunk());
            if(toPlot != null)
            {
                if (resident == null || !toPlot.can(resident, PermissionSet.ENTRY) || !toPlot.can(resident, PermissionSet.ACCESS)){
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "You don't have permission to Ender Pearl to that area.");
                    if(cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL){
                        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Resident resident = ResidentManager.getResident(player);
        if (resident != null && resident.isJailed()) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        if (resident == null) {
            return;
        }
        if (resident.isJailed()) {
            resident.getJailData().sendExplanation(resident);
            event.setRespawnLocation(resident.getJailData().getJailLocation());
        } else if (resident.getTown() != null && resident.getTown().canWarpToSpawn(resident, false)) {
            event.setRespawnLocation(resident.getTown().getSpawn());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event)
    {
        //if the player isn't going anywhere, take no action
        if(event.getTo() == null || event.getTo().getWorld() == null) return;


        Player player = event.getPlayer();

        if(event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)
        {
            // if players get trapped in a nether portal, send them back through to the other side
            CheckForPortalTrapTask task = new CheckForPortalTrapTask(player, event.getFrom());
            Populace.getInstance().getServer().getScheduler().runTaskLater(Populace.getInstance(), task, 200L);

            // if the player teleporting doesn't have permission to build a nether portal and none already exists at the destination, cancel the teleport

            Location destination = event.getTo();
            if(event.useTravelAgent())
            {
                if(event.getPortalTravelAgent().getCanCreatePortal())
                {
                    //hypothetically find where the portal would be created if it were
                    TravelAgent agent = event.getPortalTravelAgent();
                    agent.setCanCreatePortal(false);
                    destination = agent.findOrCreate(destination);
                    agent.setCanCreatePortal(true);
                }
                else

                {
                    //if not able to create a portal, we don't have to do anything here
                    return;
                }
            }

            //if creating a new portal
            if(destination.getBlock().getType() != Material.PORTAL)
            {
                //check for a plots and the player's build permission

                Plot plot = PlotManager.getPlot(destination.getChunk());
                Resident resident = ResidentManager.getResident(player);
                if(plot != null && (resident == null || !plot.can(resident, PermissionSet.BUILD)))
                {
                    //cancel and inform about the reason
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "Destination portal would be placed in a location where you don't have permission to build. Teleport cancelled.");
                }
            }
        }
    }

}
