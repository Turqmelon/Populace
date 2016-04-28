package com.turqmelon.Populace;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.MelonEco.utils.Currency;
import com.turqmelon.Populace.Commands.*;
import com.turqmelon.Populace.Events.Core.PopulaceNewDayEvent;
import com.turqmelon.Populace.Listeners.*;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Town.TownRank;
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

    private void postSetup() {
        PluginManager pm = getServer().getPluginManager();
        Plugin chat = pm.getPlugin("PopulaceChat");
        Plugin market = pm.getPlugin("PopulaceMarket");

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
    }

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
                    TownManager.getTowns().add(new Town(town));
                }

                getLog().log(Level.INFO, "Loaded " + TownManager.getTowns().size() + " towns.");

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveData() throws IOException {

        getLog().log(Level.INFO, "Saving data...");

        File dir = getInstance().getDataFolder();
        if (!dir.exists()){
            dir.mkdir();
        }

        File file = new File(dir, "data.json");
        if (!file.exists()){
            file.createNewFile();
        }

        JSONObject data = new JSONObject();

        JSONArray residents = new JSONArray();
        for(Resident resident : ResidentManager.getResidents()){
            residents.add(resident.toJSON());
        }

        JSONArray towns = new JSONArray();
        for(Town town : TownManager.getTowns()){
            towns.add(town.toJSON());
        }

        data.put("lastnewday", getLastNewDay());
        data.put("residents", residents);
        data.put("towns", towns);

        FileWriter writer = new FileWriter(file);
        writer.write(data.toJSONString());
        writer.flush();
        writer.close();

        getLog().log(Level.INFO, "Saving complete.");

    }

    // Runs daily, keeps everything working swell
    public void newDay() {

        Bukkit.broadcastMessage(Msg.OK + "A new day begins...");

        Bukkit.getPluginManager().callEvent(new PopulaceNewDayEvent());

        // Collect taxes from all residents
        for(Resident resident : ResidentManager.getResidents()){
            double upkeep = resident.getDailyTax();
            if (upkeep == 0)continue; // Players not in towns and mayors
            Account account = AccountManager.getAccount(resident.getUuid());
            if (account == null || !account.withdraw(Populace.getCurrency(), upkeep)){
                // The resident could not afford their taxes
                resident.getTown().kickOut(resident, null, "Unable to afford taxes.");
            }
            else{
                // Good residents are good. Inform them of the deduction
                resident.sendMessage(Msg.OK + Populace.getCurrency().format(upkeep) + " was deducted for your daily taxes. Thank you!");
            }

            // If the resident can't afford the upkeep tomorrow, warn them
            if (account != null && account.getBalance(Populace.getCurrency()) < upkeep){
                resident.sendMessage(Msg.WARN + "You currently do not have enough " + Populace.getCurrency().getPlural() + " to pay your taxes tomorrow.");
                resident.sendMessage(Msg.WARN + "Make sure you collect more (or unclaim plots), otherwise you'll be kicked from the town.");
            }
        }

        // Collect daily upkeep from towns
        for(Town town : TownManager.getTowns()){
            if (System.currentTimeMillis() < town.getGracePeriodExpiration()){
                town.sendTownBroadcast(TownRank.RESIDENT, "Town was not charged daily upkeep since the grace period is still active.");
                continue;
            }
            double upkeep = town.getDailyUpkeep();
            double bank = town.getBank();

            if (bank < upkeep){
                // The town can't afford their daily upkeep. Destroy it.
                Bukkit.broadcastMessage(Msg.WARN + town.getName() + " has fallen to ruin.");
                town.destroy(true);
            }
            else{
                // Take the daily upkeep from the bank. Their safe for another day.
                town.setBank(town.getBank()-upkeep);
                town.sendTownBroadcast(TownRank.RESIDENT, "The daily upkeep of " + getCurrency().format(upkeep) + " has been collected from the town bank. Thank you!");
            }
            if (town.getBank() < upkeep){
                town.sendTownBroadcast(TownRank.ASSISTANT, town.getName() + town.getLevel().getSuffix() + " can't afford the daily upkeep for tomorrow.");
                town.sendTownBroadcast(TownRank.ASSISTANT, "You may want to raise taxes or deposit " + getCurrency().getPlural() + " to avoid town destruction.");
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                int days = Configuration.PURGE_USER_DATA_DAYS;
                if (days > 0) {
                    getLog().log(Level.INFO, "Purging old users...");
                    int count = 0;
                    for (int i = 0; i < ResidentManager.getResidents().size(); i++) {
                        Resident resident = ResidentManager.getResidents().get(i);
                        if (resident.getTown() == null && System.currentTimeMillis() - resident.getSeen() > TimeUnit.DAYS.toMillis(days)) {
                            count++;
                            ResidentManager.getResidents().remove(resident);
                        }
                    }
                    getLog().log(Level.INFO, "Purged " + count + " user(s) who haven't logged in for " + days + " days.");
                }
            }
        }.runTaskAsynchronously(this);

    }

    public static String getNewDayCountdown(){
        long nextDay = getLastNewDay()+TimeUnit.DAYS.toMillis(1);
        return ClockUtil.formatDateDiff(nextDay, false);
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

        for(Player player : Bukkit.getOnlinePlayers()){
            player.kickPlayer(Msg.ERR + "Reload detected!");
        }

        instance = this;
        logger = getLogger();

        if (AccountManager.getDefaultCurrency() == null){
            getLogger().log(Level.SEVERE, "There is no default currency in MelonEco. Create one, then restart your server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        currency = AccountManager.getDefaultCurrency();
        lastNewDay = System.currentTimeMillis();

        try {
            loadData();
        } catch (ParseException | IOException e) {
            getLogger().log(Level.SEVERE, "An error occurred while loading data.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new CommandPreProcess(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getCommand("allow").setExecutor(new AllowCommand());
        getCommand("claim").setExecutor(new ClaimCommand());
        getCommand("forsale").setExecutor(new ForSaleCommand());
        getCommand("giveplot").setExecutor(new GivePlotCommand());
        getCommand("invite").setExecutor(new InviteCommand());
        getCommand("join").setExecutor(new JoinCommand());
        getCommand("map").setExecutor(new MapCommand());
        getCommand("newtown").setExecutor(new NewTownCommand());
        getCommand("notforsale").setExecutor(new NotForSaleCommand());
        getCommand("populace").setExecutor(new PopulaceCommand());
        getCommand("plot").setExecutor(new PlotCommand());
        getCommand("town").setExecutor(new TownCommand());
        getCommand("towns").setExecutor(new TownsCommand());
        getCommand("unclaim").setExecutor(new UnclaimCommand());
        getCommand("visit").setExecutor(new VisitCommand());
        getCommand("resident").setExecutor(new ResidentCommand());
        getCommand("visualize").setExecutor(new VisualizeCommand());

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

        new BukkitRunnable(){
            @Override
            public void run() {
                if (!fullyEnabled)return;
                getLog().log(Level.INFO, "Autosaving...");
                try {
                    saveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this, TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(5));

        new BukkitRunnable(){
            @Override
            public void run() {
                if (System.currentTimeMillis() - getLastNewDay() > TimeUnit.DAYS.toMillis(1)){
                    newDay();
                    lastNewDay = System.currentTimeMillis();
                }
            }
        }.runTaskTimer(this, 600L, 600L);

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
}
