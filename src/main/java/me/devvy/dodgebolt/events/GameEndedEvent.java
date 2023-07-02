package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEndedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DodgeboltGame game;

    public GameEndedEvent(DodgeboltGame game) {
        this.game = game;
    }

    public DodgeboltGame getGame() {
        return game;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
