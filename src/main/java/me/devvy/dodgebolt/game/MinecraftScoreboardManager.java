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
import org.bukkit.scoreboard.*;

public class MinecraftScoreboardManager implements Listener {

    private final DodgeboltGame game;

    // Scoreboard name hack shit
    Scoreboard scoreboard;
    org.bukkit.scoreboard.Team team1ScoreboardTeam;
    org.bukkit.scoreboard.Team team2ScoreboardTeam;
    org.bukkit.scoreboard.Team spectatorScoreboardTeam;
    org.bukkit.scoreboard.Team adminScoreboardTeam;

    org.bukkit.scoreboard.Team sidebarLineCurrRound;
    Score blankLine;
    org.bukkit.scoreboard.Team sidebarLineTeam1ScoreLabel;
    org.bukkit.scoreboard.Team sidebarLineTeam1Score;
    Score blankLine2;
    org.bukkit.scoreboard.Team sidebarLineTeam2ScoreLabel;
    org.bukkit.scoreboard.Team sidebarLineTeam2Score;

    Objective sidebar;

    public MinecraftScoreboardManager(DodgeboltGame game) {
        this.game = game;

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        team1ScoreboardTeam = scoreboard.registerNewTeam("one");
        team2ScoreboardTeam = scoreboard.registerNewTeam("two");
        spectatorScoreboardTeam = scoreboard.registerNewTeam("spec");
        adminScoreboardTeam = scoreboard.registerNewTeam("admin");

        team1ScoreboardTeam.setAllowFriendlyFire(true);
        team2ScoreboardTeam.setAllowFriendlyFire(true);

        spectatorScoreboardTeam.setPrefix(ChatColor.DARK_GRAY + "[SPEC] ");
        spectatorScoreboardTeam.setColor(ChatColor.GRAY);
        adminScoreboardTeam.setPrefix(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "ADMIN" + ChatColor.DARK_GRAY + "] ");
        adminScoreboardTeam.setColor(ChatColor.RED);

        sidebar = scoreboard.registerNewObjective("dummy", "dummy", ChatColor.AQUA + "Dodgebolt");

        sidebarLineCurrRound = scoreboard.registerNewTeam("CurrRound");
        sidebarLineCurrRound.addEntry(ChatColor.values()[6].toString());
        sidebar.getScore(ChatColor.values()[6].toString()).setScore(6);

        blankLine = sidebar.getScore(ChatColor.values()[5].toString());
        blankLine.setScore(5);

        sidebarLineTeam1ScoreLabel = scoreboard.registerNewTeam("Team1ScoreL");
        sidebarLineTeam1ScoreLabel.addEntry(ChatColor.values()[4].toString());
        sidebar.getScore(ChatColor.values()[4].toString()).setScore(4);

        sidebarLineTeam1Score = scoreboard.registerNewTeam("Team1Score");
        sidebarLineTeam1Score.addEntry(ChatColor.values()[3].toString());
        sidebar.getScore(ChatColor.values()[3].toString()).setScore(3);

        blankLine2 = sidebar.getScore(ChatColor.values()[2].toString());
        blankLine2.setScore(2);

        sidebarLineTeam2ScoreLabel = scoreboard.registerNewTeam("Team2ScoreL");
        sidebarLineTeam2ScoreLabel.addEntry(ChatColor.values()[1].toString());
        sidebar.getScore(ChatColor.values()[1].toString()).setScore(1);

        sidebarLineTeam2Score = scoreboard.registerNewTeam("Team2Score");
        sidebarLineTeam2Score.addEntry(ChatColor.values()[0].toString());
        sidebar.getScore(ChatColor.values()[0].toString()).setScore(0);


        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
            setSpectatingCosmetics(player);
        }

        delayedUpdate();

