package com.turqmelon.Populace.Listeners;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Plot.PlotType;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.PermissionSet;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;

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
public class BlockListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPistonRetract (BlockPistonRetractEvent event)
    {
        //pulling up is always safe
        if(event.getDirection() == BlockFace.UP) return;


        //who owns the piston, if anyone?
        UUID pistonOwner = UUID.randomUUID();
        Location pistonLocation = event.getBlock().getLocation();
        Plot pistonPlot = PlotManager.getPlot(pistonLocation.getChunk());
        if(pistonPlot != null) pistonOwner = pistonPlot.getOwnerUUID();

        UUID movingBlockOwner = UUID.randomUUID();
        for(Block movedBlock : event.getBlocks())
        {
            //who owns the moving block, if anyone?
            Plot movingBlockPlot = PlotManager.getPlot(movedBlock.getChunk());
            if(movingBlockPlot != null) movingBlockOwner = movingBlockPlot.getOwnerUUID();

            //if there are owners for the blocks, they must be the same player
            //otherwise cancel the event
            if(!pistonOwner.equals(movingBlockOwner))
            {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPistonExtend (BlockPistonExtendEvent event)
    {
        //pushing down is ALWAYS safe
        if(event.getDirection() == BlockFace.DOWN) return;


        Block pistonBlock = event.getBlock();
        List<Block> blocks = event.getBlocks();

        //if no blocks moving, then only check to make sure we're not pushing into a claim from outside
        //this avoids pistons breaking non-solids just inside a claim, like torches, doors, and touchplates
        if(blocks.size() == 0)
        {
            Block invadedBlock = pistonBlock.getRelative(event.getDirection());

            //pushing "air" is harmless
            if(invadedBlock.getType() == Material.AIR) return;

            if (PlotManager.getPlot(pistonBlock.getLocation().getChunk()) == null &&
                    PlotManager.getPlot(invadedBlock.getLocation().getChunk()) != null){
                event.setCancelled(true);
            }

            return;
        }

        //who owns the piston, if anyone?
        UUID plotOwner = UUID.randomUUID();
        Plot plot = PlotManager.getPlot(event.getBlock().getChunk());
        if (plot != null){
            plotOwner = plot.getOwnerUUID();
        }

        //which blocks are being pushed?
        for (Block block : blocks) {
            //if ANY of the pushed blocks are owned by someone other than the piston owner, cancel the event
            plot = PlotManager.getPlot(block.getChunk());
            if (plot != null) {
                if (!plot.getOwnerUUID().equals(plotOwner)) {
                    event.setCancelled(true);
                    event.getBlock().getWorld().createExplosion(event.getBlock().getLocation(), 0);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType()));
                    event.getBlock().setType(Material.AIR);
                    return;
                }
            }
        }

        //if any of the blocks are being pushed into a claim from outside, cancel the event
        for (Block block : blocks) {
            Plot originalPlot = PlotManager.getPlot(block.getChunk());
            if (originalPlot != null) {
                plotOwner = originalPlot.getOwnerUUID();
            }

            Plot newPlot = PlotManager.getPlot(block.getRelative(event.getDirection()).getChunk());
            UUID newOwnerUUID = UUID.randomUUID();
            if (newPlot != null) {
                newOwnerUUID = newPlot.getOwnerUUID();
            }

            //if pushing this block will change ownership, cancel the event and take away the piston (for performance reasons)
            if (!newOwnerUUID.equals(plotOwner)) {
                event.setCancelled(true);
                event.getBlock().getWorld().createExplosion(event.getBlock().getLocation(), 0);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType()));
                event.getBlock().setType(Material.AIR);
                return;
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockSpread (BlockSpreadEvent spreadEvent)
    {
        if(spreadEvent.getSource().getType() != Material.FIRE) return;

        // Cancels fire spreading within towns
        if(PlotManager.getPlot(spreadEvent.getBlock().getChunk()) != null) {
            spreadEvent.setCancelled(true);

            //if the source of the spread is not fire on netherrack, put out that source fire to save cpu cycles
            Block source = spreadEvent.getSource();
            if(source.getRelative(BlockFace.DOWN).getType() != Material.NETHERRACK) {
                source.setType(Material.AIR);
            }
        }
    }

    // Prevents igniting flames inside protected areas
    @EventHandler
    public void onIngite(BlockIgniteEvent event){
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        Block block = event.getBlock();
        Plot plot = PlotManager.getPlot(block.getChunk());

        if (plot != null){

            if (resident == null || !plot.can(resident, PermissionSet.BUILD)){
                event.setCancelled(true);
                player.sendMessage(Msg.ERR + "You can't ignite fires here.");
            }
            else if (plot.getType() != PlotType.BATTLE){
                boolean playersFound = false;
                for(Player p : block.getWorld().getPlayers()){
                    if (p.getUniqueId().equals(player.getUniqueId()))continue;
                    if (p.getLocation().distanceSquared(block.getLocation())<=(10*10)){
                        Plot pl = PlotManager.getPlot(p.getLocation().getChunk());
                        if (pl == null || pl.getType() == PlotType.BATTLE){
                            playersFound = true;
                            break;
                        }
                    }
                }

                if (playersFound){
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "You can't ignite fire near other players.");
                }
            }

        }
        else if (resident != null){
            resident.warnForBuilding();
        }
    }

    // Blocks are never destroyed by fire in towns
    @EventHandler
    public void onBurn(BlockBurnEvent event){
        if (PlotManager.getPlot(event.getBlock().getChunk()) != null){
            event.setCancelled(true);
        }
    }

    // Manages fluid flowing
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockFromTo (BlockFromToEvent spreadEvent)
    {
        //always allow fluids to flow straight down
        if(spreadEvent.getFace() == BlockFace.DOWN) return;

        //where to?
        Block toBlock = spreadEvent.getToBlock();
        Location toLocation = toBlock.getLocation();
        Plot toPlot = PlotManager.getPlot(toLocation.getChunk());
        Plot fromPlot = PlotManager.getPlot(spreadEvent.getBlock().getChunk());

        // Wilderness flowing is fine
        if (toPlot == null && fromPlot == null){
            return;
        }

        UUID toOwner = toPlot != null ? toPlot.getOwnerUUID() : UUID.randomUUID();
        UUID fromOwner = fromPlot != null ? fromPlot.getOwnerUUID() : UUID.randomUUID();

        // If the source block is owned by someone other then the target, no flow for you
        if (!toOwner.equals(fromOwner)){
            spreadEvent.setCancelled(true);
        }

    }

    // Prevents trees from growing inside plots from the outside
    @EventHandler(ignoreCancelled = true)
    public void onTreeGrow (StructureGrowEvent growEvent)  {


        Location rootLocation = growEvent.getLocation();
        Plot rootPlot = PlotManager.getPlot(rootLocation.getChunk());
        UUID rootOwner = UUID.randomUUID();

        //who owns the spreading block, if anyone?
        if(rootPlot != null){
            rootOwner = rootPlot.getOwnerUUID();
        }

        //for each block growing
        for (int i = 0; i < growEvent.getBlocks().size(); i++) {
            BlockState block = growEvent.getBlocks().get(i);
            Plot blockPlot = PlotManager.getPlot(block.getLocation().getChunk());

            //if it's growing into a plot
            if(blockPlot != null) {
                //if there's no owner for the new tree, or the owner for the new tree is different from the owner of the claim
                if(!rootOwner.equals(blockPlot.getOwnerUUID()))  {
                    growEvent.getBlocks().remove(i--);
                }
            }
        }
    }

    //ensures dispensers can't be used to dispense a block(like water or lava) or item across a plot boundary
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onDispense(BlockDispenseEvent dispenseEvent) {

        //from where?
        Block fromBlock = dispenseEvent.getBlock();
        Dispenser dispenser = new Dispenser(Material.DISPENSER, fromBlock.getData());

        //to where?
        Block toBlock = fromBlock.getRelative(dispenser.getFacing());
        Plot fromPlot = PlotManager.getPlot(fromBlock.getChunk());
        Plot toPlot = PlotManager.getPlot(toBlock.getChunk());

        // Wilderness is fine
        if(fromPlot == null && toPlot == null) return;

        // Plot to plot is fine
        if(fromPlot!=null&&toPlot!=null&&fromPlot.getUuid().equals(toPlot.getUuid())) return;

        // Everything else isn't
        dispenseEvent.setCancelled(true);
    }

    // Prevents cheaters from sending sign packet data for signs they can't touch
    @EventHandler
    public void onSignChange(SignChangeEvent event){
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        Block block = event.getBlock();
        Plot plot = PlotManager.getPlot(block.getChunk());

        if (plot != null){

            if (resident == null || !plot.can(resident, PermissionSet.BUILD)){
                event.setCancelled(true);
                player.sendMessage(Msg.ERR + "You can't edit signs there.");
            }

        }
        else if (resident != null){
            resident.warnForBuilding();
        }

    }

    @EventHandler
    public void onDump(PlayerBucketFillEvent event){
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        Plot plot = PlotManager.getPlot(block.getChunk());

        if (plot != null){

            if (resident == null || !plot.can(resident, PermissionSet.BUILD)){
                event.setCancelled(true);
                player.sendMessage(Msg.ERR + "You can't fill buckets there.");
            }

        }

    }

    @EventHandler
    public void onDump(PlayerBucketEmptyEvent event){
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        Plot plot = PlotManager.getPlot(block.getChunk());

        if (plot != null){

            if (resident == null || !plot.can(resident, PermissionSet.BUILD)){
                event.setCancelled(true);
                player.sendMessage(Msg.ERR + "You can't dump buckets there.");
            }
            else if (plot.getType() != PlotType.BATTLE && event.getBucket() == Material.LAVA_BUCKET){

                boolean playersFound = false;
                for(Player p : block.getWorld().getPlayers()){
                    if (p.getUniqueId().equals(player.getUniqueId()))continue;
                    if (p.getLocation().distanceSquared(block.getLocation())<=(10*10)){
                        Plot pl = PlotManager.getPlot(p.getLocation().getChunk());
                        if (pl == null || pl.getType() == PlotType.BATTLE){
                            playersFound = true;
                            break;
                        }
                    }
                }

                if (playersFound){
                    event.setCancelled(true);
                    player.sendMessage(Msg.ERR + "You can't dump lava near other players.");
                }

            }

        }
        else if (block.getLocation().getBlockY() >= block.getWorld().getSeaLevel()-7){
            event.setCancelled(true);
            player.sendMessage(Msg.ERR + "Dumping liquids in the wilderness (above sea level) is not allowed.");
        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        Block block = event.getBlock();
        Plot plot = PlotManager.getPlot(block.getChunk());

        if (plot != null){

            if (resident == null || !plot.can(resident, PermissionSet.BUILD)){
                event.setCancelled(true);
                player.sendMessage(Msg.ERR + "You can't break that.");
            }

        }
        else if (resident != null){
            resident.warnForBuilding();
        }

    }

    @EventHandler
    public void onBuild(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        Block block = event.getBlock();
        Plot plot = PlotManager.getPlot(block.getChunk());

        if (plot != null){

            if (resident == null || !plot.can(resident, PermissionSet.BUILD)){
                event.setCancelled(true);
                player.sendMessage(Msg.ERR + "You can't build there.");
            }

        }
        else if (resident != null){
            resident.warnForBuilding();
        }

    }

}
