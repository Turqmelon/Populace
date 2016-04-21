package com.turqmelon.Populace.Utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemUtil {

    public static boolean hasName(ItemStack item) {
        return hasMetaData(item) && item.getItemMeta().getDisplayName() != null;
    }

    public static boolean hasMetaData(ItemStack item) {
        return item != null && item.getItemMeta() != null;
    }

    public static boolean hasLore(ItemStack item) {
        return hasMetaData(item) && item.getItemMeta().getLore() != null;
    }

    public static boolean hasData(ItemStack item) {
        return item.getData() != null && item.getDurability() != 0;
    }

    public static boolean hasEnchants(ItemStack item) {
        return item.getEnchantments() != null && !item.getEnchantments().isEmpty();
    }

    public static boolean hasHeadOwner(ItemStack item) {
        return hasMetaData(item) && item.getItemMeta() instanceof SkullMeta && ((SkullMeta) item.getItemMeta()).getOwner() != null && !((SkullMeta) item.getItemMeta()).getOwner().equals("");
    }

    public static boolean hasLeatherColor(ItemStack item) {
        return hasMetaData(item) && item.getItemMeta() instanceof LeatherArmorMeta;
    }

    public static org.bukkit.inventory.ItemStack createSkull(String urlToFormat) {
        String url = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" + urlToFormat;
        org.bukkit.inventory.ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        if (url.isEmpty()) {
            return head;
        }
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

    public static void addGlow(ItemStack item) {
        item.addEnchantment(Enchantment.getByName("Glow"), 1);
    }

    public static ItemStack deepCopy(ItemStack item) {
        return item == null ? null : new ItemBuilder(item).build();
    }

    public static String getEnchantmentName(Enchantment enchantment) {

        if (enchantment.getName().equalsIgnoreCase(Enchantment.ARROW_DAMAGE.getName())) {
            return "Power";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.ARROW_FIRE.getName())) {
            return "Flame";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.ARROW_INFINITE.getName())) {
            return "Infinity";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.DAMAGE_ALL.getName())) {
            return "Sharpness";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.DAMAGE_ARTHROPODS.getName())) {
            return "Bane of Arthropods";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.DAMAGE_UNDEAD.getName())) {
            return "Smite";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.ARROW_KNOCKBACK.getName())) {
            return "Punch";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.DEPTH_STRIDER.getName())) {
            return "Depth Strider";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.DIG_SPEED.getName())) {
            return "Efficiency";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.DURABILITY.getName())) {
            return "Unbreaking";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.FIRE_ASPECT.getName())) {
            return "Fire Aspect";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.KNOCKBACK.getName())) {
            return "Knockback";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.LOOT_BONUS_BLOCKS.getName())) {
            return "Fortune";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.LOOT_BONUS_MOBS.getName())) {
            return "Looting";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.LUCK.getName())) {
            return "Luck of the Seas";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.LURE.getName())) {
            return "Lure";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.OXYGEN.getName())) {
            return "Respiration";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.PROTECTION_ENVIRONMENTAL.getName())) {
            return "Protection";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.PROTECTION_EXPLOSIONS.getName())) {
            return "Blast Resistance";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.PROTECTION_FALL.getName())) {
            return "Feather Falling";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.PROTECTION_FIRE.getName())) {
            return "Fire Resistance";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.PROTECTION_PROJECTILE.getName())) {
            return "Projectile Protection";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.SILK_TOUCH.getName())) {
            return "Silk Touch";
        } else if (enchantment.getName().equalsIgnoreCase(Enchantment.THORNS.getName())) {
            return "Thorns";
        }

        return WordUtils.capitalizeFully(enchantment.getName().replace("_", " "));
    }

    public static String getItemName(ItemStack item) {

        if (hasName(item)) {
            return WordUtils.capitalize(item.getItemMeta().getDisplayName());
        }

        //TODO Arrays.asList(derp).get(item.getDurability);
        if (item.getType() == Material.STONE) {
            switch (item.getDurability()) {
                case 0:
                    return "Stone";
                case 1:
                    return "Granite";
                case 2:
                    return "Polished Granite";
                case 3:
                    return "Diorite";
                case 4:
                    return "Polished Diorite";
                case 5:
                    return "Andesite";
                case 6:
                    return "Polished Andesite";
            }
        } else if (item.getType() == Material.DIRT) {
            switch (item.getDurability()) {
                case 0:
                    return "Dirt";
                case 1:
                    return "Coarse Dirt";
                case 2:
                    return "Podzol";
            }
        } else if (item.getType() == Material.WOOD) {
            switch (item.getDurability()) {
                case 0:
                    return "Oak Wood Planks";
                case 1:
                    return "Spruce Wood Planks";
                case 2:
                    return "Birch Wood Planks";
                case 3:
                    return "Jungle Wood Planks";
                case 4:
                    return "Acacia Wood Planks";
                case 5:
                    return "Dark Oak Wood Planks";
            }
        } else if (item.getType() == Material.SAND) {
            switch (item.getDurability()) {
                case 0:
                    return "Sand";
                case 1:
                    return "Red Sand";
            }
        } else if (item.getType() == Material.LOG) {
            switch (item.getDurability()) {
                case 0:
                    return "Oak Wood";
                case 1:
                    return "Spruce Wood";
                case 2:
                    return "Birch Wood";
                case 3:
                    return "Jungle Wood";
            }
        } else if (item.getType() == Material.LOG_2) {
            switch (item.getDurability()) {
                case 0:
                    return "Acacia Wood";
                case 1:
                    return "Dark Oak Wood";
            }
        } else if (item.getType() == Material.SPONGE) {
            switch (item.getDurability()) {
                case 0:
                    return "Sponge";
                case 1:
                    return "Wet Sponge";
            }
        } else if (item.getType() == Material.SANDSTONE) {
            switch (item.getDurability()) {
                case 0:
                    return "Sandstone";
                case 1:
                    return "Chiseled Sandstone";
                case 2:
                    return "Smooth Sandstone";
            }
        } else if (item.getType() == Material.WOOL) {
            switch (item.getDurability()) {
                case 0:
                    return "Wool";
                case 1:
                    return "Orange Wool";
                case 2:
                    return "Magenta Wool";
                case 3:
                    return "Light Blue Wool";
                case 4:
                    return "Yellow Wool";
                case 5:
                    return "Lime Wool";
                case 6:
                    return "Pink Wool";
                case 7:
                    return "Gray Wool";
                case 8:
                    return "Light Gray Wool";
                case 9:
                    return "Cyan Wool";
                case 10:
                    return "Purple Wool";
                case 11:
                    return "Blue Wool";
                case 12:
                    return "Brown Wool";
                case 13:
                    return "Green Wool";
                case 14:
                    return "Red Wool";
                case 15:
                    return "Black Wool";
            }
        } else if (item.getType() == Material.INK_SACK) {
            switch (item.getDurability()) {
                case 0:
                    return "Ink Sac";
                case 1:
                    return "Rose Red";
                case 2:
                    return "Cactus Green";
                case 3:
                    return "Cocoa Beans";
                case 4:
                    return "Lapis Lazuli";
                case 5:
                    return "Purple Dye";
                case 6:
                    return "Cyan Dye";
                case 7:
                    return "Light Gray Dye";
                case 8:
                    return "Gray Dye";
                case 9:
                    return "Pink Dye";
                case 10:
                    return "Lime Dye";
                case 11:
                    return "Dandelion Yellow";
                case 12:
                    return "Light Blue Dye";
                case 13:
                    return "Magenta Dye";
                case 14:
                    return "Orange Dye";
                case 15:
                    return "Bone Meal";
            }
        } else if (item.getType() == Material.STAINED_GLASS) {
            switch (item.getDurability()) {
                case 0:
                    return "White Stained Glass";
                case 1:
                    return "Orange Stained Glass";
                case 2:
                    return "Magenta Stained Glass";
                case 3:
                    return "Light Blue Stained Glass";
                case 4:
                    return "Yellow Stained Glass";
                case 5:
                    return "Lime Stained Glass";
                case 6:
                    return "Pink Stained Glass";
                case 7:
                    return "Gray Stained Glass";
                case 8:
                    return "Light Gray Stained Glass";
                case 9:
                    return "Cyan Stained Glass";
                case 10:
                    return "Purple Stained Glass";
                case 11:
                    return "Blue Stained Glass";
                case 12:
                    return "Brown Stained Glass";
                case 13:
                    return "Green Stained Glass";
                case 14:
                    return "Red Stained Glass";
                case 15:
                    return "Black Stained Glass";
            }
        } else if (item.getType() == Material.SMOOTH_BRICK) {
            switch (item.getDurability()) {
                case 0:
                    return "Stone Bricks";
                case 1:
                    return "Mossy Stone Bricks";
                case 2:
                    return "Cracked Stone Bricks";
                case 3:
                    return "Chiseled Stone Bricks";
            }
        } else if (item.getType() == Material.SULPHUR) {
            return "Gunpowder";
        } else if (item.getType() == Material.PRISMARINE) {
            switch (item.getDurability()) {
                case 0:
                    return "Prismarine";
                case 1:
                    return "Prismarine Bricks";
                case 2:
                    return "Dark Prismarine";
            }
        } else if (item.getType() == Material.EXP_BOTTLE) {
            return "Bottle o' Enchanting";
        } else if (item.getType() == Material.TNT)
            return "TNT";
        else if (item.getType() == Material.STEP) {
            switch (item.getDurability()) {
                case 0:
                    return "Stone Slab";
                case 1:
                    return "Sandstone Slab";
                case 2:
                    return "Wooden Slab";
                case 3:
                    return "Cobblestone Slab";
                case 4:
                    return "Brick Slab";
                case 5:
                    return "Stone Brick Slab";
                case 6:
                    return "Nether Brick Slab";
                case 7:
                    return "Quartz Slab";
            }
        }

        return WordUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));
    }

    public static String itemToJSON(ItemStack stack) {
        if (stack == null) return null;
        ItemStack newStack = stack.clone();
        ItemMeta meta = newStack.getItemMeta();

        if (hasName(newStack))
            meta.setDisplayName(meta.getDisplayName().replaceAll("§", "&"));

        if (hasLore(newStack)) {
            List<String> newLore = meta.getLore().stream().map(s -> s.replaceAll("§", "&")).collect(Collectors.toList());
            meta.setLore(newLore);
        }

        newStack.setItemMeta(meta);

        return CraftItemStack.asNMSCopy(newStack).save(new NBTTagCompound()).toString();
    }

    public static ItemStack JSONtoItemStack(String json) throws MojangsonParseException {
        if (json == null) return null;
        ItemStack stack = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_8_R3.ItemStack.createStack(MojangsonParser.parse(json)));
        ItemMeta meta = stack.getItemMeta();

        if (hasName(stack))
            meta.setDisplayName(meta.getDisplayName().replaceAll("&", "§"));

        if (hasLore(stack)) {
            List<String> newLore = meta.getLore().stream().map(s -> s.replaceAll("&", "§")).collect(Collectors.toList());
            meta.setLore(newLore);
        }

        stack.setItemMeta(meta);

        return stack;
    }

    public static ItemStack addTag(ItemStack stack, String tagName, NBTBase tag) {
        NBTTagCompound base = new NBTTagCompound();
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        nmsStack.save(base);

        base.getCompound("tag").set(tagName, tag);

        nmsStack.c(base);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static NBTBase getTag(ItemStack stack, String tagName) {
        NBTTagCompound base = new NBTTagCompound();
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        nmsStack.save(base);

        return base.getCompound("tag").get(tagName);
    }
}