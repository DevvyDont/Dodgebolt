package me.devvy.dodgebolt.statistics;


import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Keeps tracks of stats per round, this is what we will use to deduce if an ace happens etc
 */
public class RoundStatistics {

    // Keeps track of all general stats for only this round
    Map<UUID, PlayerMatchStatContainer> thisRoundStatContainer = new HashMap<>();

    // We also need to keep track of who has dealt damage to everyone so we can award assists properly
    Map<UUID, Set<UUID>> receivedDamageFromMap = new HashMap<>();

    public PlayerMatchStatContainer getRoundPlayerStats(UUID player) {

        // Already exists, return it
        if (thisRoundStatContainer.containsKey(player))
            return thisRoundStatContainer.get(player);

        // Create a new entry for the player and recursively call code above this
        thisRoundStatContainer.put(player, new PlayerMatchStatContainer(player));
        return getRoundPlayerStats(player);
    }

    /**
     * Call to register a player being eligible for an assist
     *
     * @param damager
     * @param victim
     */
    public void registerEligibleForAssist(Player damager, Player victim) {

        // Ensure a list is present
        receivedDamageFromMap.computeIfAbsent(victim.getUniqueId(), k -> new HashSet<>());
        receivedDamageFromMap.get(victim.getUniqueId()).add(damager.getUniqueId());
    }

    /**
     * Returns a list of players that contributed to a player's death
     *
     * @param victim
     * @return
     */
    public Collection<UUID> getPlayersEligibleForAssist(Player victim) {
        return receivedDamageFromMap.getOrDefault(victim.getUniqueId(), Collections.emptySet());
    }

    // Look for special cases where a player aced, returns null if it didn't happen
    public Player getWhoAced(Team winningTeam) {

        Team opposingTeam = Dodgebolt.getInstance().getGame().getOpposingTeam(winningTeam);

        // Only look for this case if the team that died has a size of at least 3
        if (opposingTeam.getMembers().size() <= 2)
            return null;

        int killsRequiredForAce = opposingTeam.getMembers().size();


        // Loop through all players on the team, if any of them get kills == to opposition size they get the ace
        for (UUID id : winningTeam.getMembers()) {

            Player player = Bukkit.getPlayer(id);
            if (player == null)
                return null;

            int kills = getRoundPlayerStats(player.getUniqueId()).getKills();

            // If they got a lot of kills then it's an ace
            if (kills >= killsRequiredForAce)
                return player;
        }

        // Nobody aced
        return null;
    }

    // Look for special case where everyone on a team got 1 kill
    public boolean teamAced(Team winningTeam) {

        // Again only consider a team size of at least 3
        if (winningTeam.getMembers().size() < 3)
            return false;

        // A team ace only occurs when everyone gets at least 1 kill
        boolean aceSatisfied = true;

        // Loop through all members and check for a kill
        for (UUID player : winningTeam.getMembers()) {

            int kills = getRoundPlayerStats(player).getKills();
            if (kills <= 0)
                aceSatisfied = false;

        }

        return aceSatisfied;
    }

    // Look for special case where a team killed the other team without dying
    public boolean teamFlawlessed(Team winningTeam) {

        // Only consider if the team has at least 2 people
        if (winningTeam.getMembers().size() < 2)
            return false;

        // If the elim tracker is still empty and we won then its a flawless
        return winningTeam.getElimTracker().nobodyIsDead();
    }

    // Look for someone who won the round as last alive on a team size of at least 3
    public boolean teamClutched(Team winningTeam) {

        // Opposing team needs at least 3 players and we need to be last alive
        if (Dodgebolt.getInstance().getGame().getOpposingTeam(winningTeam).getMembers().size() < 3)
            return false;

        return winningTeam.getElimTracker().getTeamMembersAlive() == 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (UUID id : thisRoundStatContainer.keySet())
            sb.append(String.format("%s - %sk\n", Bukkit.getPlayer(id).getName(), thisRoundStatContainer.get(id)));

        return sb.toString();
    }
}
