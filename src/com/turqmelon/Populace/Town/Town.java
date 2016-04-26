package com.turqmelon.Populace.Town;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Plot.*;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Utils.ClockUtil;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_8_R3.NBTTagString;
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

    private long founded = 0;

    private long bonusLand = 0;

    private List<Plot> plots = new ArrayList<>();
    private Map<Resident, TownRank> residents = new HashMap<>();
    private Map<PermissionSet, TownRank> permissions = new HashMap<>();

    private MapView mv = null;
    private int mvid = -1;

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

        this.founded = (long) object.get("founded");
        this.bonusLand = (long) object.get("bonusland");

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

        JSONArray plots = (JSONArray)object.get("plots");
        for(Object o : plots){
            JSONObject obj = (JSONObject)o;
            Plot plot = new Plot(obj, this);
            getPlots().add(plot);
            if (plot.getOwner() != null){
                plot.getOwner().getPlotChunks().add(plot.getPlotChunk());
            }
        }

        this.level = TownLevel.getAppropriateLevel(getResidents().size());
        initializeMapView();
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

        return object;
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

    public int getUsedBonusBlocks() {
        int claimed = getPlots().size();
        int levelLimit = getLevel().getMaxland();
        return claimed-levelLimit;
    }

    public boolean sellBonusBlocks(int amount){
        long newAmount = getBonusLand()-amount;
        if (newAmount >= getUsedBonusBlocks()){
            if (newAmount >= 0){
                setBonusLand(newAmount);
                return true;
            }
        }
        return false;
    }

    public boolean buyBonusBlocks(int amount){

        long newAmount = getBonusLand()+amount;
        if (newAmount <= getLevel().getMaxland()){
            setBonusLand(newAmount);

            return true;
        }

        return false;
    }

    public long getGracePeriodExpiration() {
        return getFounded()+ TimeUnit.HOURS.toMillis(Configuration.TOWN_GRACE_PERIOD_HOURS);
    }

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

        return cost;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

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

    public boolean claimLand(Resident resident, PlotChunk chunk){
        return claimLand(resident, chunk, false);
    }

    private boolean buyChunk(Resident resident, PlotChunk chunk, boolean outpost){
        double price = outpost ? 100 : 50;
        if (getBank() >= price){
            setBank(getBank()-price);
            sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " purchased " + (outpost?"an outpost":"a chunk") + " for the town.");
            getPlots().add(new Plot(UUID.randomUUID(), chunk, outpost?PlotType.OUTPOST:PlotType.RESIDENTIAL, this));

            // For the first plot, set a town spawn so it can be warped back to
            if (getPlots().size() == 1){
                Player player = Bukkit.getPlayer(resident.getUuid());
                if (player != null){
                    setSpawn(player.getLocation());
                }
                initializeMapView();

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
        }
        getMapView().addRenderer(new TownMapRenderer(this));
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
            for(int x = -1; x <= 1; x++){
                for(int z = -1; z <= 1; z++){
                    PlotChunk chunk = new PlotChunk(plot.getPlotChunk().getWorld(), plot.getPlotChunk().getX()+x, plot.getPlotChunk().getZ()+z);
                    Plot p = PlotManager.getPlot(chunk);
                    if (p != null && p.getTown() != null && p.getTown().getUuid().equals(getUuid())){
                        adjacent++;
                    }
                }
            }

            // The owner asking to unclaim their plot won't really unclaim it, just relinquish ownership
            if (plot.getOwner() != null && plot.getOwner().getUuid().equals(resident.getUuid())){

                if (confirmed){
                    plot.getOwner().getPlotChunks().remove(plot.getPlotChunk());
                    plot.setOwner(null);
                    plot.getAllowList().clear();
                    resident.sendMessage(Msg.OK + "You gave up ownership of this plot.");
                    sendTownBroadcast(TownRank.ASSISTANT, resident.getName() + " gave up ownership of a plot in " + plot.getPlotChunk().getWorld().getName() + " @ X: " + plot.getPlotChunk().getX() +", Z: " + plot.getPlotChunk().getZ());
                }
                else{
                    resident.sendMessage(Msg.INFO + "Are you sure you want to give up ownership of this plot?");
                    resident.sendMessage(Msg.INFO + "You'll lose access to everything in it" + (getPlotTax()>0?" and you won't be taxed for it anymore.":"."));
                    resident.setPendingAction(() -> unclaimLand(resident, plot, true));
                }
                return true;

            }
            else if (plot.getOwner() != null && rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()){

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
                    plot.getOwner().getPlotChunks().remove(plot.getPlotChunk());
                    getPlots().remove(plot);
                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " unclaimed an owned plot in " + plot.getPlotChunk().getWorld().getName() + " @ X: " + plot.getPlotChunk().getX() +", Z: " + plot.getPlotChunk().getZ());
                }
                else{
                    resident.sendMessage(Msg.INFO + "Unclaiming this plot will cause it to become Wilderness.");
                    resident.setPendingAction(() -> unclaimLand(resident, plot, true));
                }
                return true;
            }
            else if (rank.getPermissionLevel() >= TownRank.ASSISTANT.getPermissionLevel()){

                if (adjacent == 4){
                    resident.sendMessage(Msg.ERR + "You can't use that in the middle of town. Unclaiming must be done from the borders.");
                    return false;
                }

                if (getPlots().size() == 1){
                    resident.sendMessage(Msg.ERR + "Towns must have at least 1 claim.");
                    resident.sendMessage(Msg.ERR + "If you made a mistake, destroy your town and start over.");
                    return false;
                }

                getPlots().remove(plot);
                sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " unclaimed a plot in " + plot.getPlotChunk().getWorld().getName() + " @ X: " + plot.getPlotChunk().getX() +", Z: " + plot.getPlotChunk().getZ());
                return true;
            }
            else{
                resident.sendMessage(Msg.ERR + "You can't do that here.");
            }

        }
        else{
            resident.sendMessage(Msg.ERR + "There's no plot here!");
        }
        return false;
    }

    private boolean claimLand(Resident resident, PlotChunk chunk, boolean confirmed){
        TownRank rank = getRank(resident);
        if (rank.getPermissionLevel() >= TownRank.ASSISTANT.getPermissionLevel()){

            ClaimFailureReason reason = canClaimLand(chunk);
            switch(reason){
                case ALL_LAND_USED:
                    resident.sendMessage(Msg.ERR + "Town can't claim anymore land at it's current size.");
                    if (rank == TownRank.MAYOR){
                        resident.sendMessage(Msg.ERR + "Consider buying bonus land from your §f/town§c menu or inviting more residents.");
                    }
                    else{
                        resident.sendMessage(Msg.ERR + "Consider asking the Mayor to buy bonus land.");
                    }
                    return false;
                case ALREADY_CLAIMED:

                    Plot plot = PlotManager.getPlot(chunk);
                    if (plot.getTown().getUuid().equals(getUuid()) && (plot.getInvited().contains(resident.getUuid())||plot.isForSale())){
                        double price = plot.getPrice();
                        if (confirmed){
                            Account account = AccountManager.getAccount(resident.getUuid());
                            if (price == 0 || (account != null && account.withdraw(Populace.getCurrency(), price))){
                                setBank(getBank()+price);
                                resident.getPlotChunks().add(chunk);
                                plot.setOwner(resident);
                                plot.setPrice(0);
                                plot.setForSale(false);
                                plot.getInvited().clear();
                                if (price > 0){
                                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " bought a plot for " + Populace.getCurrency().format(price) + "!");
                                }
                                else{
                                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " accepted an invitation to claim a plot!");
                                }
                            }
                            else{
                                resident.sendMessage(Msg.ERR + "You don't have enough " + Populace.getCurrency().getPlural() + "!");
                            }
                        }
                        else{
                            if (price > 0){
                                resident.sendMessage(Msg.INFO + "Purchase this plot from the town for " + Populace.getCurrency().format(price) + "?");
                                resident.setPendingAction(() -> claimLand(resident, chunk, true));
                            }
                            else{
                                resident.sendMessage(Msg.INFO + "Claim this plot as yours?");
                                resident.setPendingAction(() -> claimLand(resident, chunk, true));
                            }

                        }
                        return true;
                    }else{
                        resident.sendMessage(Msg.ERR + "That land is already claimed and not for sale.");
                    }
                    return false;
                case TOO_CLOSE_TO_TOWN:
                    resident.sendMessage(Msg.ERR + "You can't place your first claim here, as it's within 240 blocks of another town's borders.");
                    return false;
                case REQUIRE_OUTPOST:

                    if (getLevel().getResidents() >= TownLevel.CITY.getResidents()){
                        if (confirmed){
                            return buyChunk(resident, chunk, true);
                        }
                        else{
                            resident.sendMessage(Msg.INFO + "This land is not adjacent to any existing town claim, so it must be an outpost.");
                            resident.sendMessage(Msg.INFO + "Outposts have a higher daily upkeep than normal claims.");
                            resident.setPendingAction(() -> claimLand(resident, chunk, true));
                        }
                        return true;
                    }
                    else{
                        resident.sendMessage(Msg.ERR + "To claim an outpost, your town must be a City. (" + TownLevel.CITY.getResidents() + " Residents)");
                        resident.sendMessage(Msg.ERR + "Land claims must be adjacent to existing claims to not be considered outposts.");
                    }

                    return false;
                default:
                    return buyChunk(resident, chunk, false);

            }

        }
        else{
            resident.sendMessage(Msg.ERR + "Only town assistants can claim land.");
        }
        return false;
    }

    private ClaimFailureReason canClaimLand(PlotChunk chunk){


        Plot p = PlotManager.getPlot(chunk);
        if (p != null){
            return ClaimFailureReason.ALREADY_CLAIMED;
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

            for(int x = -15; x <= 15; x++){
                for(int z = -15; z <= 15; z++){
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
                        "§fSales Tax §e" + new DecimalFormat("#.#").format(getSalesTax())  + "%",
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

                if (rank.getPermissionLevel() >= TownRank.GUEST.getPermissionLevel() && rank.getPermissionLevel()<TownRank.MAYOR.getPermissionLevel()){
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

                list = new ArrayList<>();
                list.add("§7These policies control who can warp to");
                list.add("§7" + getName() + getLevel().getSuffix() + ", and what they can do.");
                list.add("§a");
                for(PermissionSet set : PermissionSet.values()){
                    if (!set.isApplicableTo(PermissionSet.PermissionScope.TOWN))continue;
                    list.add("§fWho can " + set.getLoredescription() + "? " + getRequiredRank(set).getPrefix());
                }
                if (rank==TownRank.MAYOR){
                    list.add("§a");
                    list.add("§aLeft Click§f to change town permissions.");
                    list.add("§aRight Click§f to update town spawn.");
                }

                return new ItemBuilder(Material.EYE_OF_ENDER)
                        .withCustomName("§b§lVisitor Policies")
                        .withLore(list).build();
        }

        return null;
    }

    public TownLevel getNextLevel(){
        if (getLevel().isHighest()){
            return null;
        }

        for(TownLevel level : TownLevel.values()){
            if (level.getResidents() > getResidents().size()){
                return level;
            }
        }
        return null;
    }

    public void sendTownBroadcast(TownRank rank, String message){
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

    public void destroy(boolean force){

        if (!force){

            if (getResidents().size() > 1){
                getMayor().sendMessage(Msg.ERR + "Destroy failed. You must kick all residents first.");
                return;
            }

        }

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

    private void joinTown(Resident resident){
        if (resident.getTown() == null){
            getResidents().put(resident, TownRank.RESIDENT);
            resident.setTown(this);
            if (Populace.isPopulaceChatLoaded()) {
                TownChatBridge.firstTownJoin(resident);
            }
            if (getNextLevel() != null && getResidents().size() >= getNextLevel().getResidents()){
                TownLevel newLevel = TownLevel.getAppropriateLevel(getResidents().size());
                setLevel(newLevel);
                sendTownBroadcast(TownRank.RESIDENT, getName() + " has leveled up to §f" + newLevel.getName() + "§d!");
                sendTownBroadcast(TownRank.RESIDENT, "Maximum land has been increased for expansion.");
            }
            resident.sendMessage(Msg.OK + "Welcome to " + getName() + getLevel().getSuffix() + "!");
            if (resident.getDailyTax() > 0){
                resident.sendMessage(Msg.OK + "To live here, there is a daily tax of " + Populace.getCurrency().format(resident.getDailyTax()) + ".");
                resident.sendMessage(Msg.OK + "If you cannot afford the tax, you will be kicked from the town.");
                resident.sendMessage(Msg.OK + "The next collection time is in " + Populace.getNewDayCountdown() + ".");
            }
        }
    }

    // Called when a user is removed from a town
    private void leaveTown(Resident resident){
        if (resident.getTown() != null && resident.getTown().getUuid().equals(getUuid())){
            getResidents().remove(resident); // Removes them from the town directory
            resident.setTown(null); // Updates user data
            getPlots().stream().filter(plot -> plot.getOwner() != null && plot.getOwner().getUuid().equals(resident.getUuid())).forEach(plot -> {
                // Removes them as the owner of any plots, and clears the allow list
                plot.setOwner(null);
                plot.getAllowList().clear();
            });
            resident.getPlotChunks().clear();

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
            leaveTown(resident);
        }
        else{
            resident.sendMessage(Msg.ERR + "You're not part of " + getName() + getLevel().getSuffix() + ".");
        }
    }

    public void joinByPublic(Resident resident){
        if (resident.getTown() == null){
            if (isOpen()){

                if (getPlots().size() > 0){
                    joinTown(resident);
                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " joined "  + getName() + getLevel().getSuffix() + "!");

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
                    joinTown(resident);
                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " joined " + getName() + getLevel().getSuffix() + " on behalf of " + resident.getTownInvites().get(getUuid()).getName() + "'s invitation!");
                    resident.getTownInvites().remove(getUuid());

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

        TownRank rank = getRank(resident);

        if (kicker != null){
            if (rank.getPermissionLevel() < getRank(kicker).getPermissionLevel()){
                if (rank.getPermissionLevel() >= TownRank.MANAGER.getPermissionLevel()){
                    sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " was kicked by " + kicker.getName() + (reason!=null?" for " + reason + "!":"!"));
                    leaveTown(resident);
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
            sendTownBroadcast(TownRank.RESIDENT, resident.getName() + " was kicked" + (reason!=null?" for " + reason + "!":"!"));
            leaveTown(resident);
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


    public boolean isOpen() {
        return open;
    }

    public double getBank() {
        return bank;
    }

    public double getTax() {
        return tax;
    }

    public double getPlotTax() {
        return plotTax;
    }

    public double getSalesTax() {
        return salesTax;
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

    public void setLevel(TownLevel level) {
        this.level = level;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setBank(double bank) {
        this.bank = bank;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setPlotTax(double plotTax) {
        this.plotTax = plotTax;
    }

    public void setSalesTax(double salesTax) {
        this.salesTax = salesTax;
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
