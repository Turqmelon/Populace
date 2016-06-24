package com.turqmelon.Populace.Resident;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Plot.PlotType;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Town.*;
import com.turqmelon.Populace.Utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
public class Resident implements Comparable {

    private UUID uuid;
    private String name;

    private long joined = 0;
    private long seen = 0;

    private Town town = null;
    private List<PlotChunk> plotChunks = new ArrayList<>();

    private Map<UUID, Resident> townInvites = new HashMap<>();

    private Runnable pendingAction = null;
    private DisplayEntity displayEntity = null;
    private long lastBuildWarning = 0;
    private long fallImmunity = 0;

    private boolean bypassMode = false;

    private JailData jailData = null;
    private String prefix = null;

    public Resident(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.displayEntity = new DisplayEntity();
    }

    public Resident(JSONObject object){
        this.uuid = UUID.fromString((String) object.get("uuid"));
        this.name = (String) object.get("name");
        this.joined = (long) object.get("joined");
        this.seen = (long) object.get("seen");
        String townid = (String) object.get("town");
        if (townid != null){
            this.town = TownManager.getTown(UUID.fromString(townid));
        }
        this.displayEntity = new DisplayEntity();
        Object jaildata = object.get("jaildata");
        if (jaildata != null) {
            this.jailData = new JailData((JSONObject) jaildata);
        }
        this.prefix = (String) object.getOrDefault("prefix", null);
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        object.put("uuid", getUuid().toString());
        object.put("name", getName());
        object.put("joined", getJoined());
        object.put("seen", getSeen());
        object.put("town", getTown()!=null?getTown().getUuid().toString():null);
        object.put("jaildata", getJailData() != null ? getJailData().toJSON() : null);
        object.put("prefix", getPrefix());
        return object;
    }

    public boolean isBypassMode() {
        return bypassMode;
    }

