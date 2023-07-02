package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is ONLY called if a team wins a round based under normal conditions.
 * An ace did not occur, a flawless did not occur, nobody clutched, no team ace, etc etc
 */
public class DodgeboltTeamWonBasicRoundEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Team team;

    public DodgeboltTeamWonBasicRoundEvent(Team team) {
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
