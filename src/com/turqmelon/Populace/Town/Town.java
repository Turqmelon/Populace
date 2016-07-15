package com.turqmelon.Populace.Town;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.Events.Resident.ResidentJoinTownEvent;
import com.turqmelon.Populace.Events.Resident.ResidentKickedFromTownEvent;
import com.turqmelon.Populace.Events.Resident.ResidentLeaveTownEvent;
import com.turqmelon.Populace.Events.Town.*;
import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.GUI.TownManagement.BonusLandGUI;
import com.turqmelon.Populace.Plot.*;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Utils.ClockUtil;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
public class Town implements Comparable {

    private UUID uuid;
    private String name;

    private TownLevel level;
    private boolean open = false; // Allows anyone to join the town
    private double bank = 0;

    private double tax = 0; // The daily tax on residents
    private double plotTax = 0; // The daily tax on each plot owned
    private double salesTax = 0; // Percentage added to shop prices

    private Location spawn = null;

    private long founded = 0; // The timestamp of when a town was created

    private long bonusLand = 0; // The amount of bonus land purchased by the town

    private List<Plot> plots = new ArrayList<>(); // All the plots owned by the town
    private Map<Resident, TownRank> residents = new HashMap<>(); // All the residents within the town (and their rank)
    private Map<PermissionSet, TownRank> permissions = new HashMap<>(); // The permission settings for the town

    private MapView mv = null; // The generated map for the town
    private int mvid = -1;

    private Location townJail = null; // The jail point for the town

    private List<TownAnnouncement> announcements = new ArrayList<>();

    public Town(UUID uuid, String name, TownLevel level) {
        this.uuid = uuid;
        this.name = name;
        this.level = level;
        if (getPlots().size() > 0){
            initializeMapView();
        }
    }

    public Town(JSONObject object){
        this.uuid = UUID.fromString((String) object.get("uuid"));
        this.name = (String) object.get("name");

        this.open = (boolean) object.get("open");
        this.bank = (double) object.get("bank");

        this.tax = (double) object.get("tax");
        this.plotTax = (double) object.get("plottax");
        this.salesTax = (double) object.get("salestax");

        this.mvid = (int) ((long) object.get("mapid"));

        String spawnData = (String) object.get("spawn");
        if (spawnData != null){
            String[] ds = spawnData.split(",");
            World world = Bukkit.getWorld(ds[0]);
            if (world != null){
                double x = Double.parseDouble(ds[1]);
                double y = Double.parseDouble(ds[2]);
                double z = Double.parseDouble(ds[3]);
                float yaw = Float.parseFloat(ds[4]);
                float pitch = Float.parseFloat(ds[5]);
                this.spawn = new Location(world, x, y, z, yaw, pitch);
            }
        }
        Object jailData = object.getOrDefault("jail", null);
        if (jailData != null) {
            String jd = (String) jailData;
            String[] ds = jd.split(",");
            World world = Bukkit.getWorld(ds[0]);
            if (world != null) {
                double x = Double.parseDouble(ds[1]);
                double y = Double.parseDouble(ds[2]);
                double z = Double.parseDouble(ds[3]);
                float yaw = Float.parseFloat(ds[4]);
                float pitch = Float.parseFloat(ds[5]);
                this.townJail = new Location(world, x, y, z, yaw, pitch);
            }
        }

        this.founded = (long) object.get("founded");
        this.bonusLand = (long) object.get("bonusland");

        JSONArray announcements = (JSONArray) object.getOrDefault("announcements", null);
        if (announcements != null) {
            for (Object o : announcements) {
                this.announcements.add(new TownAnnouncement((JSONObject) o));
            }
        }

        if (this.announcements.size() > 0) {
            Collections.sort(this.announcements);
        }

        JSONArray res = (JSONArray) object.get("residents");
        for(Object o : res){
            String r = (String)o;
            String[] rd = r.split(":");
            Resident resident = ResidentManager.getResident(UUID.fromString(rd[0]));
            if (resident == null){
                continue;
            }
            TownRank rank = TownRank.valueOf(rd[1]);
            getResidents().put(resident, rank);
            resident.setTown(this);
        }

        JSONArray perms = (JSONArray) object.get("permissions");
        for(Object o : perms){
            String perm = (String)o;
            String[] permData = perm.split(":");
            permissions.put(PermissionSet.valueOf(permData[0]), TownRank.valueOf(permData[1]));
        }

        loadPlots(object);

        this.level = TownLevel.getAppropriateLevel(getResidents().size());
        initializeMapView();
    }

    // Overridden by warzone and spawn classes
    public boolean isSpecial() {
        return false;
    }

    public void loadPlots(JSONObject object) {
        JSONArray plots = (JSONArray)object.get("plots");
        for(Object o : plots){
            JSONObject obj = (JSONObject)o;
            Plot plot = new Plot(obj, this);
            getPlots().add(plot);
            if (plot.getOwner() != null){
                plot.getOwner().getPlotChunks().add(plot.getPlotChunk());
            }
        }
    }

    public Location getTownJail() {
        return townJail;
    }

    public void setTownJail(Location townJail) {
        this.townJail = townJail;
    }

