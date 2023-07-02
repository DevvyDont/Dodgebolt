package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DodgeboltTeamAcedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Team team;

    public DodgeboltTeamAcedEvent(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
