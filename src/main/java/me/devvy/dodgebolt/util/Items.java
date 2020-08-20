package me.devvy.dodgebolt.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Items {

    public static ItemStack getDodgeboltBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setUnbreakable(true);
        bowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        bowMeta.setDisplayName(ChatColor.AQUA + "Dodgebolt Bow");
        bow.setItemMeta(bowMeta);
        return bow;
    }

    public static void equipWinnerCrown(Player player) {
        int wins = PlayerStats.getPlayerWins(player);
        int rounds = PlayerStats.getPlayerRoundWins(player);
        int kills = PlayerStats.getPlayerKills(player);
        ItemStack crown = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = crown.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Winner's Crown");
        meta.setLore(Arrays.asList(
                "",
                ChatColor.GRAY + "This crown belongs to " + player.getDisplayName(),
                "",
                ChatColor.AQUA + "Lifetime Wins: " + ChatColor.YELLOW + wins,
                ChatColor.AQUA + "Lifetime Kills: " + ChatColor.YELLOW + kills,
                ChatColor.AQUA + "Rounds Won: " + ChatColor.YELLOW + rounds
        ));
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        crown.setItemMeta(meta);
        player.getInventory().setHelmet(crown);
    }

}
