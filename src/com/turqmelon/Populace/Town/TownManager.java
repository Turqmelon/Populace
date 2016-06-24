package com.turqmelon.Populace.Town;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.Events.Town.TownCreationEvent;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.Configuration;
import com.turqmelon.Populace.Utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
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
public class TownManager {

    private static List<Town> towns = new ArrayList<>();

    public static void createTown(Resident mayor, String name){
        createTown(mayor, name, false);
    }

    // Creates a new town on behalf of the supplied resident. Will validate the name. Passing "true"
    // for the confirmation will ackowledge the town creation fee (if applicable)
    private static void createTown(Resident mayor, String name, boolean confirm){

        // Prevents being in multiple towns at once
        if (mayor.getTown() != null){
            mayor.sendMessage(Msg.ERR + "You must leave your town to start one!");
            return;
        }

        // Ensures names are at least 2 characters
        if (name.length() < 2){
            mayor.sendMessage(Msg.ERR + "Town name must be at least 2 characters.");
            return;
        }

        // Ensures names don't exceed 16 characters
        if (name.length() > 16){
            mayor.sendMessage(Msg.ERR + "Town name can't exceed 16 characters.");
            return;
        }

        // Ensures names only consist of letters
        if (!name.matches("[A-Za-z]+")){
            mayor.sendMessage(Msg.ERR + "Town names must be alphabetic. (No numbers, no symbols. Only letters.)");
            return;
        }

        // Ensures the name is not in use
        if (getTown(name) != null){
            mayor.sendMessage(Msg.ERR + "There is already a town named \"" + name + "\".");
            return;
        }

        double price = Configuration.TOWN_CREATION_FEE;

        if (confirm||price <= 0){
            Account account = AccountManager.getAccount(mayor.getUuid());
            if (price == 0  || (account != null && account.withdraw(Populace.getCurrency(), price))){

                Town town = new Town(UUID.randomUUID(), name, TownLevel.getAppropriateLevel(1));
                town.getResidents().put(mayor, TownRank.MAYOR);
                mayor.setTown(town);
                town.setBank(Configuration.TOWN_BANK_DEFAULT);
                town.setFounded(System.currentTimeMillis());
                town.setTax(Configuration.TOWN_DEFAULT_TAX);
                town.setPlotTax(Configuration.TOWN_DEFAULT_PLOT_TAX);
                town.setSalesTax(Configuration.TOWN_DEFAULT_SALES_TAX);

                TownManager.getTowns().add(town);
                Bukkit.broadcastMessage(Msg.OK + mayor.getName() + " founded " + town.getName() + town.getLevel().getSuffix() + "!");
                town.sendTownBroadcast(TownRank.RESIDENT, "Welcome to your new town!");

                TownCreationEvent event = new TownCreationEvent(town, mayor);
                Bukkit.getPluginManager().callEvent(event);

                // Welcome messages. Introduces the new mayor to some basic commands.

                new BukkitRunnable(){

                    @Override
                    public void run() {
                        town.sendTownBroadcast(TownRank.RESIDENT, "Get started by claiming your first plot of land: §f/claim");
                    }
                }.runTaskLater(Populace.getInstance(), 40L);
                new BukkitRunnable(){

                    @Override
                    public void run() {
                        town.sendTownBroadcast(TownRank.RESIDENT, "View details about your town and change settings: §f/town");
                    }
                }.runTaskLater(Populace.getInstance(), 70L);
                new BukkitRunnable(){

                    @Override
                    public void run() {
                        town.sendTownBroadcast(TownRank.RESIDENT, "Invite more people to your town: §f/invite");
                    }
                }.runTaskLater(Populace.getInstance(), 100L);
                new BukkitRunnable(){

                    @Override
                    public void run() {
                        town.sendTownBroadcast(TownRank.RESIDENT, "We've added " + Populace.getCurrency().format(town.getBank()) + " to your town bank to start you off.");
                    }
                }.runTaskLater(Populace.getInstance(), 130L);


            }
            else{
                mayor.sendMessage(Msg.ERR + "You can't afford the town creation fee.");
            }
        }
        else{
            mayor.sendMessage(Msg.INFO + "Create the town \"" + name + "\" for " + Populace.getCurrency().format(price) + "?");
            mayor.setPendingAction(() -> createTown(mayor, name, true));
        }

    }

    // Returns a town with the matching UUID
    public static Town getTown(UUID uuid){
        for(Town town : getTowns()){
            if (town.getUuid().equals(uuid)){
                return town;
            }
        }
        return null;
    }

    // Returns a town with the matching name (not case sensitive)
    public static Town getTown(String name){
        for(Town town : getTowns()){
            if (town.getName().equalsIgnoreCase(name)){
                return town;
            }
        }
        return null;
    }

    // Returns the "Spawn" town
    public static Spawn getSpawn() {
        for (Town town : getTowns()) {
            if ((town instanceof Spawn)) {
                return (Spawn) town;
            }
        }
        return null;
    }

    // Returns the "Warzone" town
    public static Warzone getWarzone() {
        for (Town town : getTowns()) {
            if ((town instanceof Warzone)) {
                return (Warzone) town;
            }
        }
        return null;
    }

    // If true, returns all towns. If false, returns all player towns.
    public static List<Town> getTowns(boolean includeSpecial) {
        if (includeSpecial) {
            return getTowns();
        } else {
            List<Town> list = new ArrayList<>();
            for (Town town : getTowns()) {
                if (!town.isSpecial()) {
                    list.add(town);
                }
            }
            return list;
        }
    }

    public static List<Town> getTowns() {
        return towns;
    }
}
