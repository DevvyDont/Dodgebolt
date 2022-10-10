package me.devvy.dodgebolt.team;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.PlayerJoinTeamEvent;
import me.devvy.dodgebolt.events.PlayerLeaveTeamEvent;
import me.devvy.dodgebolt.events.TeamColorChangeEvent;
import me.devvy.dodgebolt.util.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Team {

    private String name;
    private ChatColor teamColor;
    private int score = 0;

    private Set<UUID> members = new HashSet<>();
    private TeamElimTracker elimTracker = new TeamElimTracker();

    public Team(ChatColor color) {
        this.name = ColorTranslator.chatColorToActualName(color);
        this.teamColor = color;
    }

    public String getName() {
        return name;
    }

    public ChatColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(ChatColor teamColor) {
        this.name = ColorTranslator.chatColorToActualName(teamColor);
        this.teamColor = teamColor;
        TeamColorChangeEvent event = new TeamColorChangeEvent(this, this.teamColor, teamColor);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);

        for (Player player : getMembersAsPlayers())
            player.setDisplayName(teamColor + player.getName());
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

        PlayerLeaveTeamEvent event = new PlayerLeaveTeamEvent(this, player);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);
    }

    public void clearPlayers() {
        for (Player p : this.getMembersAsPlayers())
            removePlayer(p);

        members.clear();
    }

    public void setScore(int score){
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public TeamElimTracker getElimTracker() {
        return elimTracker;
    }

    public void setElimTracker(TeamElimTracker elimTracker) {
        this.elimTracker = elimTracker;
    }
}
