package me.devvy.dodgebolt.util;

import me.devvy.dodgebolt.Dodgebolt;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class PlayerStats {

    private final static NamespacedKey PLAYER_WINS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_wins");
    private final static NamespacedKey PLAYER_ROUND_WINS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_round_wins");
    private final static NamespacedKey PLAYER_KILLS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_kills");
    private final static NamespacedKey PLAYER_DEATHS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_deaths");
    private final static NamespacedKey ARROWS_FIRED = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "arrows_fired");

    public static int addPlayerWins(Player player) {
        int old = player.getPersistentDataContainer().getOrDefault(PLAYER_WINS, PersistentDataType.INTEGER, 0);
        player.getPersistentDataContainer().set(PLAYER_WINS, PersistentDataType.INTEGER, old + 1);
        return old + 1;
    }

    public static int getPlayerWins(Player player) {
        return player.getPersistentDataContainer().getOrDefault(PLAYER_WINS, PersistentDataType.INTEGER, 0);
    }

    public static int addPlayerRoundWins(Player player) {
        int old = player.getPersistentDataContainer().getOrDefault(PLAYER_ROUND_WINS, PersistentDataType.INTEGER, 0);
        player.getPersistentDataContainer().set(PLAYER_ROUND_WINS, PersistentDataType.INTEGER, old + 1);
        return old + 1;
    }

    public static int getPlayerRoundWins(Player player) {
        return player.getPersistentDataContainer().getOrDefault(PLAYER_ROUND_WINS, PersistentDataType.INTEGER, 0);
    }

    public static int addPlayerKills(Player player) {
        int old = player.getPersistentDataContainer().getOrDefault(PLAYER_KILLS, PersistentDataType.INTEGER, 0);
        player.getPersistentDataContainer().set(PLAYER_KILLS, PersistentDataType.INTEGER, old + 1);
        return old + 1;
    }

    public static int getPlayerKills(Player player) {
        return player.getPersistentDataContainer().getOrDefault(PLAYER_KILLS, PersistentDataType.INTEGER, 0);
    }

    public static int addPlayerDeaths(Player player) {
        int old = player.getPersistentDataContainer().getOrDefault(PLAYER_DEATHS, PersistentDataType.INTEGER, 0);
        player.getPersistentDataContainer().set(PLAYER_DEATHS, PersistentDataType.INTEGER, old + 1);
        return old + 1;
    }

    public static int getPlayerDeaths(Player player) {
        return player.getPersistentDataContainer().getOrDefault(PLAYER_DEATHS, PersistentDataType.INTEGER, 0);
    }

    public static int addArrowsFired(Player player) {
        int old = player.getPersistentDataContainer().getOrDefault(ARROWS_FIRED, PersistentDataType.INTEGER, 0);
        player.getPersistentDataContainer().set(ARROWS_FIRED, PersistentDataType.INTEGER, old + 1);
        return old + 1;
    }

    public static int getArrowsFired(Player player) {
        return player.getPersistentDataContainer().getOrDefault(ARROWS_FIRED, PersistentDataType.INTEGER, 0);
    }

}
