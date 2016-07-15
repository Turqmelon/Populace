package com.turqmelon.Populace.Town;

import com.turqmelon.Populace.Utils.ClockUtil;
import com.turqmelon.Populace.Utils.ItemUtil;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;

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

public class TownAnnouncement implements Comparable {

    private UUID uuid;
    private String title;
    private String text;
    private TownRank requiredRank;
    private long posted;

    public TownAnnouncement(String title, String text, TownRank requiredRank, long posted) {
        this.requiredRank = requiredRank;
        this.uuid = UUID.randomUUID();
        this.title = title;
        this.text = text;
        this.posted = posted;
    }

    public TownAnnouncement(JSONObject object) {
        this.uuid = UUID.fromString((String) object.get("uuid"));
        this.title = (String) object.get("title");
        this.text = (String) object.get("text");
        this.posted = (long) object.get("posted");
        this.requiredRank = TownRank.valueOf((String) object.get("rank"));
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("uuid", getUuid().toString());
        obj.put("title", getTitle());
        obj.put("text", getText());
        obj.put("posted", getPosted());
        obj.put("rank", getRequiredRank().name());
        return obj;
    }

    public TownRank getRequiredRank() {
        return requiredRank;
    }

    public void setRequiredRank(TownRank requiredRank) {
        this.requiredRank = requiredRank;
    }

    public ItemStack toIcon(TownRank rank) {
        return toIcon(rank, false);
    }

    public ItemStack toIcon(TownRank rank, boolean preview) {
        ItemStack icon = new ItemStack(Material.EMPTY_MAP, 1);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName("§e§l" + getTitle());
        List<String> lore = new ArrayList<>();
        if (preview) {
            lore.add("§6Preview");
        } else {
            lore.add("§7Posted " + ClockUtil.formatDateDiff(getPosted(), true) + " ago");
        }
        lore.add("§a");
        if (preview || rank.isAtLeast(getRequiredRank())) {
            String[] words = getText().split(" ");
            int count = 0;
            String line = "";
            for (String word : words) {
                line = line + word + " ";
                count++;
                if (count >= 7) {
                    lore.add("§f" + line);
                    line = "";
                    count = 0;
                }
            }
            if (line.length() > 0) {
                lore.add("§f" + line);
            }
        } else {
            lore.add("§cYou can't view this.");
        }
        if (preview) {
            lore.add("§a");
            lore.add("§fWill be viewable to: " + getRequiredRank().getPrefix());
        } else if (rank.isAtLeast(TownRank.MAYOR)) {
            lore.add("§a");
            lore.add("§cShift Right Click§f to delete.");
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return ItemUtil.addTag(icon, "announcementid", new NBTTagString(getUuid().toString()));
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getPosted() {
        return posted;
    }

    public void setPosted(long posted) {
        this.posted = posted;
    }

    @Override
    public int compareTo(Object o) {
        if ((o instanceof TownAnnouncement)) {
            TownAnnouncement announcement = (TownAnnouncement) o;
            if (announcement.getPosted() > getPosted()) {
                return 1;
            } else if (announcement.getPosted() < getPosted()) {
                return -1;
            }
        }
        return 0;
    }
}