        new BukkitRunnable() {
            @Override
            public void run() {
                updateSidebar();
            }
        }.runTaskTimer(Dodgebolt.getPlugin(Dodgebolt.class), 5, 20);
    }

    /**
     * Gives a player the spectator tag as well as update their chat identity and colors
     *
     * @param player The player that wants spectating cosmetics
     */
    public void setSpectatingCosmetics(Player player) {

        if (player.isOp()) {
            adminScoreboardTeam.addEntry(player.getName());
            player.setDisplayName(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "ADMIN" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + ChatColor.stripColor(player.getName()));
        } else {
            spectatorScoreboardTeam.addEntry(player.getName());
            player.setDisplayName(ChatColor.DARK_GRAY + "[SPEC] " + ChatColor.GRAY + ChatColor.stripColor(player.getName()));
        }

    }

    private void delayedUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                team1ScoreboardTeam.setPrefix(ChatColor.GRAY + "[" + game.getTeam1().getTeamColor() + game.getTeam1().getName() + ChatColor.GRAY + "] ");
                team2ScoreboardTeam.setPrefix(ChatColor.GRAY + "[" + game.getTeam2().getTeamColor() + game.getTeam2().getName() + ChatColor.GRAY + "] ");
                team1ScoreboardTeam.setColor(game.getTeam1().getTeamColor());
                team2ScoreboardTeam.setColor(game.getTeam2().getTeamColor());

                sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

                updateSidebar();
            }
        }.runTaskLater(Dodgebolt.getPlugin(Dodgebolt.class), 20);
    }

    public void updateSidebar() {
        sidebarLineCurrRound.setPrefix(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Current Round: ");
        sidebarLineCurrRound.setSuffix(ChatColor.WHITE.toString() + (game.getState() == DodgeboltGameState.WAITING ? 0 : (Math.min(game.getTeam1().getScore() + game.getTeam2().getScore() + 1, game.getRoundsToWin() * 2 -2))) + "/" + (game.getRoundsToWin() * 2 - 2));
        sidebarLineTeam1ScoreLabel.setPrefix(game.getTeam1().getTeamColor() + teamColorToCallsign(game.getTeam1().getTeamColor()) + game.getTeam1().getName());
        sidebarLineTeam2ScoreLabel.setPrefix(game.getTeam2().getTeamColor() + teamColorToCallsign(game.getTeam2().getTeamColor()) + game.getTeam2().getName());

        String preSufTeam1 = getTeamScorePrefixSuffix(game.getTeam1());
        String preSufTeam2 = getTeamScorePrefixSuffix(game.getTeam2());
        sidebarLineTeam1Score.setPrefix(preSufTeam1);
        sidebarLineTeam2Score.setPrefix(preSufTeam2);
    }

    private String getTeamScorePrefixSuffix(me.devvy.dodgebolt.team.Team team) {

        if (game.getRoundsToWin() > 5)
            return team.getTeamColor().toString() + ChatColor.BOLD + team.getScore() + ChatColor.GRAY + " / " + ChatColor.WHITE + ChatColor.BOLD + game.getRoundsToWin();

        StringBuilder suffix = new StringBuilder(team.getScore() > 0 ? team.getTeamColor().toString() + ChatColor.BOLD : "");
        for (int i = 0; i < team.getScore(); i++)
            suffix.append("✕ ");

        if (team.getScore() < game.getRoundsToWin())
            suffix.append(ChatColor.GRAY).append(ChatColor.BOLD);

        for (int i = team.getScore(); i < game.getRoundsToWin(); i++)
            suffix.append("✕ ");

        return suffix.toString().trim();
    }

    private String teamColorToCallsign(ChatColor color) {

        switch (color) {

            case RED:
                return "♡ ";

            case BLUE:
                return "❃ ";

            case BLACK:
                return "♤ ";

            case AQUA:
                return "♢ ";

            case DARK_PURPLE:
                return "Ω ";

            case YELLOW:
                return "♕ ";

            case GOLD:
                return "☆ ";

            case GRAY:
                return "☁ ";

            case WHITE:
                return "☃ ";

            case GREEN:
                return "☺ ";

            case DARK_GRAY:
                return "✉ ";

            case DARK_GREEN:
                return "▽ ";

            case DARK_RED:
                return "☭ ";

            case LIGHT_PURPLE:
                return "♬ ";

            case DARK_BLUE:
                return "▽ ";

            case DARK_AQUA:
                return "₪ ";

            default:
                throw new IllegalArgumentException("not a color!");


        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(scoreboard);
        team1ScoreboardTeam.removeEntry(event.getPlayer().getName());
        team2ScoreboardTeam.removeEntry(event.getPlayer().getName());

        setSpectatingCosmetics(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeaveTeam(PlayerLeaveTeamEvent event) {

        team1ScoreboardTeam.removeEntry(event.getPlayer().getName());
        team2ScoreboardTeam.removeEntry(event.getPlayer().getName());

        setSpectatingCosmetics(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoinTeam(PlayerJoinTeamEvent event) {

        spectatorScoreboardTeam.removeEntry(event.getPlayer().getName());
        adminScoreboardTeam.removeEntry(event.getPlayer().getName());

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
