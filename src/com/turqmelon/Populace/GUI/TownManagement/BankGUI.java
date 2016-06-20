package com.turqmelon.Populace.GUI.TownManagement;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.GUI.TownGUI;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownRank;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
public class BankGUI extends TownGUI {
    public BankGUI(Resident resident, Town town) {
        super(resident, town);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();

        TownRank rank = getTown().getRank(getResident());
        if (rank != TownRank.MAYOR){
            player.closeInventory();
            return;
        }

        int raw = event.getRawSlot();
        if (raw == 0){
            TownGUI gui = new TownGUI(getResident(), getTown(), 1);
            gui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
        else if (raw == 4){
            TaxesGUI gui = new TaxesGUI(getResident(), getTown());
            gui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
        else{

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.STAINED_GLASS_PANE){

                double amt = Double.parseDouble(ItemUtil.getTag(clicked, "ecoamount").toString().replace("\"", ""));
                EcoAction action = EcoAction.valueOf(ItemUtil.getTag(clicked, "ecoaction").toString().replace("\"", ""));

                if (performTransaction(getResident(), action, amt)){
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    repopulate();
                }
                else{
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                }


            }

        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();
        inv.setItem(0, new ItemBuilder(Material.PAPER).withCustomName("§e§l< BACK").build());
        inv.setItem(4, getTown().getIcon(IconType.TREASURY, getResident()));

        int[] d100 = {18, 19, 27, 36};
        int[] w100 = {20, 29, 38, 37};
        int[] d1000 = {21, 22, 30, 39};
        int[] w1000 = {23, 32, 41, 40};
        int[] d10000 = {24, 25, 33, 42};
        int[] w10000 = {26, 35, 44, 43};

        String bankBalance = "§6§lBank Balance: " + Populace.getCurrency().format(getTown().getBank());

        inv.setItem(28, new ItemBuilder(Material.GOLD_NUGGET).withCustomName(bankBalance).build());
        inv.setItem(31, new ItemBuilder(Material.GOLD_INGOT).withCustomName(bankBalance).build());
        inv.setItem(34, new ItemBuilder(Material.GOLD_BLOCK).withCustomName(bankBalance).build());

        for(int i : d100){
            inv.setItem(i, getButton(EcoAction.DEPOSIT, 100));
        }

        for(int i : w100){
            inv.setItem(i, getButton(EcoAction.WITHDRAW, 100));
        }

        for(int i : d1000){
            inv.setItem(i, getButton(EcoAction.DEPOSIT, 1000));
        }

        for(int i : w1000){
            inv.setItem(i, getButton(EcoAction.WITHDRAW, 1000));
        }

        for(int i : d10000){
            inv.setItem(i, getButton(EcoAction.DEPOSIT, 10000));
        }

        for(int i : w10000){
            inv.setItem(i, getButton(EcoAction.WITHDRAW, 10000));
        }

    }

    private ItemStack getButton(EcoAction action, double amount){
        return new ItemBuilder(Material.STAINED_GLASS_PANE)
                .withData((byte)(action==EcoAction.WITHDRAW?14:5))
                .withCustomName((action==EcoAction.WITHDRAW?"§c":"§a")+"§l"+action.getName() + " " + Populace.getCurrency().format(amount))
                .tagWith("ecoamount", new NBTTagString(amount+""))
                .tagWith("ecoaction", new NBTTagString(action.name())).build();
    }

    private boolean performTransaction(Resident resident, EcoAction action, double amount){

        TownRank rank = getTown().getRank(resident);
        if (rank != TownRank.MAYOR){
            return false;
        }

        Account account = AccountManager.getAccount(resident.getUuid());

        switch(action){
            case WITHDRAW:
                double withdrawAmt = amount;
                if (withdrawAmt > getTown().getBank()){
                    withdrawAmt = getTown().getBank();
                }

                if (account != null && account.deposit(Populace.getCurrency(), withdrawAmt)){

                    getTown().setBank(getTown().getBank()-withdrawAmt);
                    getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + resident.getName() + " withdrew " + Populace.getCurrency().format(withdrawAmt) + " from the bank.");
                    return true;
                }
                else{
                    resident.sendMessage(Msg.ERR + "Withdraw failed.");
                    return false;
                }
            case DEPOSIT:

                if (account != null && account.withdraw(Populace.getCurrency(), amount)){
                    getTown().setBank(getTown().getBank()+amount);
                    getTown().sendTownBroadcast(TownRank.RESIDENT, "Mayor " + resident.getName() + " deposited " + Populace.getCurrency().format(amount) + " to the bank.");
                    return true;
                }
                else{
                    resident.sendMessage(Msg.ERR + "You don't have enough " + Populace.getCurrency().getPlural() + " to deposit.");
                    return false;
                }


        }

        return false;

    }

    enum EcoAction {
        WITHDRAW("Withdraw"),
        DEPOSIT("Deposit");

        private String name;

        EcoAction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
