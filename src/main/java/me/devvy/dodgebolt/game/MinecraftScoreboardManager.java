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
import org.bukkit.scheduler.BukkitRunnable;
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
        team2ScoreboardTeam = scoreboard.registerNewTeam("two");
        spectatorScoreboardTeam = scoreboard.registerNewTeam("spec");

        team1ScoreboardTeam.setAllowFriendlyFire(false);
        team2ScoreboardTeam.setAllowFriendlyFire(false);

        spectatorScoreboardTeam.setPrefix(ChatColor.GRAY + "[SPEC] ");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
            spectatorScoreboardTeam.addEntry(player.getName());
        }

        delayedUpdate();
    }

    private void delayedUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                team1ScoreboardTeam.setPrefix(ChatColor.GRAY + "[" + game.getTeam1().getTeamColor() + game.getTeam1().getName() + ChatColor.GRAY + "] ");
                team2ScoreboardTeam.setPrefix(ChatColor.GRAY + "[" + game.getTeam2().getTeamColor() + game.getTeam2().getName() + ChatColor.GRAY + "] ");
                team1ScoreboardTeam.setColor(game.getTeam1().getTeamColor());
                team2ScoreboardTeam.setColor(game.getTeam2().getTeamColor());
            }
        }.runTaskLater(Dodgebolt.getPlugin(Dodgebolt.class), 20);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(scoreboard);
        team1ScoreboardTeam.removeEntry(event.getPlayer().getName());
        team2ScoreboardTeam.removeEntry(event.getPlayer().getName());
        spectatorScoreboardTeam.addEntry(event.getPlayer().getName());
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

        scoreboardTeam.setColor(event.getNew());
        scoreboardTeam.setPrefix(ChatColor.GRAY + "[" + event.getNew() + event.getTeam().getName() + ChatColor.GRAY + "] ");
    }
}
