package me.devvy.dodgebolt.util;

import me.devvy.dodgebolt.Dodgebolt;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class PlayerStats {

    public final static NamespacedKey PLAYER_WINS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_wins");
    public final static NamespacedKey PLAYER_ROUND_WINS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_round_wins");
    public final static NamespacedKey PLAYER_KILLS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_kills");
    public final static NamespacedKey PLAYER_DEATHS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "player_deaths");
    public final static NamespacedKey ARROWS_FIRED = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "arrows_fired");
    public final static NamespacedKey MATCH_MVPS = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "match_mvps");
    public static final NamespacedKey ACES = new NamespacedKey(Dodgebolt.getPlugin(Dodgebolt.class), "aces");

    public static int addStatistic(Player player, NamespacedKey statistic) {
        int old = player.getPersistentDataContainer().getOrDefault(statistic, PersistentDataType.INTEGER, 0);
        player.getPersistentDataContainer().set(statistic, PersistentDataType.INTEGER, old + 1);
        return old + 1;
    }

    public static int getStatistic(Player player, NamespacedKey statistic) {
        return player.getPersistentDataContainer().getOrDefault(statistic, PersistentDataType.INTEGER, 0);
    }

}
