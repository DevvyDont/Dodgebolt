package me.devvy.dodgebolt.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerFiredDodgeboltArrowEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player shooter;

    public PlayerFiredDodgeboltArrowEvent(Player player) {
        this.shooter = player;
    }

    public Player getShooter() {
        return shooter;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
