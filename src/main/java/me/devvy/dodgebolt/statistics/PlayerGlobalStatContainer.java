package me.devvy.dodgebolt.statistics;


import me.devvy.dodgebolt.Dodgebolt;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

/**
 * Stores the same things as the session container, but adds functionality to store/load using persistent data containers
 */
public class PlayerGlobalStatContainer extends PlayerSessionStatContainer {

    public static final NamespacedKey PERSISTENT_DATA_KEY = new NamespacedKey(Dodgebolt.getInstance(), "global-stats");

    public static PlayerGlobalStatContainer fromPersistentDataContainer(Player player) {

        PlayerGlobalStatContainer stats = new PlayerGlobalStatContainer(player.getUniqueId());
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (!container.has(PERSISTENT_DATA_KEY))
            return stats;

        int[] data = container.get(PERSISTENT_DATA_KEY, PersistentDataType.INTEGER_ARRAY);

        // Make sure the length of array read is at least the size of the data we are about to read
        assert data != null;
        if (data.length < 16)
            data = Arrays.copyOf(data, 16);

        // This is the order we chose to define how stats are stored, if we ever add new
        // stats then be sure to always add the value at the end
        stats.damageDealt = data[0];
        stats.kills = data[1];
        stats.teamKills = data[2];
        stats.assists = data[3];
        stats.deaths = data[4];
        stats.arrowsFired = data[5];
        stats.arrowsLanded = data[6];
        stats.roundsPlayed = data[7];
        stats.roundsWon = data[8];
        stats.aces = data[9];
        stats.clutches = data[10];
        stats.flawlesses = data[11];
        stats.matchesPlayed = data[12];
        stats.matchesWon = data[13];
        stats.crownsAchieved = data[14];
        stats.matchMVPs = data[15];
        // End of initial implementation batch

        return stats;
    }

    /**
     * Used to prohibit creation of this class from other files
     */
    private PlayerGlobalStatContainer(UUID playerID) {
        super(playerID);
    }

    public void saveToPersistentDataContainer(PersistentDataContainer container) {

        int[] data = {
                getDamageDealt(),    // 0
                getKills(),          // 1
                getTeamKills(),      // 2
                getAssists(),        // 3
                getDeaths(),         // 4
                getArrowsFired(),    // 5
                getArrowsLanded(),   // 6
                getRoundsPlayed(),   // 7
                getRoundsWon(),      // 8
                getAces(),           // 9
                getClutches(),       // 10
                getFlawlesses(),     // 11
                getMatchesPlayed(),  // 12
                getMatchesWon(),     // 13
                getCrownsAchieved(), // 14
                getMatchMVPs(),      // 15
        };

        container.set(PERSISTENT_DATA_KEY, PersistentDataType.INTEGER_ARRAY, data);
    }

    @Override
    public void save(Player player) {
        saveToPersistentDataContainer(player.getPersistentDataContainer());
    }
}
