package me.devvy.dodgebolt.hologram;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.statistics.GameStatisticsManager;
import me.devvy.dodgebolt.statistics.PlayerMatchStatContainer;
import me.devvy.dodgebolt.statistics.PlayerSessionStatContainer;
import me.devvy.dodgebolt.team.Team;
import me.devvy.dodgebolt.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * A couple of armor stands with nametags stacked on each other to make a scoreboard
 */
public class HolographicDynamicScoreboard {

    private final float Y_PADDING = .35F;

    private List<ArmorStand> entities = new ArrayList<>();
    private final Location location;
    private final GameStatisticsManager gameStatisticsManager;

    public HolographicDynamicScoreboard(GameStatisticsManager gameStatisticsManager, Location location) {
        this.gameStatisticsManager = gameStatisticsManager;
        this.location = location;
    }

    private ArmorStand spawnArmorStand(String name, int index) {
        float yOffset = index * Y_PADDING;

        ArmorStand as = location.getWorld().spawn(location.clone().add(0, 200, 0), ArmorStand.class);
        as.setVisible(false);
        as.setMarker(true);
        as.setCustomName(name);
        as.setCustomNameVisible(true);
        as.teleport(location.clone().subtract(0, yOffset, 0));
        Items.tagItemAsDodgeboltItem(as);
        return as;
    }

    private List<PlayerSessionStatContainer> getSessionPlayersPlaying() {
        // Gets players on session leaderboard and filters it based on people who are playing
        List<PlayerSessionStatContainer> players = new ArrayList<>(gameStatisticsManager.getAllSessionContainers());
        List<Player> activePlayers = new ArrayList<>(Dodgebolt.getInstance().getGame().getAllPlayersOnTeams());
        players.removeIf(container -> !activePlayers.contains(Bukkit.getPlayer(container.getOwner())));
        return players;
    }

    public void update(boolean shouldUseSessionStats) {

        List<? extends PlayerMatchStatContainer> matchStats;

        if (shouldUseSessionStats)
            matchStats = getSessionPlayersPlaying();
        else
            matchStats = new ArrayList<>(gameStatisticsManager.getCurrentMatchStatContainers());

        matchStats.sort(Comparator.comparing(PlayerMatchStatContainer::calculateScore).reversed());

        List<ArmorStand> newArmorStands = new ArrayList<>();

        int t1score = Dodgebolt.getInstance().getGame().getTeam1().getScore();
        ChatColor t1color = Dodgebolt.getInstance().getGame().getTeam1().getTeamColor();
        int t2score = Dodgebolt.getInstance().getGame().getTeam2().getScore();
        ChatColor t2color = Dodgebolt.getInstance().getGame().getTeam2().getTeamColor();

        // Spawn an armor stand that displays the round score
        newArmorStands.add(spawnArmorStand(String.format("%s%s%s %s%s- %s%s%s", t1color, ChatColor.BOLD, t1score, ChatColor.GRAY, ChatColor.BOLD, t2color, ChatColor.BOLD, t2score), -3));

        // Spawn an armor stand that displays scoreboard header
        // Space codes will be as follows: name:20 Score:5 K:2 D:2 A:2 ACC:3
        newArmorStands.add(spawnArmorStand(String.format("%-20s %5s / %2s / %2s / %2s / %3s%%", ChatColor.GRAY.toString() + ChatColor.BOLD + "Player", "Score", "K", "D", "A", "ACC"), -1));

        // Now we have a sorted list of player stats, construct the new holograms
        for (int i = 0; i < matchStats.size(); i++) {
            PlayerMatchStatContainer entry = matchStats.get(i);

            Player p = Bukkit.getPlayer(entry.getOwner());
            Team pTeam = null;

            if (p != null)
                pTeam = Dodgebolt.getInstance().getGame().getPlayerTeam(p);

            String displayname = pTeam != null ? pTeam.getCleanMemberString(entry.getName()) : ChatColor.DARK_GRAY + entry.getName();
            displayname += ChatColor.GRAY;
            newArmorStands.add(spawnArmorStand(String.format("%-20s %5s / %2s / %2s / %2s / %3s%%", displayname, entry.calculateScore(), entry.getKills(), entry.getDeaths(), entry.getAssists(), entry.getAccuracy()), i));
        }

        List<ArmorStand> oldArmorStands = entities;

        // Delete the old ones
        new BukkitRunnable() {

            @Override
            public void run() {
                for (ArmorStand as : oldArmorStands)
                    as.remove();
            }

        }.runTaskLater(Dodgebolt.getInstance(), 3);
        entities = newArmorStands;
    }

    public void delete() {
        Items.removeDodgeboltEntities(ArmorStand.class);
        entities.clear();
    }
}
