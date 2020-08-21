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

    private static double getKDRatio(int kills, int deaths) {

        if (deaths == 0)
            deaths = 1;

        double percent = (double) kills / (double) deaths;
        return Math.round(percent * 100.0) / 100.0;
    }

    public static void equipWinnerCrown(Player player) {
        int wins = PlayerStats.getPlayerWins(player);
        int rounds = PlayerStats.getPlayerRoundWins(player);
        int kills = PlayerStats.getPlayerKills(player);
        int arrows = PlayerStats.getArrowsFired(player);
        int deaths = PlayerStats.getPlayerDeaths(player);
        ItemStack crown = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = crown.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Winner's Crown");
        meta.setLore(Arrays.asList(
                "",
                ChatColor.GRAY + "This crown belongs to " + player.getDisplayName(),
                "",
                ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() +  "Lifetime Stats",
                ChatColor.AQUA + "Game Wins: " + ChatColor.YELLOW + wins,
                ChatColor.AQUA + "Round Wins: " + ChatColor.YELLOW + rounds,
                "",
                ChatColor.AQUA + "Players Killed: " + ChatColor.GREEN + kills,
                ChatColor.AQUA + "Deaths: " + ChatColor.RED + deaths,
                ChatColor.AQUA + "K/D Ratio: " + ChatColor.GOLD + getKDRatio(kills, deaths),
                "",
                ChatColor.AQUA + "Arrows Fired: " + ChatColor.GREEN + arrows,
                ChatColor.AQUA + "Accuracy: " + ChatColor.GREEN + Math.round((((float)kills / arrows) * 100) * 100.0) / 100.0 + "%"
        ));
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        crown.setItemMeta(meta);
        player.getInventory().setHelmet(crown);
    }

}
