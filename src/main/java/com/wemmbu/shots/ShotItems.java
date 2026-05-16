package com.wemmbu.shots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ShotItems {

    public static final String NUKE_SHOT_KEY   = "WEMMBU_NUKE_SHOT";
    public static final String STAB_SHOT_KEY   = "WEMMBU_STAB_SHOT";

    /**
     * Nuke Shot — a fishing rod. Cast it; when the hook lands the nuke detonates.
     */
    public static ItemStack createNukeShot() {
        ItemStack item = new ItemStack(Material.FISHING_ROD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "☢ Nuke Shot");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Cast it — where the hook lands,",
                ChatColor.RED  + "a MASSIVE nuclear explosion erupts!",
                ChatColor.DARK_GRAY + "[" + NUKE_SHOT_KEY + "]"
        ));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Stab Shot — a fishing rod. Cast it; where the hook lands a shaft is bored to bedrock.
     */
    public static ItemStack createStabShot() {
        ItemStack item = new ItemStack(Material.FISHING_ROD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "⚫ Stab Shot");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Cast it — where the hook lands,",
                ChatColor.AQUA + "a void shaft bores straight to bedrock!",
                ChatColor.DARK_GRAY + "[" + STAB_SHOT_KEY + "]"
        ));
        item.setItemMeta(meta);
        return item;
    }

    /** Returns true if the given ItemStack is a Nuke Shot. */
    public static boolean isNukeShot(ItemStack item) {
        return hasTag(item, NUKE_SHOT_KEY);
    }

    /** Returns true if the given ItemStack is a Stab Shot. */
    public static boolean isStabShot(ItemStack item) {
        return hasTag(item, STAB_SHOT_KEY);
    }

    private static boolean hasTag(ItemStack item, String tag) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        for (String line : meta.getLore()) {
            if (ChatColor.stripColor(line).equals("[" + tag + "]")) return true;
        }
        return false;
    }
}
