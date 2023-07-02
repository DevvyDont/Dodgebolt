package me.devvy.dodgebolt.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerHitDodgeboltArrowEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player shooter;
    private final Player victim;
    private final double damage;

    public PlayerHitDodgeboltArrowEvent(Player player, Player victim, double damage) {
        this.shooter = player;
        this.victim = victim;
        this.damage = damage;
    }

    public Player getShooter() {
        return shooter;
    }

    public Player getVictim() {
        return victim;
    }

    public double getDamage() {
        return damage;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
