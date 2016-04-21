package com.turqmelon.Populace.Town;

import org.bukkit.Material;

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
public enum PermissionSet {

    VISIT("Visit", "Permits warping to town spawn.", "warp to town spawn", TownRank.GUEST, new PermissionScope[]{PermissionScope.TOWN}, Material.ENDER_PORTAL_FRAME),
    ENTRY("Entry", "Permits entry into the area.", "walk into the plot", TownRank.GUEST, new PermissionScope[]{PermissionScope.PLOT}, Material.GOLD_BOOTS),
    SHOP("Shop", "Permits buying from shops.", "buy from shops", TownRank.GUEST, new PermissionScope[]{PermissionScope.TOWN}, Material.DIAMOND),
    ACCESS("Access", "Permits using doors and switches.", "use doors/switches", TownRank.RESIDENT, new PermissionScope[]{PermissionScope.PLOT, PermissionScope.TOWN}, Material.WOOD_DOOR),
    CONTAINER("Container", "Permits accessing containers.", "open chests and containers", TownRank.RESIDENT, new PermissionScope[]{PermissionScope.PLOT, PermissionScope.TOWN}, Material.CHEST),
    BUILD("Build", "Permits building and destroying blocks.", "build", TownRank.ASSISTANT, new PermissionScope[]{PermissionScope.PLOT, PermissionScope.TOWN}, Material.WORKBENCH);

    private String name;
    private String description;
    private String loredescription;
    private TownRank defaultRank;
    private PermissionScope[] scopes;
    private Material icon;

    PermissionSet(String name, String description, String loredescription, TownRank defaultRank, PermissionScope[] scopes, Material icon) {
        this.name = name;
        this.description = description;
        this.loredescription = loredescription;
        this.defaultRank = defaultRank;
        this.scopes = scopes;
        this.icon = icon;
    }

    public Material getIcon() {
        return icon;
    }

    public String getLoredescription() {
        return loredescription;
    }

    public boolean isApplicableTo(PermissionScope scope){
        for(PermissionScope s : getScopes()){
            if (s==scope){
                return true;
            }
        }
        return false;
    }

    public PermissionScope[] getScopes() {
        return scopes;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TownRank getDefaultRank() {
        return defaultRank;
    }

    public static enum PermissionScope {
        TOWN,
        PLOT
    }
}
