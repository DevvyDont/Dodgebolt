package me.devvy.dodgebolt.events;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamColorChangeEvent extends Event {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private Team team;
    private ChatColor old;
    private ChatColor _new;

    public TeamColorChangeEvent(Team team, ChatColor old, ChatColor _new) {
        this.team = team;
        this.old = old;
        this._new = _new;
    }

    public ChatColor getNew() {
        return _new;
    }

    public void setNew(ChatColor _new) {
        this._new = _new;
    }

    public Team getTeam() {
        return team;
    }

    public ChatColor getOld() {
        return old;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