    public JSONObject toJSON(){
        JSONObject object = new JSONObject();

        object.put("uuid", getUuid().toString());
        object.put("name", getName());

        object.put("open", isOpen());
        object.put("bank", getBank());

        object.put("tax", getTax());
        object.put("plottax", getPlotTax());
        object.put("salestax", getSalesTax());

        object.put("spawn", getSpawn()!=null?getSpawn().getWorld().getName()+","+getSpawn().getX()+","+getSpawn().getY()+","+getSpawn().getZ()+","+getSpawn().getYaw()+","+getSpawn().getPitch():null);
        object.put("jail", getTownJail() != null ? getTownJail().getWorld().getName() + "," + getTownJail().getX() + "," + getTownJail().getY() + "," + getTownJail().getZ() + "," + getTownJail().getYaw() + "," + getTownJail().getPitch() : null);
        object.put("founded", getFounded());
        object.put("bonusland", getBonusLand());

        JSONArray res = new JSONArray();
        for(Resident resident : getResidents().keySet()){
            TownRank rank = getResidents().get(resident);
            res.add(resident.getUuid().toString() + ":" + rank.name());
        }

        JSONArray theplots = new JSONArray();
        for(Plot plot : getPlots()){
            theplots.add(plot.toJSON());
        }

        object.put("plots", theplots);
        object.put("residents", res);
        object.put("permissions", permissions.keySet().stream().map(set -> set.name() + ":" + permissions.get(set).name()).collect(Collectors.toCollection(JSONArray::new)));

        object.put("mapid", this.mvid);

        JSONArray announcements = new JSONArray();
        for (TownAnnouncement announcement : getAnnouncements()) {
            announcements.add(announcement.toJSON());
        }

        object.put("announcements", announcements);

        return object;
    }

    public void sendAnnouncementMOTD(Resident resident) {
        if (getLevel().getResidents() < TownLevel.VILLAGE.getResidents()) return;
        List<TownAnnouncement> toDisplay = new ArrayList<>();
        for (TownAnnouncement announcement : getAnnouncements()) {
            if (toDisplay.size() >= 5) break;
            if (getRank(resident).isAtLeast(announcement.getRequiredRank())) {
                toDisplay.add(announcement);
            }
        }

        if (toDisplay.size() > 0) {
            resident.sendMessage(" ");
            resident.sendMessage(ChatColor.LIGHT_PURPLE + " --- " + ChatColor.BOLD + getName().toUpperCase() + " MESSAGE BOARD " + ChatColor.LIGHT_PURPLE + " ---------------");
            for (TownAnnouncement announcement : toDisplay) {
                resident.sendMessage(ChatColor.DARK_GRAY + " - " + announcement.getRequiredRank().getPrefix() + announcement.getTitle() + ChatColor.WHITE + " " + ClockUtil.formatDateDiff(announcement.getPosted(), true) + " ago");
            }
            resident.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.WHITE + "/board" + ChatColor.LIGHT_PURPLE + " to view town board.");
            resident.sendMessage(" ");
        }
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public MapView getMapView() {
        return this.mv;
    }

    public long getFounded() {
        return founded;
    }

    public void setFounded(long founded) {
        this.founded = founded;
    }

    public Plot getPlot(World world, long x, long z){
        for(Plot plot : getPlots()){
            if (plot.getPlotChunk().getWorld().getName().equals(world.getName()) &&
                    plot.getPlotChunk().getX() == x &&
                    plot.getPlotChunk().getZ() == z){
                return plot;
            }
        }
        return null;
    }

    public Map<PermissionSet, TownRank> getPermissions() {
        return permissions;
    }

    public List<TownAnnouncement> getAnnouncements() {
        return announcements;
    }

    public long getMaxLand() {
        return getLevel().getMaxland() + getBonusLand();
    }

    // Calculates how many of the purchased bonus blocks are in used
    // (Prevents selling bonus blocks when they're in use)
    public int getUsedBonusBlocks() {
        int claimed = getPlots().size();
        int levelLimit = getLevel().getMaxland();
        return claimed-levelLimit;
    }

