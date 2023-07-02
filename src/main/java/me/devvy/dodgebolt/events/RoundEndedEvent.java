package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RoundEndedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Team winner;
    private final Team loser;

    public RoundEndedEvent(Team winner, Team loser) {
        this.winner = winner;
        this.loser  = loser;
    }

    public Team getWinner() {
        return winner;
    }

    public Team getLoser() {
        return loser;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
