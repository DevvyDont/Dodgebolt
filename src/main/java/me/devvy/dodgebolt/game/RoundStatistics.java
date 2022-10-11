package me.devvy.dodgebolt.game;


import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Keeps tracks of stats per round, this is what we will use to deduce if an ace happens etc
 */
public class RoundStatistics {

    // How many kills players have this round (suicides/teamkills do not count)
    Map<UUID, Integer> playerToKillsMap = new HashMap<>();

    public void registerKill(Player killer, Player whoDied) {

        if (killer.equals(whoDied) || Dodgebolt.getInstance().getGame().getPlayerTeam(killer) == Dodgebolt.getInstance().getGame().getPlayerTeam(whoDied))
            return;

        playerToKillsMap.put(killer.getUniqueId(), playerToKillsMap.getOrDefault(killer.getUniqueId(), 0) + 1);
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

            int kills = playerToKillsMap.getOrDefault(id, 0);

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

            int kills = playerToKillsMap.getOrDefault(player, 0);
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

        if (winningTeam.getMembers().size() < 3)
            return false;

        return winningTeam.getElimTracker().getTeamMembersAlive() == 1;
    }

}
