package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.hologram.PlayerEntry;
import me.devvy.dodgebolt.team.Team;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A container that holds any stats that we may need to draw from to display on a holographic scoreboard
 * kills, deaths, accuracy, be able to deduce when an ace/clutch/flawless/team ace happens
 */
public class GameStatisticsManager {

    // Several data structures we will use to store stats across an entire map
    Map<UUID, Integer> playerToKillsMap = new HashMap<>();
    Map<UUID, Integer> playerToDeathsMap = new HashMap<>();
    Map<UUID, Integer> playerToArrowsFiredMap = new HashMap<>();

    Map<Integer, RoundStatistics> roundTimeline = new HashMap<>();

    /**
     * Setup for a new round given round number
     */
    public void newRound(int roundNumber) {
        roundTimeline.put(roundNumber, new RoundStatistics());
    }

    public RoundStatistics getRoundStatistics(int roundNumber) {
        return roundTimeline.get(roundNumber);
    }

    /**
     * Called when someone dies. killer and whoDied will be the same person if its a suicide
     * Teamkills can also be called here
     *
     * @param killer
     * @param whoDied
     */
    public void registerKill(Player killer, Player whoDied) {

        Team killerTeam = Dodgebolt.getInstance().getGame().getPlayerTeam(killer);
        Team diedTeam = Dodgebolt.getInstance().getGame().getPlayerTeam(whoDied);

        // Increment a kill to the person who got the kill only if they are on opposing teams
        if (Dodgebolt.getInstance().getGame().getOpposingTeam(killerTeam).equals(diedTeam))
            playerToKillsMap.put(killer.getUniqueId(), playerToKillsMap.getOrDefault(killer.getUniqueId(), 0) + 1);

        // Increment a death to the person who died
        playerToDeathsMap.put(whoDied.getUniqueId(), playerToDeathsMap.getOrDefault(whoDied.getUniqueId(), 0) + 1);

        getRoundStatistics(Dodgebolt.getInstance().getGame().getCurrentRoundNumber()).registerKill(killer, whoDied);

    }

    public void registerArrowShot(Player player) {
        playerToArrowsFiredMap.put(player.getUniqueId(), playerToArrowsFiredMap.getOrDefault(player.getUniqueId(), 0) + 1);
    }

    // Completely wipe all stats for a new game
    public void clear() {
        playerToKillsMap.clear();
        playerToArrowsFiredMap.clear();
        playerToDeathsMap.clear();
        roundTimeline.clear();
    }

    public PlayerEntry getPlayerStatistics(UUID uuid) {
        return new PlayerEntry(uuid,
                playerToKillsMap.getOrDefault(uuid, 0),
                playerToDeathsMap.getOrDefault(uuid, 0),
                playerToArrowsFiredMap.getOrDefault(uuid, 0));
    }


}
