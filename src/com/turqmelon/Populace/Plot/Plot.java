package com.turqmelon.Populace.Plot;

import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.*;
import com.turqmelon.Populace.Utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

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
public class Plot {

    private UUID uuid;
    private PlotChunk plotChunk;
    private PlotType type;

    private Town town;
    private Resident owner = null;

    private Map<PermissionSet, TownRank> permissions = new HashMap<>();
    private List<Resident> allowList = new ArrayList<>();

    private List<UUID> invited = new ArrayList<>();

    private boolean forSale = false;
    private double price = 0 ;

    public Plot(UUID uuid, PlotChunk plotChunk, PlotType type, Town town) {
        this.uuid = uuid;
        this.plotChunk = plotChunk;
        this.type = type;
        this.town = town;
    }

    public Plot(JSONObject object, Town town) {
        this.uuid = UUID.fromString((String) object.get("uuid"));
        this.plotChunk = new PlotChunk((JSONObject) object.get("location"));
        this.town = town;
        String ownerid = (String)object.get("resident");
        if (ownerid != null){
            this.owner = ResidentManager.getResident(UUID.fromString(ownerid));
        }
        this.type = PlotType.valueOf((String)object.get("type"));
        JSONArray perms = (JSONArray) object.get("permissions");
        for(Object o : perms){
            String perm = (String)o;
            String[] permData = perm.split(":");
            permissions.put(PermissionSet.valueOf(permData[0]), TownRank.valueOf(permData[1]));
        }
        JSONArray allowed = (JSONArray)object.get("allowlist");
        for(Object o : allowed){
            allowList.add(ResidentManager.getResident(UUID.fromString((String)o)));
        }
        this.forSale = (boolean) object.get("forsale");
        this.price = (double) object.get("price");
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("uuid", getUuid().toString());
        obj.put("location", getPlotChunk().toJSON());
        obj.put("type", getType().name());
        obj.put("town", getTown() != null ? getTown().getUuid().toString() : null);
        obj.put("resident", getOwner() != null ? getOwner().getUuid().toString() : null);
        obj.put("permissions", permissions.keySet().stream().map(set -> set.name() + ":" + permissions.get(set).name()).collect(Collectors.toCollection(JSONArray::new)));
        obj.put("allowlist", getAllowList().stream().map(resident -> resident.getUuid().toString()).collect(Collectors.toCollection(JSONArray::new)));
        obj.put("forsale", isForSale());
        obj.put("price", getPrice());
        return obj;
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public PlotType getType() {
        if (isWarzoneLand()) {
            return PlotType.BATTLE;
        } else if (isSpawnLand()) {
            return PlotType.MERCHANT;
        }
        return type;
    }

    public void setType(PlotType type) {
        this.type = type;
    }

    public boolean isSpawnLand() {
        return ((getTown() instanceof Spawn));
    }

    public boolean isWarzoneLand() {
        return ((getTown() instanceof Warzone));
    }

    public ItemStack getIcon(){
        return new ItemBuilder(Material.GRASS)
                .withCustomName("§b§l" + getTown().getName() + getTown().getLevel().getSuffix() + " Plot")
                .withLore(Arrays.asList(
                        "§7A plot is a minecraft chunk that's claimed",
                        "§7by a town.",
                        "§a",
                        "§fLocation §e" + getPlotChunk().getWorld().getName() + ", " + getPlotChunk().getX() + ", " + getPlotChunk().getZ(),
                        "§fType §e" + getType().getName(),
                        "§fOwner §e" + (getOwner()!=null?getOwner().getName():getTown().getName() + getTown().getLevel().getSuffix()),
                        "§fAllow List §e" + getAllowList().size() + " Players",
                        "§fFor Sale " + (isForSale()?"§eYes, " + Populace.getCurrency().format(getPrice()):"§cNo")
                )).build();
    }

    // Returns a resident UUID or town UUID, depending on plot ownership
    public UUID getOwnerUUID() {
        if (getOwner() != null){
            return getOwner().getUuid();
        }
        else{
            return getTown().getUuid();
        }
    }

    public boolean can(Resident resident, PermissionSet set) {

        if (isWarzoneLand()) {
            switch (set) {
                case ENTRY:
                case ACCESS:
                case CONTAINER:
                    return true;
                case SHOP:
                case BUILD:
                    return false;
                default:
                    return false;
            }
        } else if (isSpawnLand()) {
            switch (set) {
                case ENTRY:
                case SHOP:
                    return true;
                case BUILD:
                case ACCESS:
                case CONTAINER:
                    return false;
                default:
                    return false;
            }
        } else {
            if (getOwner() != null && getOwner().getUuid().equals(resident.getUuid())) {
                return true;
            }

            if (isOnAllowList(resident)) {
                return true;
            }

            Player player = Bukkit.getPlayer(resident.getUuid());
            if (player != null && player.hasPermission("populace.bypass")) {
                return true;
            }

            TownRank rank = getTown().getRank(resident);
            TownRank requiredPlot = getRequiredRank(set);
            TownRank requiredTown = getTown().getRequiredRank(set);

            return rank.isAtLeast(requiredPlot) || rank.isAtLeast(requiredTown) && set.isApplicableTo(PermissionSet.PermissionScope.TOWN);
        }

    }




    public List<UUID> getInvited() {
        if (getOwner() != null)return new ArrayList<>();
        return invited;
    }

    public boolean isOnAllowList(Resident resident){
        for(Resident r : getAllowList()){
            if (r.getUuid().equals(resident.getUuid())){
                return true;
            }
        }
        return false;
    }

    public void removeFromAllowList(Resident resident){
        if (isOnAllowList(resident)){
            getAllowList().remove(resident);
        }
    }

    public void addToAllowList(Resident resident){
        if (!isOnAllowList(resident)){
            getAllowList().add(resident);
        }
    }

    public void requireRank(PermissionSet set, TownRank rank){
        permissions.put(set, rank);
    }

    public void revertRank(PermissionSet set){
        if (permissions.containsKey(set)){
            permissions.remove(set);
        }
    }

    public List<Resident> getAllowList() {
        return allowList;
    }

    public TownRank getRequiredRank(PermissionSet set){
        return permissions.containsKey(set) ? permissions.get(set) : set.getDefaultRank();
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlotChunk getPlotChunk() {
        return plotChunk;
    }

    public Town getTown() {
        return town;
    }

    public Resident getOwner() {
        return owner;
    }

    public void setOwner(Resident owner) {
        this.owner = owner;
    }

    public boolean isForSale() {
        return getOwner() == null && forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
