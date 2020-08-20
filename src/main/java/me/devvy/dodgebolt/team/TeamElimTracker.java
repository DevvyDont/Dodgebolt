package me.devvy.dodgebolt.team;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

}
