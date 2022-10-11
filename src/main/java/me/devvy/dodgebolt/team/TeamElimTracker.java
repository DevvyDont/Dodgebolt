package me.devvy.dodgebolt.team;

import org.bukkit.entity.Player;

import java.util.*;

public class TeamElimTracker {

    private int deathsNeeded = 0;
    private Set<UUID> deadPlayers = new HashSet<>();

    public boolean teamMemberDied(Player player) {
        return deadPlayers.add(player.getUniqueId());
    }

    public void reset(int deathsNeeded) {
        this.deathsNeeded = deathsNeeded;
        deadPlayers.clear();
    }

    public boolean teamIsDead() {
        return deadPlayers.size() >= deathsNeeded;
    }

    public int getTeamMembersAlive() {
        return deathsNeeded - deadPlayers.size();
    }

    public boolean nobodyIsDead() {
        return deadPlayers.isEmpty();
    }

    public boolean isDead(Player player) {
        return deadPlayers.contains(player.getUniqueId());
    }
}
