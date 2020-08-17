package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJoinTeamEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Team team;
    private final Player player;

    public PlayerJoinTeamEvent(Team team, Player player) {
        this.team = team;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
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
