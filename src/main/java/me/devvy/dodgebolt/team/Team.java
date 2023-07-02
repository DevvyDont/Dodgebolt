package me.devvy.dodgebolt.team;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.PlayerJoinTeamEvent;
import me.devvy.dodgebolt.events.PlayerLeaveTeamEvent;
import me.devvy.dodgebolt.events.TeamColorChangeEvent;
import me.devvy.dodgebolt.util.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Cat;
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

    public String getColoredName() {
        return getTeamColor() + getName();
    }

    public ChatColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(ChatColor teamColor) {
        this.name = ColorTranslator.chatColorToActualName(teamColor);
        this.teamColor = teamColor;
        TeamColorChangeEvent event = new TeamColorChangeEvent(this, this.teamColor, teamColor);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);
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

    public Collection<Player> getPlayersAlive() {
        List<Player> alive = new ArrayList<>();

        for (Player member : getMembersAsPlayers())
            if (!elimTracker.isDead(member))
                alive.add(member);

        return alive;
    }

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public void addPlayer(Player player) {
        if (!members.add(player.getUniqueId()))
            return;

        PlayerJoinTeamEvent event = new PlayerJoinTeamEvent(this, player);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);
        Dodgebolt.getInstance().getGame().broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + getCleanMemberString(player) + ChatColor.GREEN + " joined the " + getColoredName() + " team" + ChatColor.GREEN + "!");
    }

    public void removePlayer(Player player) {
        if (!members.remove(player.getUniqueId()))
            return;

        PlayerLeaveTeamEvent event = new PlayerLeaveTeamEvent(this, player);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().callEvent(event);

        Dodgebolt.getInstance().getGame().broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + getCleanMemberString(player) + ChatColor.RED + " left the " + getColoredName() + " team" + ChatColor.RED + "!");
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

    /**
     * Sends a title to all players on this team, similarly to just calling it on a player
     *
     * @param title
     * @param subtitle
     * @param fadeIn
     * @param stay
     * @param fadeOut
     */
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : getMembersAsPlayers())
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        for (Player player : getMembersAsPlayers())
            player.playSound(player.getEyeLocation(), sound, volume, pitch);
    }

    /**
     * Cleans a player name to be just their displayname colored as their team
     *
     * @param player
     * @return
     */
    public String getCleanMemberString(Player player) {
        return getCleanMemberString(player.getName());
    }

    public String getCleanMemberString(String name) {
        return teamColor + ChatColor.stripColor(name);
    }

    /**
     *
     * @return a string that contains player names formatted for clean printing in a string
     */
    public String getCleanMembersString() {

        List<Player> players = new ArrayList<>(getMembersAsPlayers());

        // If nobody is on this team just display it as their color
        if (players.isEmpty())
            return teamColor + name;

        // If 1 person is on this team, just return them
        if (players.size() == 1)
            return getCleanMemberString(players.get(0));

        // If 2 people are on this team, connect them with just and
        if (players.size() == 2)
            return getCleanMemberString(players.get(0)) + ChatColor.GRAY + " and " + getCleanMemberString(players.get(1));

        // Do a comma separated list with the last one being connected with and
        StringBuilder sb = new StringBuilder();
        // Iterate as normal but don't process last player
        for (int i = 0; i < players.size()-1; i++) {
            sb.append(getCleanMemberString(players.get(i))).append(ChatColor.GRAY).append(", ");
        }

        // For last player add the and
        sb.append(ChatColor.GRAY + "and ").append(getCleanMemberString(players.get(players.size() - 1)));
        return sb.toString();

    }
}
