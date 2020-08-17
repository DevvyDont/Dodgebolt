package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.PlayerJoinTeamEvent;
import me.devvy.dodgebolt.events.PlayerLeaveTeamEvent;
import me.devvy.dodgebolt.events.TeamColorChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;

public class MinecraftScoreboardManager implements Listener {

    private final DodgeboltGame game;

    // Scoreboard name hack shit
    Scoreboard scoreboard;
    org.bukkit.scoreboard.Team team1ScoreboardTeam;
    org.bukkit.scoreboard.Team team2ScoreboardTeam;
    org.bukkit.scoreboard.Team spectatorScoreboardTeam;

    public MinecraftScoreboardManager(DodgeboltGame game) {
        this.game = game;

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        team1ScoreboardTeam = scoreboard.registerNewTeam("one");
        team1ScoreboardTeam.setPrefix(ChatColor.BLUE.toString());
        team2ScoreboardTeam = scoreboard.registerNewTeam("two");
        team1ScoreboardTeam.setPrefix(ChatColor.GOLD.toString());
        spectatorScoreboardTeam = scoreboard.registerNewTeam("spec");
        spectatorScoreboardTeam.setPrefix(ChatColor.GRAY + "[SPEC] ");

        team1ScoreboardTeam.setColor(game.getTeam1().getTeamColor());
        team1ScoreboardTeam.setColor(game.getTeam2().getTeamColor());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
            spectatorScoreboardTeam.addEntry(player.getName());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(scoreboard);
    }

    @EventHandler
    public void onPlayerLeaveTeam(PlayerLeaveTeamEvent event) {
        team1ScoreboardTeam.removeEntry(event.getPlayer().getName());
        team2ScoreboardTeam.removeEntry(event.getPlayer().getName());
        spectatorScoreboardTeam.addEntry(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerJoinTeam(PlayerJoinTeamEvent event) {
        spectatorScoreboardTeam.removeEntry(event.getPlayer().getName());
        if (event.getTeam() == game.getTeam1())
            team1ScoreboardTeam.addEntry(event.getPlayer().getName());
        else if (event.getTeam() == game.getTeam2())
            team2ScoreboardTeam.addEntry(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeamChangedColor(TeamColorChangeEvent event) {

        org.bukkit.scoreboard.Team scoreboardTeam = event.getTeam() == game.getTeam1() ? team1ScoreboardTeam : team2ScoreboardTeam;

        scoreboardTeam.setPrefix(event.getNew().toString());
        scoreboardTeam.setColor(event.getNew());
    }
}
