package me.devvy.dodgebolt.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerObtainedCrownEvent extends Event {

    public enum CrownType {
        MATCH_WIN,
        MATCH_MVP
    }

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final CrownType crown;

    public PlayerObtainedCrownEvent(Player player, CrownType type) {
        this.player = player;
        this.crown = type;
    }

    public Player getPlayer() {
        return player;
    }

    public CrownType getCrown() {
        return crown;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
