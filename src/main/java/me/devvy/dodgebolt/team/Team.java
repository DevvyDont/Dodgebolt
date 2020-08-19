package me.devvy.dodgebolt.team;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.PlayerJoinTeamEvent;
import me.devvy.dodgebolt.events.PlayerLeaveTeamEvent;
import me.devvy.dodgebolt.events.TeamColorChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Team {

    public static final int CAPACITY = 4;

    private final String name;
    private ChatColor teamColor;
    private int score = 0;

    private Set<UUID> members = new HashSet<>();

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

    public void setTeamColor(ChatColor teamColor) {
        TeamColorChangeEvent event = new TeamColorChangeEvent(this, this.teamColor, teamColor);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        this.teamColor = teamColor;
    }

    public Collection<UUID> getMembers() {
        return members;
    }

    public Collection<Player> getMembersAsPlayers() {
        ArrayList<Player> buffer = new ArrayList<>();
        for (UUID id : members)
            if (Bukkit.getPlayer(id) != null)
                buffer.add(Bukkit.getPlayer(id));
        return buffer;
    }

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public void addPlayer(Player player) {
        if (!members.add(player.getUniqueId()))
            return;

        player.setDisplayName(teamColor + player.getName());
        PlayerJoinTeamEvent event = new PlayerJoinTeamEvent(this, player);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);
    }

    public void removePlayer(Player player) {
        if (!members.remove(player.getUniqueId()))
            return;

        player.setDisplayName(ChatColor.DARK_GRAY + "[SPEC] " + ChatColor.stripColor(player.getName()));
        PlayerLeaveTeamEvent event = new PlayerLeaveTeamEvent(this, player);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);
    }

    public boolean isFull() {
        return members.size() >= CAPACITY;
    }

    public void setScore(int score){
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
