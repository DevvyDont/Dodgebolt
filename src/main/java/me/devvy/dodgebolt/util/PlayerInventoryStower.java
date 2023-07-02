package me.devvy.dodgebolt.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInventoryStower {

    static class PlayerInventorySnapshot {

        ItemStack[] contents;
        ItemStack[] extraContents;
        ItemStack[] armorContents;

        public PlayerInventorySnapshot(PlayerInventory inventory) {
            contents = inventory.getContents();
            extraContents = inventory.getExtraContents();
            armorContents = inventory.getArmorContents();
        }

        public void restore(Player player) {
            player.getInventory().setContents(contents);
            player.getInventory().setExtraContents(extraContents);
            player.getInventory().setArmorContents(armorContents);
        }
    }

    Map<UUID, PlayerInventorySnapshot> inventoryStorage = new HashMap<>();

    public void storeInventory(Player player) {
        if (inventoryStorage.containsKey(player.getUniqueId()))
            throw new IllegalStateException("Player " + player.getName() + " cannot have more than one stowed inventory at a time!");

        inventoryStorage.put(player.getUniqueId(), new PlayerInventorySnapshot(player.getInventory()));
        player.getInventory().clear();
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] " + ChatColor.YELLOW + "Your inventory has been stowed, don't worry your items are not lost!");
    }

    public void restoreInventory(Player player) {

        if (!inventoryStorage.containsKey(player.getUniqueId()))
            throw new IllegalStateException("Had no inventory stowed for " + player.getName() + "!");

        inventoryStorage.get(player.getUniqueId()).restore(player);
        inventoryStorage.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Your inventory has been restored!");
    }

    public void restoreAll() {
        // If we manage our players correctly, then a player will never be null (we restore inventories on logout)
        for (UUID id : inventoryStorage.keySet())
            inventoryStorage.get(id).restore(Bukkit.getPlayer(id));
    }

}
