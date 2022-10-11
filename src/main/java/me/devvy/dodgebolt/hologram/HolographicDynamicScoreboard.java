package me.devvy.dodgebolt.hologram;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.game.GameStatisticsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * A couple of armor stands with nametags stacked on each other to make a scoreboard
 */
public class HolographicDynamicScoreboard {

    private final float Y_PADDING = .35F;

    private List<ArmorStand> entities = new ArrayList<>();
    private final Location location;
    private GameStatisticsManager gameStatisticsManager;
    private HashSet<UUID> playersToDisplay = new HashSet<>();

    public HolographicDynamicScoreboard(GameStatisticsManager gameStatisticsManager, Location location) {
        this.gameStatisticsManager = gameStatisticsManager;
        this.location = location;

        for (ArmorStand as : location.getWorld().getEntitiesByClass(ArmorStand.class))
            as.remove();
    }

    public void trackPlayer(UUID id) {
        playersToDisplay.add(id);
    }

    public void stopTrackingPlayer(UUID id) {
        playersToDisplay.remove(id);
    }

    public void clearPlayers() {
        playersToDisplay.clear();
    }

    private List<PlayerEntry> constructPlayerEntries() {
        List<PlayerEntry> entries = new ArrayList<>();

        for (UUID playerID : playersToDisplay)
            entries.add(gameStatisticsManager.getPlayerStatistics(playerID));

        return entries;
    }

    private ArmorStand spawnArmorStand(String name, int index) {
        float yOffset = index * Y_PADDING;

        ArmorStand as = location.getWorld().spawn(location.clone().add(0, 200, 0), ArmorStand.class);
        as.setVisible(false);
        as.setMarker(true);
        as.setCustomName(name);
        as.setCustomNameVisible(true);
        as.teleport(location.clone().subtract(0, yOffset, 0));

        return as;
    }

    public void update() {

        List<PlayerEntry> entries = constructPlayerEntries();
        entries.sort(Comparator.comparing(PlayerEntry::getKills).reversed());

        List<ArmorStand> newArmorStands = new ArrayList<>();

        int t1score = Dodgebolt.getInstance().getGame().getTeam1().getScore();
        ChatColor t1color = Dodgebolt.getInstance().getGame().getTeam1().getTeamColor();
        int t2score = Dodgebolt.getInstance().getGame().getTeam2().getScore();
        ChatColor t2color = Dodgebolt.getInstance().getGame().getTeam2().getTeamColor();
        newArmorStands.add(spawnArmorStand(String.format("%s%s%s %s%s- %s%s%s", t1color, ChatColor.BOLD, t1score, ChatColor.GRAY, ChatColor.BOLD, t2color, ChatColor.BOLD, t2score), -3));
        newArmorStands.add(spawnArmorStand(String.format("%-30s %18s", ChatColor.GRAY.toString() + ChatColor.BOLD + "Player", "K / D / ACC"), -1));

        // Now we have a sorted list of player stats, construct the holograms
        for (int i = 0; i < entries.size(); i++) {
            PlayerEntry entry = entries.get(i);
            newArmorStands.add(spawnArmorStand(String.format("%-32s %24s", entry.getPlayer().getDisplayName(), ChatColor.GRAY + entry.getStatString()), i));
        }

        List<ArmorStand> oldArmorStands = entities;
        new BukkitRunnable() {

            @Override
            public void run() {
                for (ArmorStand as : oldArmorStands)
                    as.remove();
            }

        }.runTaskLater(Dodgebolt.getInstance(), 5);
        entities = newArmorStands;

    }

    public void hide() {
        for (ArmorStand as : entities)
            as.remove();

        entities.clear();
    }

}
