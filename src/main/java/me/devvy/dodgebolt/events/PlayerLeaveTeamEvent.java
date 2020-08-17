package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeaveTeamEvent extends Event {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final Team team;
    private final Player player;

    public PlayerLeaveTeamEvent(Team team, Player player) {
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
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }


}
