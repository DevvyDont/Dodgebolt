package me.devvy.dodgebolt.team;

import org.bukkit.ChatColor;

public class Team {

    private final String name;
    private final ChatColor teamColor;

    public Team(String name, ChatColor color) {
        this.name = name;
        this.teamColor = color;
    }

    public String getName() {
        return name;
    }

    public ChatColor getTeamColor() {
        return teamColor;
    }
}
