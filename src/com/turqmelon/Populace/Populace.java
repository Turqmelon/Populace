package com.turqmelon.Populace;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.MelonEco.utils.Currency;
import com.turqmelon.Populace.Commands.*;
import com.turqmelon.Populace.Events.Core.PopulaceNewDayEvent;
import com.turqmelon.Populace.Listeners.*;
import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Plot.RuinManager;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.*;
import com.turqmelon.Populace.Utils.ClockUtil;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.EnchantGlow;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class Populace extends JavaPlugin {

    private static Populace instance;
    private static Logger logger;
    private static Currency currency;
    private static long lastNewDay = 0;
    private static boolean fullyEnabled = false;

    private static boolean populaceChatLoaded = false;
    private static boolean populaceMarketLoaded = false;
    private static boolean populaceWarzoneLoaded = false;

    // Save all data to file
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveData() throws IOException {

        getLog().log(Level.INFO, "Saving data...");

        File dir = getInstance().getDataFolder();
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, "data.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        JSONObject data = new JSONObject();

        JSONArray residents = new JSONArray();
        for (Resident resident : ResidentManager.getResidents()) {
            residents.add(resident.toJSON());
        }

        JSONArray towns = new JSONArray();
        for (Town town : TownManager.getTowns()) {
            towns.add(town.toJSON());
        }

        JSONArray ruins = new JSONArray();
        for (PlotChunk plotChunk : RuinManager.getChunkList()) {
            ruins.add(plotChunk.toJSON());
        }

        data.put("lastnewday", getLastNewDay());
        data.put("residents", residents);
        data.put("towns", towns);
        data.put("ruins", ruins);

        FileWriter writer = new FileWriter(file);
        writer.write(data.toJSONString());
        writer.flush();
        writer.close();

        getLog().log(Level.INFO, "Saving complete.");

    }

    // Returns a friendly countdown until the next new day
    public static String getNewDayCountdown() {
        long nextDay = getLastNewDay() + TimeUnit.DAYS.toMillis(1);
        return ClockUtil.formatDateDiff(nextDay, true);
    }

    public static boolean isPopulaceWarzoneLoaded() {
        return populaceWarzoneLoaded;
    }

    public static boolean isPopulaceChatLoaded() {
        return populaceChatLoaded;
    }

    public static boolean isPopulaceMarketLoaded() {
        return populaceMarketLoaded;
    }

    public static Logger getLog() {
        return logger;
    }

    public static boolean isFullyEnabled() {
        return fullyEnabled;
    }

    public static long getLastNewDay() {
        return lastNewDay;
    }

    public static Currency getCurrency() {
        return currency;
    }

    public static Populace getInstance() {
        return instance;
    }

    // Hooks with other core Populace modules
    private void postSetup() {
        PluginManager pm = getServer().getPluginManager();
        Plugin chat = pm.getPlugin("PopulaceChat");
        Plugin market = pm.getPlugin("PopulaceMarket");
        Plugin warzone = pm.getPlugin("PopulaceWarzone");

        getLog().log(Level.INFO, "Looking for PopulaceChat...");
        if (chat != null && chat.isEnabled()) {
            getLog().log(Level.INFO, "PopulaceChat is here! Hooked!");
            populaceChatLoaded = true;
        } else {
            getLog().log(Level.WARNING, "PopulaceChat not found. Features using it won't be used.");
        }

        getLog().log(Level.INFO, "Looking for PopulaceMarket...");
        if (market != null && market.isEnabled()) {
            getLog().log(Level.INFO, "PopulaceMarket is here! Hooked!");
            populaceMarketLoaded = true;
        } else {
            getLog().log(Level.WARNING, "PopulaceMarket not found. Features using it won't be used.");
        }
        getLog().log(Level.INFO, "Looking for PopulaceWarzone...");
        if (warzone != null && warzone.isEnabled()) {
            getLog().log(Level.INFO, "PopulaceWarzone is here! Hooked!");
            populaceWarzoneLoaded = true;
        } else {
            getLog().log(Level.WARNING, "PopulaceWarzone not found. Features using it won't be used.");
        }
    }

    // Loads all data from file
    public void loadData() throws ParseException, IOException {

        getLog().log(Level.INFO, "Loading data...");

        File dir = getInstance().getDataFolder();
        if (dir.exists()){
            File file = new File(dir, "data.json");
            if (file.exists()){

                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(new FileReader(file));

                getLog().log(Level.INFO, "Loading residents...");

                JSONArray residentArray = (JSONArray) object.get("residents");
                for(Object o : residentArray){
                    JSONObject resident = (JSONObject)o;
                    ResidentManager.getResidents().add(new Resident(resident));
                }

                getLog().log(Level.INFO, "Loaded " + ResidentManager.getResidents().size() + " residents.");
                getLog().log(Level.INFO, "Loading towns...");

                JSONArray townArray = (JSONArray) object.get("towns");
                for(Object o : townArray){
                    JSONObject town = (JSONObject)o;
                    String townName = (String) town.get("name");
                    if (townName.equalsIgnoreCase("warzone") || townName.equalsIgnoreCase("spawn")) {
                        UUID uuid = UUID.fromString((String) town.get("uuid"));
                        SpecialTown specialTown;
                        if (townName.equalsIgnoreCase("warzone")) {
                            specialTown = new Warzone(uuid);
                        } else {
                            specialTown = new Spawn(uuid);
                        }
                        specialTown.loadPlots(town);
                        TownManager.getTowns().add(specialTown);
                    } else {
                        TownManager.getTowns().add(new Town(town));
                    }
                }

                JSONArray ruinArray = (JSONArray) object.getOrDefault("ruins", null);
                if (ruinArray != null) {
                    for (Object o : ruinArray) {
                        RuinManager.getChunkList().add(new PlotChunk((JSONObject) o));
                    }
                }

                getLog().log(Level.INFO, "Loaded " + TownManager.getTowns().size() + " towns.");

                if (Configuration.DESTRUCTIVE_UNCLAIM) {
                    getLog().log(Level.INFO, "Loaded " + RuinManager.getChunkList().size() + " chunks to ruin. Destruction will happen once they're loaded.");
                }

                lastNewDay = (long) object.get("lastnewday");
                getLog().log(Level.INFO, "Next new day in " + getNewDayCountdown() + "!");

            }
            else{
                getLog().log(Level.WARNING, "Populace data file doesn't exist. Nothing to load!");
            }
        }
        else{
            getLog().log(Level.WARNING, "Populace directory doesn't exist. Nothing to load!");
        }


    }

    // Runs daily, assesses taxes and runs old resident cleanup
    public void newDay() {

        Bukkit.broadcastMessage(Msg.OK + "A new day begins...");
        // Collect taxes from all residents
        for(Resident resident : ResidentManager.getResidents()){
            double upkeep = resident.getDailyTax();
            if (upkeep == 0)continue; // Players not in towns and mayors
            Account account = AccountManager.getAccount(resident.getUuid());
            if (account == null || !account.withdraw(Populace.getCurrency(), upkeep)){
                // The resident could not afford their taxes. AWAY WITH YOU!!! >=(
                resident.getTown().kickOut(resident, null, "Unable to afford taxes.");
            }
            else{
                // Good citizens pay their taxes. We need to be polite and thank them for their continued servitude.

                double reserveDeposit = upkeep * 0.1;
                double bankDeposit = upkeep - reserveDeposit;

                Town town = resident.getTown();
                town.setBank(town.getBank() + bankDeposit);
                town.creditReserve(reserveDeposit);

                resident.sendMessage(Msg.OK + Populace.getCurrency().format(upkeep) + " was deducted for your daily taxes. Thank you!");
            }

            // If the resident can't afford the upkeep tomorrow, warn them
            if (account != null && account.getBalance(Populace.getCurrency()) < upkeep){
                resident.sendMessage(Msg.WARN + "You currently do not have enough " + Populace.getCurrency().getPlural() + " to pay your taxes tomorrow.");
                resident.sendMessage(Msg.WARN + "Make sure you collect more (or unclaim plots), otherwise you'll be kicked from the town.");
            }
        }

        // Collect daily upkeep from towns
        for (Town town : TownManager.getTowns(false)) {
            if (System.currentTimeMillis() < town.getGracePeriodExpiration()){
                town.sendTownBroadcast(TownRank.RESIDENT, "Town was not charged daily upkeep since the grace period is still active.");
                continue;
            }
            double upkeep = town.getDailyUpkeep();
            double bank = town.getBankAndReserve();

            if (bank < upkeep){
                // The town can't afford their daily upkeep. Destroy it.
                Bukkit.broadcastMessage(Msg.WARN + town.getName() + " has fallen to ruin.");
                town.destroy(true);
            }
            else{
                // Take the daily upkeep from the bank. Their safe for another day.
                town.sendTownBroadcast(TownRank.RESIDENT, "The daily town upkeep has been collected (" + getCurrency().format(upkeep) + ")!");
                if (town.getBank() >= upkeep) {
                    town.setBank(town.getBank() - upkeep);
                } else {
                    double remainingUpkeep = upkeep - town.getBank();
                    town.setBank(0);
                    town.setReserve(town.getReserve() - remainingUpkeep);
                    town.sendTownBroadcast(TownRank.RESIDENT, getCurrency().format(remainingUpkeep) + " had to be drawn from the emergency reserve!");
                }
            }
            if (town.getBankAndReserve() < upkeep) {
                town.sendTownBroadcast(TownRank.ASSISTANT, town.getName() + town.getLevel().getSuffix() + " can't afford the daily upkeep for tomorrow.");
                town.sendTownBroadcast(TownRank.ASSISTANT, "You may want to raise taxes or deposit " + getCurrency().getPlural() + " to avoid town destruction.");
            }

            if (town.getPendingTax() != -1) {
                town.setTax(town.getPendingTax());
                town.setPendingTax(-1);
                town.sendTownBroadcast(TownRank.RESIDENT, "The town §fResident Tax§d has changed to " + getCurrency().format(town.getTax()) + "§d for tomorrow.");
            }

            if (town.getPendingPlotTax() != -1) {
                town.setPendingPlotTax(town.getPendingPlotTax());
                town.setPendingPlotTax(-1);
                town.sendTownBroadcast(TownRank.RESIDENT, "The town §fPlot Tax§d has changed to " + getCurrency().format(town.getPlotTax()) + "§d for tomorrow.");
            }

        }

        Bukkit.getPluginManager().callEvent(new PopulaceNewDayEvent());

        new BukkitRunnable() {
            @Override
            public void run() {
                int days = Configuration.PURGE_USER_DATA_DAYS;
                if (days > 0) {
                    getLog().log(Level.INFO, "Purging old users...");
                    int count = 0;
                    List<Resident> toRemove = new ArrayList<>();
                    for (int i = 0; i < ResidentManager.getResidents().size(); i++) {
                        Resident resident = ResidentManager.getResidents().get(i);
                        if (resident.getTown() == null && System.currentTimeMillis() - resident.getSeen() > TimeUnit.DAYS.toMillis(days)) {
                            count++;
                            toRemove.add(resident);
                        }
                    }
                    for (Resident resident : toRemove) {
                        ResidentManager.getResidents().remove(resident);
                    }
                    getLog().log(Level.INFO, "Purged " + count + " user(s) who haven't logged in for " + days + " days.");
                }
            }
        }.runTaskAsynchronously(this);

    }

    @Override
    public void onDisable() {

        // Prevents writing broken data
        if (fullyEnabled){
            try {
                saveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onEnable() {

        // Kicks all players in the event of a reload (which shouldn't happen >_>)
        for(Player player : Bukkit.getOnlinePlayers()){
            player.kickPlayer(Msg.ERR + "Reload detected!");
        }

        instance = this;
        logger = getLogger();

        // Ensures there's a currency to use
        if (AccountManager.getDefaultCurrency() == null){
            getLogger().log(Level.SEVERE, "There is no default currency in MelonEco. Create one, then restart your server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        currency = AccountManager.getDefaultCurrency();
        lastNewDay = System.currentTimeMillis();

        try {

            loadData();

            // Create our fake warzone and spawn towns if they don't exist

            Town warzone = TownManager.getTown("Warzone");
            if (warzone == null) {
                warzone = new Warzone();
                TownManager.getTowns().add(warzone);
            }

            Town spawn = TownManager.getTown("spawn");
            if (spawn == null) {
                spawn = new Spawn();
                TownManager.getTowns().add(spawn);
            }

        } catch (ParseException | IOException e) {
            getLogger().log(Level.SEVERE, "An error occurred while loading data.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register ALL THE THINGS!

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new CommandPreProcess(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);

        getCommand("allow").setExecutor(new AllowCommand());
        getCommand("board").setExecutor(new BoardCommand());
        getCommand("claim").setExecutor(new ClaimCommand());
        getCommand("forsale").setExecutor(new ForSaleCommand());
        getCommand("giveplot").setExecutor(new GivePlotCommand());
        getCommand("invite").setExecutor(new InviteCommand());
        getCommand("join").setExecutor(new JoinCommand());
        getCommand("map").setExecutor(new MapCommand());
        getCommand("newtown").setExecutor(new NewTownCommand());
        getCommand("nextnewday").setExecutor(new NextNewDayCommand());
        getCommand("notforsale").setExecutor(new NotForSaleCommand());
        getCommand("populace").setExecutor(new PopulaceCommand());
        getCommand("plot").setExecutor(new PlotCommand());
        getCommand("jail").setExecutor(new JailCommand());
        getCommand("setjail").setExecutor(new SetJailCommand());
        getCommand("unjail").setExecutor(new UnjailCommand());
        getCommand("town").setExecutor(new TownCommand());
        getCommand("towns").setExecutor(new TownsCommand());
        getCommand("unclaim").setExecutor(new UnclaimCommand());
        getCommand("visit").setExecutor(new VisitCommand());
        getCommand("resident").setExecutor(new ResidentCommand());
        getCommand("visualize").setExecutor(new VisualizeCommand());

        // Register our glow enchantment

        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);

            Enchantment.registerEnchantment(new EnchantGlow(1000));

        } catch (Exception ignored) {
        }

        fullyEnabled = true; // Data successfully loaded!

        // Will check for Chat and Market plugins after all plugins load
        getServer().getScheduler().scheduleSyncDelayedTask(this, this::postSetup);

        // Starts autosave task in case of server crash
        new BukkitRunnable(){
            @Override
            public void run() {
                if (!fullyEnabled)return;
                TownManager.getTowns().stream().filter(town -> town.getBankAndReserve() < town.getDailyUpkeep()).forEach(town -> {
                    town.sendTownBroadcast(TownRank.RESIDENT, town.getName() + town.getLevel().getSuffix() + " is unable to afford the upkeep cost (" + getCurrency().format(town.getDailyUpkeep()) + ")!");
                    town.sendTownBroadcast(TownRank.RESIDENT, "If this doesn't change, it will face destruction in " + getNewDayCountdown() + ".");
                });
                getLog().log(Level.INFO, "Autosaving...");
                try {
                    saveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this, TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(5));

        // Runs the new day code
        new BukkitRunnable(){
            @Override
            public void run() {
                if (System.currentTimeMillis() - getLastNewDay() > TimeUnit.HOURS.toMillis(Configuration.NEW_DAY_INTERVAL)) {
                    newDay();
                    lastNewDay = System.currentTimeMillis();
                }
            }
        }.runTaskTimer(this, 600L, 600L);

    }
}