    public void setBypassMode(boolean bypassMode) {
        this.bypassMode = bypassMode;
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public boolean hasFallImmunity(){
        return System.currentTimeMillis() - getFallImmunity() < TimeUnit.SECONDS.toMillis(10);
    }

    public long getFallImmunity() {
        return fallImmunity;
    }

    public void setFallImmunity(long fallImmunity) {
        this.fallImmunity = fallImmunity;
    }

    public void warnForBuilding(){
        if (System.currentTimeMillis() - lastBuildWarning < TimeUnit.MINUTES.toMillis(20)){
            return;
        }
        this.lastBuildWarning = System.currentTimeMillis();
        sendMessage(Msg.WARN + "You're building in the Wilderness!");
        sendMessage(Msg.WARN + "Things in the wilderness aren't protected. Join (or create!) a town for protection.");
    }

    public ItemStack getIcon(){
        Player pl = Bukkit.getPlayer(getUuid());
        Account account = AccountManager.getAccount(getUuid());
        return new ItemBuilder(Material.SKULL_ITEM).withData((byte)3).asHeadOwner(getName())
                .withCustomName("§a" + (getPrefix() != null ? getPrefix() + " " : (getTown() != null ? getTown().getRank(this).getPrefix() : "")) + getName())
                .withLore(Arrays.asList(
                        "§fJoined §e" + ClockUtil.formatDateDiff(getJoined(), true) + " ago",
                        "§fSeen §e" + (pl!=null&&pl.isOnline()?"§aOnline":ClockUtil.formatDateDiff(getSeen(), true) + " ago"),
                        "§fTown §e" + (getTown()!=null?getTown().getName() + " (" + getTown().getRank(this).getName() + ")":"§cNone"),
                        "§fPlots Owned §e" + getPlotChunks().size(),
                        "§fBalance §e" + (account!=null?Populace.getCurrency().format(account.getBalance(Populace.getCurrency())):"§c???")
                )).build();
    }

    public String getUpdateMessage(Town town, Plot plot){

        Town lastTown = getDisplayEntity().getTown();
        Plot lastPlot = getDisplayEntity().getPlot();

        String result = "";

        if (town == null && lastTown != null){
            result = "§2§lWilderness" + (Configuration.WILDERNESS_PVP?" §4§l(PVP)":"");
        }
        else if (town != null && (lastTown == null || !lastTown.getUuid().equals(town.getUuid()))){
            if ((town instanceof Warzone)) {
                result = "§c§lWarzone";
            } else if (town instanceof Spawn) {
                result = "§a§lSpawn";
            } else {
                result = "§6§l" + town.getName() + town.getLevel().getSuffix();
            }
        }

        if (plot != null && (lastPlot == null || !lastPlot.getUuid().equals(plot.getUuid()))){
            String owner;
            if (plot.getOwner() != null){
                owner = plot.getOwner().getName();
            }
            else{
                owner = plot.getPlotChunk().getX() +", " + plot.getPlotChunk().getZ();
            }
            ChatColor color;
            switch(plot.getType()){
                case MERCHANT:
                    color = ChatColor.AQUA;
                    break;
                case BATTLE:
                    color = ChatColor.RED;
                    break;
                case RESIDENTIAL:
                    color = ChatColor.GREEN;
                    break;
                default:
                    color = ChatColor.GRAY;
                    break;
            }

            owner = color + "§l" + owner;



            if (plot.getType() == PlotType.BATTLE){
                owner += " §4§l(PVP)";
            }

            if (plot.isForSale()){
                owner += " §8§l:: §e§lBUYABLE: " + Populace.getCurrency().format(plot.getPrice());
            }

            if (result.length() > 0){
                result += " §8:: ";
            }
            result+=owner;
        }

        getDisplayEntity().setTown(town);
        getDisplayEntity().setPlot(plot);
        return result;

    }

    public DisplayEntity getDisplayEntity() {
        return displayEntity;
    }

    // The daily amount of money paid to towns
    public double getDailyTax(){

        if (getTown() != null){

            if (getTown().getRank(this) == TownRank.MAYOR){
                return 0;
            }

            double base = getTown().getTax();
            double plotTax = 0;
            for(Plot plot : getPlots()){
                if (plot.getTown() != null && plot.getTown().getUuid().equals(getTown().getUuid())){
                    plotTax = plotTax + getTown().getPlotTax();
                }
            }

            return base + plotTax;
        }

        return 0;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getSeen() {
        return seen;
    }

    public void setSeen(long seen) {
        this.seen = seen;
    }

    public long getJoined() {
        return joined;
    }

    public void setJoined(long joined) {
        this.joined = joined;
    }

    public List<Plot> getPlots(){
        List<Plot> plots = new ArrayList<>();
        for(PlotChunk chunk : getPlotChunks()){
            Plot plot = PlotManager.getPlot(chunk);
            if (plot != null){
                plots.add(plot);
            }
        }
        return plots;
    }

    public void sendMessage(String message){
        Player player = Bukkit.getPlayer(getUuid());
        if (player != null)player.sendMessage(message);
    }

    public List<PlotChunk> getPlotChunks() {
        return plotChunks;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public Map<UUID, Resident> getTownInvites() {
        return townInvites;
    }

    public long getLastBuildWarning() {
        return lastBuildWarning;
    }

    public boolean isJailed() {

        JailData data = getJailData();
        if (data != null) {
            if (data.getExpiration() != -1 && System.currentTimeMillis() > data.getExpiration()) {
                setJailData(null);

                if (getTown() != null) {
                    getTown().sendTownBroadcast(TownRank.RESIDENT, getName() + " is now free from jail.");
                    if (getTown().getSpawn() != null) {
                        Player pl = Bukkit.getPlayer(getUuid());
                        if (pl != null && pl.isOnline()) {
                            pl.teleport(getTown().getSpawn());
                        }
                    }
                } else {
                    sendMessage(Msg.OK + "You're now free from jail. Teleporting abilities have been restored.");
                }
            }
        }

        return getJailData() != null;
    }

    public JailData getJailData() {
        return jailData;
    }

    public void setJailData(JailData jailData) {
        this.jailData = jailData;
    }

    public Runnable getPendingAction() {
        return pendingAction;
    }

    public void setPendingAction(Runnable pendingAction) {
        if (pendingAction != null){
            sendMessage(Msg.INFO + "Please type §f/confirm§b to confirm this.");
        }
        this.pendingAction = pendingAction;
    }

    @Override
    public int compareTo(Object o) {

        if (o == null || o.getClass() != getClass()){
            return 0;
        }

        Resident resident = (Resident)o;

        if (getTown() == null || resident.getTown() == null){
            return 0;
        }

        if (getTown().getUuid().equals(resident.getTown().getUuid())){

            TownRank rank1 = getTown().getRank(this);
            TownRank rank2 = resident.getTown().getRank(resident);

            if (rank1.getPermissionLevel() > rank2.getPermissionLevel()){
                return -1;
            }
            else if (rank1.getPermissionLevel() < rank2.getPermissionLevel()){
                return 1;
            }

        }

        return 0;
    }
}
