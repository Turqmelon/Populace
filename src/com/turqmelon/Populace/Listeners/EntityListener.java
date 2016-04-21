package com.turqmelon.Populace.Listeners;

import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Plot.PlotType;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.PermissionSet;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

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
public class EntityListener implements Listener {


    //when an entity picks up an item
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityPickup(EntityChangeBlockEvent event) {

        //if its an enderman
        if(event.getEntity() instanceof Enderman) {
            //and the block is in a plot
            if(PlotManager.getPlot(event.getBlock().getChunk()) != null)  {
                //he doesn't get to steal it
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onHangingBreak(HangingBreakEvent event) {

        //explosions don't destroy hangings
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            event.setCancelled(true);
            return;
        }

        //only allow players to break paintings, not anything else (like water and explosions)
        if (!(event instanceof HangingBreakByEntityEvent)) {
            event.setCancelled(true);
            return;
        }

        HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent)event;

        //who is removing it?
        Entity remover = entityEvent.getRemover();

        //again, making sure the breaker is a player
        if(!(remover instanceof Player))
        {
            event.setCancelled(true);
            return;
        }

        //if the player doesn't have build permission, don't allow the breakage
        Player playerRemover = (Player)entityEvent.getRemover();
        Resident resident = ResidentManager.getResident(playerRemover);
        Plot plot = PlotManager.getPlot(event.getEntity().getLocation().getChunk());

        if (plot != null && (resident == null || !plot.can(resident, PermissionSet.BUILD))){
            event.setCancelled(true);
            playerRemover.sendMessage(Msg.ERR + "You can't break that here.");
        }


    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPaintingPlace(HangingPlaceEvent event)
    {

        // similar to above, placing a painting requires build permission in the plot

        Plot plot = PlotManager.getPlot(event.getEntity().getLocation().getChunk());
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);

        //if the player doesn't have permission, don't allow the placement
        if (plot != null && (resident == null || !plot.can(resident, PermissionSet.BUILD))){
            event.setCancelled(true);
            player.sendMessage(Msg.ERR + "You can't hang that here.");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){

        // Handle combat stuff

        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        // Never protect monsters
        if ((entity instanceof Monster)){
            return;
        }

        // Get where everyone is
        Plot entityPlot = PlotManager.getPlot(entity.getLocation().getChunk());
        Plot damagerPlot = PlotManager.getPlot(damager.getLocation().getChunk());

        Player player1 = null;
        Player player2 = null;

        // Determine what players (if any) we're working with
        if ((entity instanceof Player)){
            player1 = (Player)entity;
        }

        if ((damager instanceof Player)){
            player2 = (Player)damager;
        }
        else if ((damager instanceof Projectile)){
            Projectile proj = (Projectile)damager;
            if ((proj.getShooter() instanceof Player)){
                player2 = (Player)proj.getShooter();
            }
        }

        // Both are players. Great. This is PVP.
        if (player1 != null && player2 != null){

            // Determine whether or not both players are eligible for PVP
            boolean entityBattlePlot = (entityPlot == null && Configuration.WILDERNESS_PVP) || entityPlot != null && entityPlot.getType()== PlotType.BATTLE;
            boolean damagerBattlePlot = (damagerPlot == null && Configuration.WILDERNESS_PVP) || damagerPlot != null && damagerPlot.getType() == PlotType.BATTLE;

            // If either are false, cancel it, and explain why to the attacker
            if (!entityBattlePlot || !damagerBattlePlot){

                if (!damagerBattlePlot){
                    player2.sendMessage(Msg.ERR + "You're inside a no-PVP area.");
                }
                else {
                    player2.sendMessage(Msg.ERR + player1.getName() + " is inside a no-PVP area.");
                }

                event.setCancelled(true);
            }
        }

        // Damager is a player, entity is an animal. Animal griefing?
        else if (player2 != null && (entity instanceof Animals)){

            // Make sure it's not wilderness
            if (entityPlot != null){

                Resident resident = ResidentManager.getResident(player2);

                // Check if the resident has build permission
                if (resident == null || !entityPlot.can(resident, PermissionSet.BUILD)){
                    player2.sendMessage(Msg.ERR + "You can't hurt animals here.");
                    event.setCancelled(true);
                }

            }

        }



    }

    // If a player were to take fall damage as the result of being knocked out of a plot
    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)return;
        Entity entity = event.getEntity();
        if ((entity instanceof Player)){
            Player player = (Player)entity;
            Resident resident = ResidentManager.getResident(player);
            if (resident == null)return;
            if (resident.hasFallImmunity()){
                player.setFallDistance(0F);
                event.setCancelled(true);
                resident.setFallImmunity(0);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event)
    {
        ThrownPotion potion = event.getPotion();

        //ignore potions not thrown by players
        ProjectileSource projectileSource = potion.getShooter();
        if(projectileSource == null || !(projectileSource instanceof Player)) return;
        Player thrower = (Player)projectileSource;
        Resident resident = ResidentManager.getResident(thrower);

        Collection<PotionEffect> effects = potion.getEffects();
        for(PotionEffect effect : effects)
        {
            PotionEffectType effectType = effect.getType();

            //restrict some potions on protected animals (griefers could use this to kill or steal animals over fences)
            if(effectType.getName().equals("JUMP") || effectType.getName().equals("POISON"))
            {
                for(LivingEntity effected : event.getAffectedEntities())
                {
                    if(effected instanceof Animals)
                    {
                        Plot plot = PlotManager.getPlot(effected.getLocation().getChunk());
                        if(plot != null)
                        {
                            if(resident == null || !plot.can(resident, PermissionSet.CONTAINER))
                            {
                                event.setCancelled(true);
                                thrower.sendMessage(Msg.ERR + "You can't hurt animals here.");
                                return;
                            }
                        }
                    }
                }
            }

            //otherwise, no restrictions for positive effects
            if(positiveEffects.contains(effectType)) continue;

            for(LivingEntity effected : event.getAffectedEntities())
            {
                //always impact the thrower
                if(effected == thrower) continue;

                //always impact non players
                if(!(effected instanceof Player)) {
                }

                //otherwise if in no-pvp zone, stop effect
                else {
                    Player effectedPlayer = (Player)effected;
                    Plot throwerPlot = PlotManager.getPlot(thrower.getLocation().getChunk());
                    if(throwerPlot != null && throwerPlot.getType() != PlotType.BATTLE) {
                        event.setIntensity(effected, 0);
                        continue;
                    }

                    Plot defenderPlot = PlotManager.getPlot(effectedPlayer.getLocation().getChunk());
                    if(defenderPlot != null && defenderPlot.getType() != PlotType.BATTLE) {
                        event.setIntensity(effected, 0);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {

        Plot plot = PlotManager.getPlot(event.getLocation().getChunk());

        // no spawning in towns
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (plot != null && (reason == CreatureSpawnEvent.SpawnReason.NATURAL ||
                reason == CreatureSpawnEvent.SpawnReason.SPAWNER)){
            event.setCancelled(true);
            return;
        }

        // We're going to limit how many entities each of these types will spawn
        // Otherwise things CAN get spammy

        int entities = 0;
        if (reason == CreatureSpawnEvent.SpawnReason.BREEDING ||
                reason == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG ||
                reason == CreatureSpawnEvent.SpawnReason.EGG ||
                reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
                reason == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM ||
                reason == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN){

            for(Entity entity : event.getEntity().getNearbyEntities(30, 30, 30)){
                if ((entity.getType() == event.getEntity().getType())){
                    entities++;
                    if (entities >= 30){
                        break;
                    }
                }
            }

            // Too many entities. Stop.
            if (entities >= 30){
                event.setCancelled(true);
            }

        }



    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {

        Plot plot = PlotManager.getPlot(event.getBlock().getChunk());

        if(plot != null && event.getEntityType() == EntityType.ENDERMAN){
            event.setCancelled(true);
        }

        else if(plot != null && event.getEntityType() == EntityType.SILVERFISH)  {
            event.setCancelled(true);
        }

        else if(plot != null && event.getEntityType() == EntityType.RABBIT)  {
            event.setCancelled(true);
        }

        //don't allow the wither to break blocks, when the wither is determined, too expensive to constantly check for protected blocks
        else if(plot != null && event.getEntityType() == EntityType.WITHER)  {
            event.setCancelled(true);
        }

        //don't allow crops to be trampled, except by a player with build permission
        else if(event.getTo() == Material.DIRT && event.getBlock().getType() == Material.SOIL) {
            if(event.getEntityType() != EntityType.PLAYER) {
                event.setCancelled(true);
            }
            else {
                Player player = (Player)event.getEntity();
                Resident resident = ResidentManager.getResident(player);
                if(plot != null && (resident == null || !plot.can(resident, PermissionSet.BUILD)))  {
                    event.setCancelled(true);
                }
            }
        }

        //sand cannon fix - when the falling block doesn't fall straight down, take additional anti-grief steps
        else if (event.getEntityType() == EntityType.FALLING_BLOCK)
        {
            FallingBlock entity = (FallingBlock)event.getEntity();
            Block block = event.getBlock();

            //if changing a block TO air, this is when the falling block formed.  note its original location
            if(event.getTo() == Material.AIR) {
                entity.setMetadata("populace.fallingblock", new FixedMetadataValue(Populace.getInstance(), block.getLocation()));
            }
            //otherwise, the falling block is forming a block.  compare new location to original source
            else {
                List<MetadataValue> values = entity.getMetadata("populace.fallingblock");

                //if we're not sure where this entity came from (maybe another plugin didn't follow the standard?), allow the block to form
                if(values.size() < 1) return;

                Location originalLocation = (Location)(values.get(0).value());
                Location newLocation = block.getLocation();

                //if did not fall straight down
                if(originalLocation.getBlockX() != newLocation.getBlockX() || originalLocation.getBlockZ() != newLocation.getBlockZ()) {

                    //if landing in plot, only allow if source was also in the land claim
                    Plot newPlot = PlotManager.getPlot(newLocation.getChunk());
                    Plot originalPlot = PlotManager.getPlot(originalLocation.getChunk());

                    UUID owner1 = newPlot != null ? newPlot.getOwnerUUID() : UUID.randomUUID();
                    UUID owner2 = originalPlot != null ? originalPlot.getUuid() : UUID.randomUUID();

                    if(!owner1.equals(owner2)) {
                        //when not allowed, drop as item instead of forming a block
                        event.setCancelled(true);
                        ItemStack itemStack = new ItemStack(entity.getMaterial(), 1, entity.getBlockData());
                        Item item = block.getWorld().dropItem(entity.getLocation(), itemStack);
                        item.setVelocity(new Vector());
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityFormBlock(EntityBlockFormEvent event)
    {
        Entity entity = event.getEntity();
        if((entity instanceof Player))  {
            Player player = (Player)event.getEntity();
            Plot plot = PlotManager.getPlot(event.getBlock().getChunk());
            if (plot != null && !plot.can(ResidentManager.getResident(player), PermissionSet.BUILD)){
                event.setCancelled(true);
            }
        }
    }

    public static final HashSet<PotionEffectType> positiveEffects = new HashSet<>(Arrays.asList
            (
                    PotionEffectType.ABSORPTION,
                    PotionEffectType.DAMAGE_RESISTANCE,
                    PotionEffectType.FAST_DIGGING,
                    PotionEffectType.FIRE_RESISTANCE,
                    PotionEffectType.HEAL,
                    PotionEffectType.HEALTH_BOOST,
                    PotionEffectType.INCREASE_DAMAGE,
                    PotionEffectType.INVISIBILITY,
                    PotionEffectType.JUMP,
                    PotionEffectType.NIGHT_VISION,
                    PotionEffectType.REGENERATION,
                    PotionEffectType.SATURATION,
                    PotionEffectType.SPEED,
                    PotionEffectType.WATER_BREATHING
            ));

}
