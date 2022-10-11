package me.devvy.dodgebolt.map;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.map.DodgeboltArena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DodgeboltArenaDisappearingRing {

    private DodgeboltArena arena;
    private List<Location> blocksInRing = new ArrayList<>();
    private HashMap<Location, Material> oldBlackVal = new HashMap<Location, org.bukkit.Material>();
    private List<Location> soundLocations = new ArrayList<>();
    private boolean cleared = false;

    public DodgeboltArenaDisappearingRing(DodgeboltArena arena, List<Location> blocksInRing) {
        this.arena = arena;
        this.blocksInRing = blocksInRing;

        updateStoredBlocks();

        soundLocations.add(arena.getOrigin().clone());
        soundLocations.add(arena.getSpawn().clone());
        soundLocations.add(arena.getOrigin().clone().add(0, 0, -15));
        soundLocations.add(arena.getOrigin().clone().add(0, 0, 15));
        soundLocations.add(arena.getOrigin().clone().add(13, 0, 0));
        soundLocations.add(arena.getOrigin().clone().add(-13, 0, 0));

        soundLocations.add(arena.getOrigin().clone().add(13, 0, 15));
        soundLocations.add(arena.getOrigin().clone().add(-13, 0, 15));
        soundLocations.add(arena.getOrigin().clone().add(13, 0, -15));
        soundLocations.add(arena.getOrigin().clone().add(-13, 0, -15));
    }

    public void startFullDisappearingTask () {

        int SPEED = 5;

        new BukkitRunnable() {

            int runs = 0;
            int TARGET = 10;

            @Override
            public void run() {

                if (runs % 2 == 0)
                    flashRed();
                else
                    flashOld();

                runs++;

                if (runs > TARGET) {
                    cancel();
                    clear();
                }
            }
        }.runTaskTimer(Dodgebolt.getPlugin(Dodgebolt.class), 1, SPEED);

    }

    public void updateStoredBlocks () {
        for (Location location : blocksInRing)
            oldBlackVal.put(location, location.getBlock().getType());
        cleared = false;
    }

    public void playSound(Sound sound, float volume, float pitch) {
        for (Location location : soundLocations)
            location.getWorld().playSound(location, sound, volume, pitch);
    }

    public void flashRed() {
        for (Location location : blocksInRing) {
            location.clone().getBlock().setType(Material.RED_CARPET, false);
//            location.clone().add(0, -1, 0).getBlock().setType(Material.RED_WOOL, false);
        }

        playSound(Sound.BLOCK_LEVER_CLICK, 1, 1.4f);
    }

    public void flashOld() {

        for (Location location : blocksInRing) {
//            location.clone().add(0, -1, 0).getBlock().setType(Material.ICE, false);
            location.clone().getBlock().setType(oldBlackVal.get(location), false);
        }

        playSound(Sound.BLOCK_LEVER_CLICK, 1, 1.2f);
    }

    public void clear() {
        for (Location location : blocksInRing) {
            location.getBlock().setType(Material.AIR, false);
            location.clone().add(0, -1, 0).getBlock().setType(Material.AIR, false);
        }

        playSound(Sound.BLOCK_BEACON_DEACTIVATE, 1, .5f);
        playSound(Sound.ENTITY_ITEM_BREAK, 1, .5f);
        this.cleared = true;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }
}
