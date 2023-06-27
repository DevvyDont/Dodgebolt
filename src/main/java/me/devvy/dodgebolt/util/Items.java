package me.devvy.dodgebolt.util;

import me.devvy.dodgebolt.Dodgebolt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

    public static ItemStack getInactiveBow() {
        ItemStack clay = new ItemStack(Material.CLAY_BALL);
        ItemMeta claymeta = clay.getItemMeta();
        claymeta.setDisplayName(ChatColor.RED + "Get behind the foul line!");
        clay.setItemMeta(claymeta);
        return clay;
    }

    public static boolean isBowItem(ItemStack item) {
        return item.getType().equals(Material.BOW) || item.getType().equals(Material.CLAY_BALL);
    }

    private static double getKDRatio(int kills, int deaths) {

        if (deaths == 0)
            deaths = 1;

        double percent = (double) kills / (double) deaths;
        return Math.round(percent * 100.0) / 100.0;
    }

    public static void equipWinnerCrown(Player player, Material helmetType, String name, String reason) {

        // These are wrapped in runnables to make sure the player is actually respawned before giving them the crown

        new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().respawn();
            }
        }.runTaskLater(Dodgebolt.getInstance(), 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                int wins = PlayerStats.getStatistic(player, PlayerStats.PLAYER_WINS);
                int rounds = PlayerStats.getStatistic(player, PlayerStats.PLAYER_ROUND_WINS);
                int kills = PlayerStats.getStatistic(player, PlayerStats.PLAYER_KILLS);
                int arrows = PlayerStats.getStatistic(player, PlayerStats.ARROWS_FIRED);
                int arrowsHit = PlayerStats.getStatistic(player, PlayerStats.ARROWS_HIT);
                float acc = arrows > 0 ? ((float)arrowsHit) / arrows : 0;
                int deaths = PlayerStats.getStatistic(player, PlayerStats.PLAYER_DEATHS);
                int mvps = PlayerStats.getStatistic(player, PlayerStats.MATCH_MVPS);
                int aces = PlayerStats.getStatistic(player, PlayerStats.ACES);
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date date = new Date();
                ItemStack crown = new ItemStack(helmetType);
                ItemMeta meta = crown.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "This crown belongs to " + player.getDisplayName() + ChatColor.GRAY + " for " + reason,
                        "",
                        ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() +  "Lifetime Stats",
                        ChatColor.GRAY + "Match Victories: " + ChatColor.YELLOW + wins,
                        ChatColor.GRAY + "Round Wins: " + ChatColor.YELLOW + rounds,
                        "",
                        ChatColor.GRAY + "Kills: " + ChatColor.GREEN + kills,
                        ChatColor.GRAY + "Deaths: " + ChatColor.RED + deaths,
                        ChatColor.GRAY + "K/D Ratio: " + ChatColor.GOLD + getKDRatio(kills, deaths),
                        "",
                        ChatColor.GRAY + "Arrows Fired: " + ChatColor.GREEN + arrows,
                        ChatColor.GRAY + "Accuracy: " + ChatColor.GREEN + Math.round(((acc) * 100) * 100.0) / 100.0 + "%",
                        "",
                        ChatColor.GRAY + ChatColor.BOLD.toString() + "Match MVPs: " + ChatColor.YELLOW + mvps,
                        ChatColor.GRAY + ChatColor.BOLD.toString() + "Aces: " + ChatColor.GOLD + aces,
                        "",
                        ChatColor.DARK_GRAY + "Crown created: " + ChatColor.ITALIC + formatter.format(date) + " EST"

                ));
                meta.addEnchant(Enchantment.DURABILITY, 10, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
                crown.setItemMeta(meta);
                player.getInventory().setHelmet(crown);
            }
        }.runTaskLater(Dodgebolt.getInstance(), 40);

    }

}
