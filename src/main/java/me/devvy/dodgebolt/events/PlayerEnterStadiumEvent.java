package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEnterStadiumEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DodgeboltGame game;
    private final Player player;

    public PlayerEnterStadiumEvent(DodgeboltGame game, Player player) {
        this.game = game;
        this.player = player;
    }

    public DodgeboltGame getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
