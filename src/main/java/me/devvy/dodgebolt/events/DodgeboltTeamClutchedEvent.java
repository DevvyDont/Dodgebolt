package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DodgeboltTeamClutchedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Team team;

    public DodgeboltTeamClutchedEvent(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public Player getClutchPlayer() {
        return team.getPlayersAlive().iterator().next();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