    // Sells bonus blocks on behalf of a resident.
    // Validates to make sure they're unused, and calls associated event
    public boolean sellBonusBlocks(Resident resident, int amount) {
        long newAmount = getBonusLand()-amount;
        if (newAmount >= getUsedBonusBlocks()){
            if (newAmount >= 0){

                TownBonusBlocksChangedEvent event = new TownBonusBlocksChangedEvent(this, resident, BonusLandGUI.LandAction.SELL);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return false;
                }

                setBonusLand(newAmount);
                return true;
            }
        }
        return false;
    }

    // Purchases bonus blocks on behalf of a user
    // Validates that the town is of level to buy it
    public boolean buyBonusBlocks(Resident resident, int amount) {

        long newAmount = getBonusLand()+amount;
        if (newAmount <= getLevel().getMaxland()){

            TownBonusBlocksChangedEvent event = new TownBonusBlocksChangedEvent(this, resident, BonusLandGUI.LandAction.BUY);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }

            setBonusLand(newAmount);
            return true;
        }

        return false;
    }

    // Gets the timestamp that the town grace period expires
    // During the grace period, no daily upkeep is collected from a town
    public long getGracePeriodExpiration() {
        return getFounded()+ TimeUnit.HOURS.toMillis(Configuration.TOWN_GRACE_PERIOD_HOURS);
    }

    // Calculates the daily upkeep of the town - collected on each "new day".
    // If a town can't pay, it's destroyed.
    public double getDailyUpkeep(){

        double landCost = 0;
        for(Plot plot : getPlots()){
            landCost += plot.getType().getDailyCost();
        }

        double residentCost = 0;
        int residentTick = 0;

        double landMultiplier = 1;

        for(Resident resident : getResidents().keySet()){
            residentTick++;
            if (residentTick == 4){
                residentTick = 0;
                landMultiplier = landMultiplier + 0.3;
            }
            TownRank rank = getResidents().get(resident);
            residentCost += rank.getCost();
        }

        landCost = landCost + (getBonusLand()*Configuration.BONUS_LAND_DAILY_COST);

        landCost = landCost * landMultiplier;

        double cost = landCost + residentCost;
        cost = cost * getLevel().getUpkeepModifier();

        return cost + Configuration.TOWN_MINIMUM_UPKEEP;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    // Re
    public TownRank getPermissionLevel(Resident resident){
        if (getResidents().containsKey(resident)){
            return getResidents().get(resident);
        }
        return TownRank.GUEST;
    }

    public void revertRequiredRank(PermissionSet set){
        if (permissions.containsKey(set)){
            permissions.remove(set);
        }
    }

    public Resident getMayor(){
        for(Resident resident : getResidents().keySet()){
            TownRank rank = getResidents().get(resident);
            if (rank == TownRank.MAYOR){
                return resident;
            }
        }
        return null;
    }

    public void setRequiredRank(PermissionSet set, TownRank rank){
        permissions.put(set, rank);
    }

    public TownRank getRequiredRank(PermissionSet set){
        if (permissions.containsKey(set)){
            return permissions.get(set);
        }
        return set.getDefaultRank();
    }

    public boolean claimLand(Resident resident, PlotChunk chunk, boolean silent) {
        return claimLand(resident, chunk, false, silent);
    }

    protected boolean buyChunk(Resident resident, PlotChunk chunk, boolean outpost) {

        // When the resident in null, this is the warzone claiming land

        double price = outpost ? Configuration.OUTPOST_CLAIM_COST : Configuration.PLOT_CLAIM_COST;
        if (resident == null || getBank() >= price) {

            Plot plot = new Plot(UUID.randomUUID(), chunk, outpost ? PlotType.OUTPOST : PlotType.RESIDENTIAL, this);

            if (resident != null) {
                TownClaimLandEvent event = new TownClaimLandEvent(this, resident, plot);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return false;
                }
                setBank(getBank() - price);
                sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " purchased " + (outpost ? "an outpost" : "a chunk") + " for the town.");
            }

            getPlots().add(plot);

            if (resident != null) {
                // For the first plot, set a town spawn so it can be warped back to
                if (getPlots().size() == 1) {
                    Player player = Bukkit.getPlayer(resident.getUuid());
                    if (player != null) {
                        setSpawn(player.getLocation());
                    }
                    initializeMapView();

                }

            }

            return true;

        }
        else{
            resident.sendMessage(Msg.ERR + "Town bank has insufficient funding. It needs " + Populace.getCurrency().format(price) + ".");
        }
        return false;
    }

    public void initializeMapView() {
        if (getPlots().size() > 0){
            if (this.mvid != -1){
                this.mv = Bukkit.getMap((short) this.mvid);

            }
            else{
                this.mv = Bukkit.createMap(getPlots().get(0).getPlotChunk().getWorld());
                this.mvid = getMapView().getId();
            }
            getMapView().addRenderer(new TownMapRenderer(this));
        }
    }

    public boolean unclaimLand(Resident resident, Plot plot){
        return unclaimLand(resident, plot, false);
    }

    public boolean canWarpToSpawn(Resident resident, boolean verbose){

        // Removes the town spawn if it is suddenly no longer in town territory
        if (getSpawn() != null){
            Chunk ch = getSpawn().getChunk();
            Plot plot = PlotManager.getPlot(ch);
            if (plot == null || !plot.getTown().getUuid().equals(getUuid())){
                setSpawn(null);
            }
        }

        if (getSpawn() != null){

            TownRank rank = getRank(resident);
            TownRank required = getRequiredRank(PermissionSet.VISIT);
            if (rank.getPermissionLevel() >= required.getPermissionLevel()){
                return true;
            }
            else if (verbose){
                resident.sendMessage(Msg.ERR + "Only " + required.getPrefix() + "§cof " + getName() + getLevel().getSuffix() + " may warp to the spawn.");
            }

        }
        else if (verbose){
            resident.sendMessage(Msg.ERR + getName() + getLevel().getSuffix() + "has no spawn. Ask the mayor to set one!");
        }
        return false;
    }

    private boolean unclaimLand(Resident resident, Plot plot, boolean confirmed){

        TownRank rank = getRank(resident);
        if (plot != null) {

            int adjacent = 0;
            Plot p1 = PlotManager.getPlot(plot.getPlotChunk().getWorld(), plot.getPlotChunk().getX() + 1, plot.getPlotChunk().getZ());
            Plot p2 = PlotManager.getPlot(plot.getPlotChunk().getWorld(), plot.getPlotChunk().getX(), plot.getPlotChunk().getZ() + 1);
            Plot p3 = PlotManager.getPlot(plot.getPlotChunk().getWorld(), plot.getPlotChunk().getX() - 1, plot.getPlotChunk().getZ());
            Plot p4 = PlotManager.getPlot(plot.getPlotChunk().getWorld(), plot.getPlotChunk().getX(), plot.getPlotChunk().getZ() - 1);

            if (p1 != null && p1.getTown().getUuid().equals(getUuid())) {
                adjacent++;
            }

            if (p2 != null && p2.getTown().getUuid().equals(getUuid())) {
                adjacent++;
            }

            if (p3 != null && p3.getTown().getUuid().equals(getUuid())) {
                adjacent++;
            }

            if (p4 != null && p4.getTown().getUuid().equals(getUuid())) {
                adjacent++;
            }

            // The owner asking to unclaim their plot won't really unclaim it, just relinquish ownership
            if (resident != null && plot.getOwner() != null && plot.getOwner().getUuid().equals(resident.getUuid())) {

                if (confirmed){
                    Resident owner = plot.getOwner();
                    plot.getOwner().getPlotChunks().clear();
                    plot.setOwner(null);
                    plot.getAllowList().clear();
                    resident.sendMessage(Msg.OK + "You gave up ownership of this plot.");
                    sendTownBroadcast(TownRank.ASSISTANT, resident.getName() + " gave up ownership of a plot in " + plot.getPlotChunk().getWorld().getName() + " @ X: " + plot.getPlotChunk().getX() +", Z: " + plot.getPlotChunk().getZ());
                    if (owner.getTown() != null) {
                        owner.getTown().getPlots().stream().filter(p -> p.getOwner() != null && p.getOwner().getUuid().equals(resident.getUuid())).forEach(p -> owner.getPlotChunks().add(p.getPlotChunk()));
                    }
                }
                else{
                    resident.sendMessage(Msg.INFO + "Are you sure you want to give up ownership of this plot?");
                    resident.sendMessage(Msg.INFO + "You'll lose access to everything in it" + (getPlotTax()>0?" and you won't be taxed for it anymore.":"."));
                    resident.setPendingAction(() -> unclaimLand(resident, plot, true));
                }
                return true;

            } else if (resident != null && plot.getOwner() != null && rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()) {

                if (adjacent == 4){
                    resident.sendMessage(Msg.ERR + "You can't use that in the middle of town. Unclaiming must be done from the borders.");
                    return false;
                }

                if (getPlots().size() == 1){
                    resident.sendMessage(Msg.ERR + "Towns must have at least 1 claim.");
                    resident.sendMessage(Msg.ERR + "If you made a mistake, destroy your town and start over.");
                    return false;
                }

                if (confirmed){

                    TownUnclaimLandEvent event = new TownUnclaimLandEvent(this, resident, plot);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return false;
                    }

                    plot.getOwner().getPlotChunks().remove(plot.getPlotChunk());
                    getPlots().remove(plot);
                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " unclaimed an owned plot in " + plot.getPlotChunk().getWorld().getName() + " @ X: " + plot.getPlotChunk().getX() +", Z: " + plot.getPlotChunk().getZ());
                }
                else{
                    resident.sendMessage(Msg.INFO + "Unclaiming this plot will cause it to become Wilderness.");
                    resident.setPendingAction(() -> unclaimLand(resident, plot, true));
                }
                return true;
            } else if (resident == null || rank.getPermissionLevel() >= TownRank.ASSISTANT.getPermissionLevel()) {

                if (resident != null && adjacent == 4) {
                    resident.sendMessage(Msg.ERR + "You can't use that in the middle of town. Unclaiming must be done from the borders.");
                    return false;
                }

                if (resident != null && getPlots().size() == 1) {
                    resident.sendMessage(Msg.ERR + "Towns must have at least 1 claim.");
                    resident.sendMessage(Msg.ERR + "If you made a mistake, destroy your town and start over.");
                    return false;
                }

                if (resident != null) {
                    TownUnclaimLandEvent event = new TownUnclaimLandEvent(this, resident, plot);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return false;
                    }
                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " unclaimed a plot in " + plot.getPlotChunk().getWorld().getName() + " @ X: " + plot.getPlotChunk().getX() + ", Z: " + plot.getPlotChunk().getZ());
                }

                getPlots().remove(plot);
                return true;
            }
            else{
                resident.sendMessage(Msg.ERR + "You can't do that here.");
            }

        } else if (resident != null) {
            resident.sendMessage(Msg.ERR + "There's no plot here!");
        }
        return false;
    }

    private boolean claimLand(Resident resident, PlotChunk chunk, boolean confirmed, boolean silent) {

        TownRank rank = getRank(resident);

        ClaimFailureReason reason = canClaimLand(chunk);
        switch (reason) {
            case ALL_LAND_USED:
                resident.sendMessage(Msg.ERR + "Town can't claim anymore land at it's current size.");
                if (rank == TownRank.MAYOR) {
                    resident.sendMessage(Msg.ERR + "Consider buying bonus land from your §f/town§c menu or inviting more residents.");
                } else {
                    resident.sendMessage(Msg.ERR + "Consider asking the Mayor to buy bonus land.");
                }
                return false;
            case ALREADY_CLAIMED:

                Plot plot = PlotManager.getPlot(chunk);
                if (plot.getTown().getUuid().equals(getUuid()) && ((resident != null && plot.getInvited().contains(resident.getUuid())) || plot.isForSale())) {
                    double price = plot.getPrice();
                    if (confirmed) {
                        Account account = AccountManager.getAccount(resident.getUuid());
                        if (price == 0 || (account != null && account.withdraw(Populace.getCurrency(), price))) {
                            setBank(getBank() + price);
                            resident.getPlotChunks().add(chunk);
                            plot.setOwner(resident);
                            plot.setPrice(0);
                            plot.setForSale(false);
                            plot.getInvited().clear();
                            if (price > 0) {
                                sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " bought a plot for " + Populace.getCurrency().format(price) + "!");
                            }
                            else{
                                sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " accepted an invitation to claim a plot!");
                            }
                        }
                        else{
                            resident.sendMessage(Msg.ERR + "You don't have enough " + Populace.getCurrency().getPlural() + "!");
                        }
                    } else {
                        if (price > 0) {
                            resident.sendMessage(Msg.INFO + "Purchase this plot from the town for " + Populace.getCurrency().format(price) + "?");
                            resident.setPendingAction(() -> claimLand(resident, chunk, true, silent));
                        } else {
                            resident.sendMessage(Msg.INFO + "Claim this plot as yours?");
                            resident.setPendingAction(() -> claimLand(resident, chunk, true, silent));
                        }

                    }
                    return true;
                } else {
                    if (!silent) resident.sendMessage(Msg.ERR + "That land is already claimed and not for sale.");
                }
                return false;
            case TOO_CLOSE_TO_TOWN:
                resident.sendMessage(Msg.ERR + "You can't place your first claim here, as it's within " + Configuration.TOWN_MINIMUM_BUFFER + " blocks of another town's borders.");
                return false;
            case REQUIRE_OUTPOST:

                Player player = Bukkit.getPlayer(resident.getUuid());

                if (player != null && player.hasPermission("populace.plottypes.outpost")) {
                    if (getRank(resident).isAtLeast(TownRank.MANAGER)) {
                        if (getLevel().getResidents() >= TownLevel.CITY.getResidents()) {
                            if (confirmed) {
                                return buyChunk(resident, chunk, true);
                            } else {
                                resident.sendMessage(Msg.INFO + "This land is not adjacent to any existing town claim, so it must be an outpost.");
                                resident.sendMessage(Msg.INFO + "Outposts have a higher daily upkeep than normal claims.");
                                resident.setPendingAction(() -> claimLand(resident, chunk, true, silent));
                            }
                            return true;
                        }
                        else{
                            resident.sendMessage(Msg.ERR + "To claim an outpost, your town must be a City. (" + TownLevel.CITY.getResidents() + " Residents)");
                            resident.sendMessage(Msg.ERR + "Land claims must be adjacent to existing claims to not be considered outposts.");
                        }
                    } else {
                        resident.sendMessage(Msg.ERR + "Only Managers can claim outposts.");
                    }
                } else if (player != null) {
                    player.sendMessage(Msg.ERR + "You don't have permission to claim outposts.");
                }

                return false;
            default:

                if (resident == null || rank.isAtLeast(TownRank.ASSISTANT)) {
                    return buyChunk(resident, chunk, false);
                } else {
                    resident.sendMessage(Msg.ERR + "Only Assistants can claim land.");
                    return false;
                }


        }

    }

    private ClaimFailureReason canClaimLand(PlotChunk chunk){

        Plot p = PlotManager.getPlot(chunk);
        if (p != null){
            return ClaimFailureReason.ALREADY_CLAIMED;
        }

        if (isSpecial()) {
            return ClaimFailureReason.NONE;
        }

        if (getPlots().size() >= getMaxLand()){
            return ClaimFailureReason.ALL_LAND_USED;
        }

        boolean adjacent = false;
        for(int x = -1; x <= 1; x++){
            for(int z = -1; z <= 1; z++){
                PlotChunk c = new PlotChunk(chunk.getWorld(), chunk.getX()+x, chunk.getZ()+z);
                Plot plot = PlotManager.getPlot(c);
                if (plot != null && plot.getTown() != null && plot.getTown().getUuid().equals(getUuid())){
                    if (plot.getType() != PlotType.OUTPOST){
                        adjacent = true;
                        break;
                    }
                }
            }
        }

        if (getPlots().size() == 0){
            adjacent = true;

            int chunkView = Configuration.TOWN_MINIMUM_BUFFER / 16;

            for (int x = -chunkView; x <= chunkView; x++) {
                for (int z = -chunkView; z <= chunkView; z++) {
                    PlotChunk c = new PlotChunk(chunk.getWorld(), chunk.getX()+x, chunk.getZ()+z);
                    Plot plot = PlotManager.getPlot(c);
                    if (plot != null){
                        return ClaimFailureReason.TOO_CLOSE_TO_TOWN;
                    }
                }
            }

        }

        if (!adjacent){
            return ClaimFailureReason.REQUIRE_OUTPOST;
        }

        return ClaimFailureReason.NONE;
    }

    public ItemStack getIcon(TownGUI.IconType icon, Resident resident) {
        return getIcon(icon, resident, true);
    }

    public ItemStack getIcon(TownGUI.IconType icon, Resident resident, boolean onMenu){
        TownRank rank = getRank(resident);

        switch(icon){
            case MAIN:

                List<String> list = new ArrayList<>();
                if (onMenu){
                    list.addAll(Arrays.asList(
                            "§7Towns will level up as people move in. A",
                            "§7higher town level allows it to claim more land.",
                            "§a"
                    ));
                }
                list.addAll(Arrays.asList(

                        "§fMayor §e" + getMayor().getName(),
                        "§fFounded §e" + ClockUtil.formatDateDiff(getFounded(), true) + " ago",
                        "§fLevel §e" + getLevel().getColor() + getLevel().getName() + (getNextLevel() != null ? " §7§o(Next level at " + getNextLevel().getResidents() + " Residents)" : ""),
                        "§fLand §e" + getPlots().size() + "§f/§e" + getMaxLand(),
                        "§fResidents §e" + getResidents().size(),
                        "§fBonus Land §e" + getBonusLand(),
                        "§fStatus §e" + (isOpen()?"§aAnybody can move in.":"§cNew residents must be invited.")));
                if (onMenu){
                    if (rank == TownRank.GUEST && (isInvited(resident)||isOpen())){
                        list.add("§a");
                        list.add("§aLeft Click§f to join " + getName() + getLevel().getSuffix());
                    }
                    else if (rank == TownRank.MAYOR){
                        list.add("§a");
                        list.add("§aLeft Click§f to buy or sell bonus land.");
                        list.add("§aRight Click§f to change your town status.");
                        list.add("§cSneak Right Click§f to destroy the town.");
                    }
                    else if (rank.getPermissionLevel() >= TownRank.RESIDENT.getPermissionLevel()){
                        list.add("§a");
                        list.add("§fYou're a(n) §e" + rank.getPrefix());
                        list.add("§aLeft Click§f to leave " + getName() + getLevel().getSuffix());
                    }
                }
                else{
                    list.add("§a");
                    list.add("§aLeft Click§f for more info.");
                }

                return new ItemBuilder(Material.BED)
                        .withCustomName("§b§l" + getName() + getLevel().getSuffix())
                        .withLore(list).tagWith("clickedtownid", new NBTTagString(getUuid().toString())).build();
            case TREASURY:

                list = new ArrayList<>();
                list.addAll(Arrays.asList(
                        "§7Taxes are selected by the mayor to keep ",
                        "§7the town running. If the town can't afford ",
                        "§7the §cDaily Upkeep§7, it will be destroyed.",
                        "§a",
                        "§6§l" + getName() + getLevel().getSuffix() + " Taxes",
                        "§fResident Tax §e" + Populace.getCurrency().format(getTax()),
                        "§fPlot Tax §e" + Populace.getCurrency().format(getPlotTax()),
                        (Populace.isPopulaceMarketLoaded() ? "§fSales Tax §e" + new DecimalFormat("#.#").format(getSalesTax()) + "%" : "§fSales Tax §8--%"),
                        "§a",
                        "§6§lUpkeep",
                        "§fDaily" + getLevel().getSuffix() + " Upkeep " + (getBank()>=getDailyUpkeep()?"§a":"§c") + Populace.getCurrency().format(getDailyUpkeep()),
                        "§fLevel Multiplier §e" + getLevel().getUpkeepModifier()
                ));
                if (System.currentTimeMillis() < getGracePeriodExpiration()){
                    list.add("§fGrace Period §a" + ClockUtil.formatDateDiff(getGracePeriodExpiration(), true));
                    list.add("§7Town will not be charged a daily upkeep");
                    list.add("§7until the grace period expires.");
                }
                else{
                    list.add("§fNext Upkeep Collection §e" + Populace.getNewDayCountdown());
                }

                if (rank.getPermissionLevel() > TownRank.GUEST.getPermissionLevel() && rank.getPermissionLevel() < TownRank.MAYOR.getPermissionLevel()) {
                    list.add("§a");
                    list.add("§6§lYour Taxes");
                    list.add("§7If you cannot afford your taxes, you will be");
                    list.add("§7kicked from " + getName() + getLevel().getSuffix() + ".");
                    list.add("§a");
                    list.add("§fYour Daily Tax §e" + Populace.getCurrency().format(resident.getDailyTax()));
                    list.add("§fNext Tax Collection §e" + Populace.getNewDayCountdown());
                }
                else if (rank==TownRank.MAYOR){
                    list.add("§a");
                    list.add("§aLeft Click§f to change taxes.");
                    list.add("§aRight Click§f to deposit or withdraw from the bank.");
                }

                return new ItemBuilder(Material.GOLD_INGOT)
                        .withCustomName("§b§lBank Balance: §f§l" + Populace.getCurrency().format(getBank()))
                        .withLore(list).build();
            case TOURISM:

                if (getResidents().size() >= TownLevel.HAMLET.getResidents()) {
                    list = new ArrayList<>();
                    list.add("§7These policies control who can warp to");
                    list.add("§7" + getName() + getLevel().getSuffix() + ", and what they can do.");
                    list.add("§a");
                    for (PermissionSet set : PermissionSet.values()) {
                        if (!set.isApplicableTo(PermissionSet.PermissionScope.TOWN)) continue;
                        list.add("§fWho can " + set.getLoredescription() + "? " + getRequiredRank(set).getPrefix());
                    }
                    if (rank == TownRank.MAYOR) {
                        list.add("§a");
                        list.add("§aLeft Click§f to change town permissions.");
                        list.add("§aRight Click§f to update town spawn.");
                    }

                    return new ItemBuilder(Material.EYE_OF_ENDER)
                            .withCustomName("§b§lVisitor Policies")
                            .withLore(list).build();
                } else {
                    TownLevel req = TownLevel.HAMLET;
                    list = new ArrayList<>();
                    list.add("§a");
                    list.add("§7Editing town permissions");
                    list.add("§7will unlock once " + getName() + getLevel().getSuffix());
                    list.add("§7becomes a " + req.getName() + " (" + req.getResidents() + " residents).");

                    return new ItemBuilder(Material.EYE_OF_ENDER)
                            .withCustomName("§c§lVisitor Policies")
                            .withLore(list).build();
                }
            case MSGBOARD:

                if (getResidents().size() >= TownLevel.VILLAGE.getResidents()) {
                    list = new ArrayList<>();
                    List<String> toDisplay = new ArrayList<>();
                    for (TownAnnouncement announcement : getAnnouncements()) {
                        if (toDisplay.size() > 5) {
                            break;
                        }
                        if (getRank(resident).isAtLeast(announcement.getRequiredRank())) {
                            toDisplay.add("§8 - §f" + announcement.getTitle());
                        }
                    }
                    list.add("§a");
                    if (toDisplay.size() > 0) {
                        list.add("§eRecent Posts...");
                        list.addAll(toDisplay);
                    } else {
                        list.add("§c§oNo items to display!");
                    }
                    list.add("§a");
                    list.add("§aLeft Click§f to view town board.");

                    return new ItemBuilder(Material.BOOK)
                            .withCustomName("§b§lMessage Board")
                            .withLore(list).build();
                } else {
                    TownLevel req = TownLevel.VILLAGE;
                    list = new ArrayList<>();
                    list.add("§a");
                    list.add("§7The town message board");
                    list.add("§7will unlock once " + getName() + getLevel().getSuffix());
                    list.add("§7becomes a " + req.getName() + " (" + req.getResidents() + " residents).");

                    return new ItemBuilder(Material.BOOK)
                            .withCustomName("§c§lMessage Board")
                            .withLore(list).build();
                }

        }

        return null;
    }

    public TownLevel getNextLevel(){
        if (getLevel().isHighest()){
            return null;
        }

        for(TownLevel level : TownLevel.values()){
            if (level.getResidents() > getLevel().getResidents()) {
                return level;
            }
        }
        return null;
    }

    public void sendTownBroadcast(TownRank rank, String message){
        TownBroadcastEvent event = new TownBroadcastEvent(this, rank, message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        rank = event.getRank();
        message = event.getMessage();
        for(Player player : Bukkit.getOnlinePlayers()){
            Resident resident = ResidentManager.getResident(player);
            if (resident == null)continue;
            if (resident.getTown() != null && resident.getTown().getUuid().equals(getUuid())){
                TownRank r = getRank(resident);
                if (r.getPermissionLevel() >= rank.getPermissionLevel()){
                    player.sendMessage("§d§l[" + getName() + "]§d " + message);
                }
            }
        }
    }

    public boolean isVulnerableToDestruction() {
        return Configuration.DESTRUCTIVE_UNCLAIM && System.currentTimeMillis() - getFounded() > TimeUnit.HOURS.toMillis(1);
    }

    public void destroy(boolean force){

        if (!force){

            if (getResidents().size() > 1){
                getMayor().sendMessage(Msg.ERR + "Destroy failed. You must kick all residents first.");
                return;
            }

        }

        TownDestructionEvent event = new TownDestructionEvent(this, force);
        Bukkit.getPluginManager().callEvent(event);

        if(getBank() > 0 && getMayor() != null){
            Account account = AccountManager.getAccount(getMayor().getUuid());
            if (account != null){
                account.deposit(Populace.getCurrency(), getBank());
            }
            setBank(0);
        }

        for(Resident resident : getResidents().keySet()){
            kickOut(resident, null, "Town has been destroyed.");
        }

        for (Plot plot : getPlots()) {
            if (isVulnerableToDestruction()) {
                if (plot.getPlotChunk().getWorld().isChunkLoaded(plot.getPlotChunk().getX(), plot.getPlotChunk().getZ())) {
                    RuinManager.blowup(plot.getPlotChunk(), Configuration.DESTRUCTIVE_UNCLAIM_EXPLOSIONS);
                } else {
                    RuinManager.markRuined(plot.getPlotChunk());
                }
            }
            Bukkit.getPluginManager().callEvent(new TownUnclaimLandEvent(this, null, plot));
        }

        getPlots().clear();
        TownManager.getTowns().remove(this);

    }

    public void toggleStatus(){
        if (isOpen()){
            setOpen(false);
            sendTownBroadcast(TownRank.RESIDENT, "Town is no longer publicly joinable.");
        }
        else{
            setOpen(true);
            sendTownBroadcast(TownRank.RESIDENT, "Town is now publicly joinable.");
        }
    }

    private boolean joinTown(Resident resident) {
        getResidents().put(resident, TownRank.RESIDENT);
        resident.setTown(this);
        ResidentJoinTownEvent event = new ResidentJoinTownEvent(this, resident, null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            getResidents().remove(resident);
            resident.setTown(null);
            return false;
        }
        resident.sendMessage(Msg.OK + "Welcome to " + getName() + getLevel().getSuffix() + "!");
        if (getNextLevel() != null && getResidents().size() >= getNextLevel().getResidents()) {
            TownLevel oldLevel = getLevel();
            TownLevel newLevel = TownLevel.getAppropriateLevel(getResidents().size());
            setLevel(newLevel);
            DecimalFormat df = new DecimalFormat("#.#");
            sendTownBroadcast(TownRank.RESIDENT, getName() + " has leveled up to §f" + newLevel.getName() + "§d!");
            sendTownBroadcast(TownRank.RESIDENT, "Maximum Land: §f" + oldLevel.getMaxland() + " > " + newLevel.getMaxland());
            if (oldLevel.getUpkeepModifier() != newLevel.getUpkeepModifier()) {
                sendTownBroadcast(TownRank.RESIDENT, "Upkeep Multiplier: §f" + df.format(oldLevel.getUpkeepModifier()) + "x > " + df.format(newLevel.getUpkeepModifier()) + "x");
            }
            switch (newLevel) {
                case HAMLET:
                    sendTownBroadcast(TownRank.RESIDENT, "§aUNLOCKED:§d \"Visitor Policies\"! Click the Eye of Ender in your §f/town§d menu.");
                    break;
                case VILLAGE:
                    sendTownBroadcast(TownRank.RESIDENT, "§aUNLOCKED:§d \"Message Board\"! Use §f/board§d to see the new feature.");
                    break;
                case CITY:
                    sendTownBroadcast(TownRank.RESIDENT, "§aUNLOCKED:§d \"Outpost Claims\"! Land not adjacent to town land can now be claimed.");
                    break;
            }
        }
        if (resident.getDailyTax() > 0) {
            resident.sendMessage(Msg.OK + "To live here, there is a daily tax of " + Populace.getCurrency().format(resident.getDailyTax()) + ".");
            resident.sendMessage(Msg.OK + "If you cannot afford the tax, you will be kicked from the town.");
            resident.sendMessage(Msg.OK + "The next collection time is in " + Populace.getNewDayCountdown() + ".");
        }
        return true;
    }

    // Called when a user is removed from a town
    private void leaveTown(Resident resident, ResidentKickedFromTownEvent kickEvent) {
        if (resident.getTown() != null && resident.getTown().getUuid().equals(getUuid())){

            ResidentLeaveTownEvent event = new ResidentLeaveTownEvent(this, resident, kickEvent);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            getResidents().remove(resident); // Removes them from the town directory
            resident.setTown(null); // Updates user data
            getPlots().stream().filter(plot -> plot.getOwner() != null && plot.getOwner().getUuid().equals(resident.getUuid())).forEach(plot -> {
                // Removes them as the owner of any plots, and clears the allow list
                plot.setOwner(null);
                plot.getAllowList().clear();
            });
            resident.getPlotChunks().clear();

            if (resident.isJailed()) {
                resident.setJailData(null);
                if (getSpawn() != null) {
                    Player pl = Bukkit.getPlayer(resident.getUuid());
                    if (pl != null && pl.isOnline()) {
                        pl.teleport(getSpawn());
                    }
                }
            }

            // If this change effected the town level, downgrade the town
            if (getResidents().size() < getLevel().getResidents()){
                TownLevel newLevel = TownLevel.getAppropriateLevel(getResidents().size());
                setLevel(newLevel);
                sendTownBroadcast(TownRank.RESIDENT, getName() + " has de-leveled to §f" + newLevel.getName() + "§d!");
                sendTownBroadcast(TownRank.RESIDENT, "Further land expansion is now restricted.");
            }
        }
    }

    public boolean isInvited(Resident resident){
        return resident.getTownInvites().containsKey(getUuid());
    }

    public void leaveOnBehalf(Resident resident){
        if (resident.getTown() != null && resident.getTown().getUuid().equals(getUuid())){
            sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " left on their own accord.");
            leaveTown(resident, null);
        }
        else{
            resident.sendMessage(Msg.ERR + "You're not part of " + getName() + getLevel().getSuffix() + ".");
        }
    }

    public void joinByPublic(Resident resident){
        if (resident.getTown() == null){
            if (isOpen()){

                if (getPlots().size() > 0){

                    if (joinTown(resident)) {
                        sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " joined " + getName() + getLevel().getSuffix() + "!");
                    }

                }
                else{
                    resident.sendMessage(Msg.ERR + getName() + getLevel().getSuffix() + " has no land yet, so it's not joinable right now.");
                    sendTownBroadcast(TownRank.MANAGER, resident.getName() + " tried to join, but your town has no land claims yet. Ask them to try again once you have at least 1 claim.");
                }

            }
            else{
                resident.sendMessage(Msg.ERR + getName() + getLevel().getSuffix() + " is not joinable by the general public. Ask a manager to be invited.");
            }
        }
        else{
            resident.sendMessage(Msg.ERR + "You need to leave " + resident.getTown().getName() + resident.getTown().getLevel().getSuffix() + " before joining another town.");
        }
    }

    public void joinByInvite(Resident resident){
        if (resident.getTown() == null){

            if (resident.getTownInvites().containsKey(getUuid())){

                if (getPlots().size() > 0) {


                    if (joinTown(resident)) {
                        sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " joined " + getName() + getLevel().getSuffix() + " on behalf of " + resident.getTownInvites().get(getUuid()).getName() + "'s invitation!");
                        resident.getTownInvites().remove(getUuid());
                    }

                }
                else{
                    resident.sendMessage(Msg.ERR + getName() + getLevel().getSuffix() + " has no land yet, so it's not joinable right now.");
                    sendTownBroadcast(TownRank.MANAGER, resident.getName() + " tried to join (by invitation), but your town has no land claims yet. Ask them to try again once you have at least 1 claim.");
                }

            }
            else{
                resident.sendMessage(Msg.ERR + "You're not invited to " + getName() + getLevel().getSuffix() + "! Ask a Manager to be invited.");
            }

        }
        else{
            resident.sendMessage(Msg.ERR + "You need to leave " + resident.getTown().getName() + resident.getTown().getLevel().getSuffix() + " before joining another town.");
        }
    }

    public void kickOut(Resident resident, Resident kicker, String reason){

        if (resident.getTown() == null || !resident.getTown().getUuid().equals(getUuid())){
            return;
        }


        if (kicker != null){
            TownRank rank = getRank(resident);
            if (rank.getPermissionLevel() < getRank(kicker).getPermissionLevel()){
                if (getRank(kicker).getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()) {
                    ResidentKickedFromTownEvent event = new ResidentKickedFromTownEvent(this, resident, kicker);
                    Bukkit.getPluginManager().callEvent(event);
                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " was kicked by " + kicker.getName() + (reason!=null?" for " + reason + "!":"!"));
                    leaveTown(resident, event);
                }
                else{
                    kicker.sendMessage(Msg.ERR + "Only Managers can kick out residents.");
                }
            }
            else{
                kicker.sendMessage(Msg.ERR + resident.getName() + " can't be kicked.");
            }
        }
        else{
            ResidentKickedFromTownEvent event = new ResidentKickedFromTownEvent(this, resident, null);
            Bukkit.getPluginManager().callEvent(event);
            sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " was kicked" + (reason!=null?" for " + reason + "!":"!"));

            leaveTown(resident, event);
        }

    }

    public TownRank getRank(Resident resident){
        if (residents.containsKey(resident)){
            return residents.get(resident);
        }
        return TownRank.GUEST;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getBonusLand() {
        return bonusLand;
    }

    public void setBonusLand(long bonusLand) {
        this.bonusLand = bonusLand;
    }

    public String getName() {
        return name;
    }

    public TownLevel getLevel() {
        return level;
    }

    public void setLevel(TownLevel level) {
        this.level = level;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public double getBank() {
        return bank;
    }

    public void setBank(double bank) {
        this.bank = bank;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getPlotTax() {
        return plotTax;
    }

    public void setPlotTax(double plotTax) {
        this.plotTax = plotTax;
    }

    public double getSalesTax() {
        return salesTax;
    }

    public void setSalesTax(double salesTax) {
        this.salesTax = salesTax;
    }

    public List<Plot> getPlots() {
        return plots;
    }

    @SuppressWarnings("unchecked")
    public List<Resident> getSortedResidents() {
        List<Resident> r = getResidents().keySet().stream().collect(Collectors.toList());
        Collections.sort(r);
        return r;
    }

    public Map<Resident, TownRank> getResidents() {
        return residents;
    }

    @Override
    public int compareTo(Object o) {

        if ((o != null)){

            if ((o instanceof Town)){
                Town t = (Town)o;
                if (t.getResidents().size() > getResidents().size()){
                    return 1;
                }
                else if (t.getResidents().size() < getResidents().size()){
                    return -1;
                }
            }

        }

        return 0;
    }
}
