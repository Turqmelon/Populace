package com.turqmelon.Populace.Utils;

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

import com.google.common.collect.ImmutableList;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Dye;
import org.bukkit.potion.Potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    private Material material = null;
    private int amount = 1;
    private short data = 0;
    private String customName = null;
    private List<String> lore = null;
    private String headOwner = null;
    private Color leatherColor = null;
    private PotionMeta potionMeta = null;
    private boolean glowing = false;
    private List<ItemFlag> itemFlags = new ArrayList<>();
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private Map<String, NBTBase> nbt = new HashMap<>();

    public ItemBuilder(ItemStack item) {
        this.material = item.getType();
        this.amount = item.getAmount();

        if (ItemUtil.hasName(item)) {
            this.customName = ItemUtil.getItemName(item);
        }
        if (ItemUtil.hasLore(item)) {
            this.lore = item.getItemMeta().getLore();
        }
        if (ItemUtil.hasData(item)) {
            this.data = item.getDurability();
        }
        if (ItemUtil.hasEnchants(item)) {
            this.enchantments = item.getEnchantments();
        }
        if (ItemUtil.hasHeadOwner(item)) {
            this.headOwner = ((SkullMeta) item.getItemMeta()).getOwner();
        }

        if (ItemUtil.hasLeatherColor(item)) {
            LeatherArmorMeta m = (LeatherArmorMeta) item.getItemMeta();
            this.leatherColor = m.getColor();
        }
    }

    public ItemBuilder(Potion potion) {
        this(potion.toItemStack(1));
    }

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder(DyeColor dye) {
        Dye dyeItem = new Dye();
        dyeItem.setColor(dye);
        ItemStack is = dyeItem.toItemStack();
        this.material = is.getType();
        this.data = (byte) is.getDurability();
    }

    public ItemBuilder basedOn(PotionMeta meta) {
        this.potionMeta = meta;
        return this;
    }

    public ItemBuilder asColor(Color color) {
        this.leatherColor = color;
        return this;
    }

    public ItemBuilder withLore(String... lore) {
        this.lore = ImmutableList.copyOf(lore);
        return this;
    }

    public ItemBuilder withLore(Iterable<String> lore) {
        this.lore = ImmutableList.copyOf(lore);
        return this;
    }

    public ItemBuilder ofType(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder makeItGlow() {
        this.glowing = true;
        return this;
    }

    public ItemBuilder includeEnchantment(Enchantment enchant, int level) {
        if (enchant == null)
            return this;
        if (enchantments == null)
            enchantments = new HashMap<>();
        enchantments.put(enchant, level);
        return this;
    }

    public ItemBuilder includeEnchantments(Map<Enchantment, Integer> enchants) {
        if (enchantments == null)
            enchantments = enchants;
        else {
            for (Map.Entry<Enchantment, Integer> set : enchants.entrySet())
                enchantments.put(set.getKey(), set.getValue());
        }
        return this;
    }

    public ItemBuilder asHeadOwner(String name) {
        this.headOwner = name;
        return this;
    }


    public ItemBuilder withLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder withCustomName(String customName) {
        this.customName = customName;
        return this;
    }

    public ItemBuilder withData(short data) {
        this.data = data;
        return this;
    }

    public ItemBuilder withData(byte data) {
        this.data = data;
        return this;
    }

    public ItemBuilder withQuantity(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder tagWith(String tag, NBTTagString base){
        nbt.put(tag, base);
        return this;
    }

    public ItemBuilder flagWith(ItemFlag flag) {
        if (!this.itemFlags.contains(flag)) {
            this.itemFlags.add(flag);
        }
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(this.material, this.amount, this.data);
        if (this.customName != null || this.lore != null || this.headOwner != null || this.leatherColor != null || this.potionMeta != null) {
            ItemMeta meta = item.getItemMeta();
            if (this.potionMeta != null && this.material == Material.POTION) {
                meta = this.potionMeta;
            }
            if (headOwner != null && this.material == Material.SKULL_ITEM) {
                SkullMeta m = (SkullMeta) meta;
                m.setOwner(headOwner);
                meta = m;
            }

            if (this.leatherColor != null && meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta m = (LeatherArmorMeta) meta;
                m.setColor(this.leatherColor);
                meta = m;
            }

            if (this.customName != null) {
                meta.setDisplayName(this.customName);
            }
            if (this.lore != null) {
                meta.setLore(this.lore);
            }
            if (!this.itemFlags.isEmpty()) {
                for (ItemFlag flag : this.itemFlags) {
                    meta.addItemFlags(flag);
                }
            }
            item.setItemMeta(meta);
        }

        if (this.enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
                item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }

        if (!item.getEnchantments().isEmpty() && this.glowing) {
            ItemUtil.addGlow(item);
        }

        if (!nbt.isEmpty()){
            for(String key : nbt.keySet()){
                NBTBase n = nbt.get(key);
                item = ItemUtil.addTag(item, key, n);
            }
        }

        return item;
    }

}