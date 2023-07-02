package me.devvy.dodgebolt.util;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.statistics.PlayerGlobalStatContainer;
import me.devvy.dodgebolt.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Items {

    public static final NamespacedKey DODGEBOLT_ITEMSTACK_TAG = new NamespacedKey(Dodgebolt.getInstance(), "dodgebolt-itemstack");

    public static boolean itemIsFromDodgebolt(PersistentDataHolder item) {
        return item.getPersistentDataContainer().has(DODGEBOLT_ITEMSTACK_TAG);
    }

    public static void tagItemAsDodgeboltItem(PersistentDataHolder item) {
        item.getPersistentDataContainer().set(DODGEBOLT_ITEMSTACK_TAG, PersistentDataType.INTEGER, 1);
    }

    public static void tagItemAsDodgeboltItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(DODGEBOLT_ITEMSTACK_TAG, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
    }

    public static void removeDodgeboltEntities(Class<? extends Entity> clazz) {
        for (Entity entity : Dodgebolt.getInstance().getGame().getStadium().getSpawn().getWorld().getEntitiesByClass(clazz))
            if (itemIsFromDodgebolt(entity))
                entity.remove();
    }

    public static void clearDodgeboltItems(Player player) {
        for (ItemStack is : player.getInventory().getContents())
            if (is != null && is.hasItemMeta() && itemIsFromDodgebolt(is.getItemMeta()))
                is.setAmount(0);
    }

    public static ItemStack getDodgeboltBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setUnbreakable(true);
        bowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        bowMeta.setDisplayName(ChatColor.AQUA + "Dodgebolt Bow");
        bowMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        tagItemAsDodgeboltItem(bowMeta);
        bow.setItemMeta(bowMeta);
        return bow;
    }

    public static ItemStack getInactiveBow() {
        ItemStack clay = new ItemStack(Material.CLAY_BALL);
        ItemMeta claymeta = clay.getItemMeta();
        claymeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        claymeta.setDisplayName(ChatColor.RED + "Get behind the foul line!");
        claymeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        tagItemAsDodgeboltItem(claymeta);
        clay.setItemMeta(claymeta);
        return clay;
    }

    private static double getKDRatio(int kills, int deaths) {

        if (deaths == 0)
            deaths = 1;

        double percent = (double) kills / (double) deaths;
        return Math.round(percent * 100.0) / 100.0;
    }

    public static void equipWinnerCrown(Player player, Team team, Material helmetType, String name, String reason) {

        if (!Dodgebolt.getInstance().isEnabled())
            return;

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

                PlayerGlobalStatContainer container = Dodgebolt.getInstance().getGame().getGameStatisticsManager().getPlayerGlobalStats(player);
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date date = new Date();

                ItemStack crown = new ItemStack(helmetType);
                ItemMeta meta = crown.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "This crown belongs to " + team.getCleanMemberString(player) + ChatColor.GRAY + " for " + reason,
                        "",
                        ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() +  "Lifetime Stats",
                        ChatColor.GRAY + "Match Victories: " + ChatColor.YELLOW + container.getMatchesWon(),
                        ChatColor.GRAY + "Round Wins: " + ChatColor.YELLOW + container.getRoundsWon(),
                        "",
                        ChatColor.GRAY + "Kills: " + ChatColor.GREEN + container.getKills(),
                        ChatColor.GRAY + "Deaths: " + ChatColor.RED + container.getDeaths(),
                        ChatColor.GRAY + "K/D Ratio: " + ChatColor.GOLD + getKDRatio(container.getKills(), container.getDeaths()),
                        "",
                        ChatColor.GRAY + "Arrows Fired: " + ChatColor.GREEN + container.getArrowsFired(),
                        ChatColor.GRAY + "Accuracy: " + ChatColor.GREEN + container.getAccuracy() + "%",
                        "",
                        ChatColor.GRAY + ChatColor.BOLD.toString() + "Match MVPs: " + ChatColor.YELLOW + container.getMatchMVPs(),
                        ChatColor.GRAY + ChatColor.BOLD.toString() + "Aces: " + ChatColor.GOLD + container.getAces(),
                        "",
                        ChatColor.DARK_GRAY + "Crown created: " + ChatColor.ITALIC + formatter.format(date) + " EST"

                ));
                meta.addEnchant(Enchantment.DURABILITY, 10, true);
                meta.setUnbreakable(true);
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("dodgebolt-armor", 0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
                tagItemAsDodgeboltItem(meta);
                crown.setItemMeta(meta);
                player.getInventory().setHelmet(crown);
            }
        }.runTaskLater(Dodgebolt.getInstance(), 40);

    }

    public static ItemStack getBoots(Team team) {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootMeta.setColor(ColorTranslator.translateChatColorToColor(team.getTeamColor()));
        bootMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        bootMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        bootMeta.setUnbreakable(true);
        bootMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("dodgebolt-armor", 0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        bootMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        bootMeta.setDisplayName(team.getTeamColor() + team.getName() + " Boots");
        tagItemAsDodgeboltItem(bootMeta);

        boots.setItemMeta(bootMeta);
        return boots;
    }
}
