package me.devvy.dodgebolt.map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DodgeboltArenaOvertimeShatterTask extends BukkitRunnable {

    private final DodgeboltArena arena;
    private List<Location> decayLocations = new ArrayList<>();
    private HashMap<Location, Material> oldBlackVal = new HashMap<Location, org.bukkit.Material>();

    int currentIndex = 0;

    int currentTick = 0;

    public DodgeboltArenaOvertimeShatterTask(DodgeboltArena arena) {
        this.arena = arena;

        // Loop through all the blocks that don't get destroyed by the rings and add them to the list
        for (int x = -6; x <= 6; x++)
            for (int z = -8; z <= 8; z++) {
                Location loc = arena.getOrigin().clone().add(x, 0, z);

                // Skip the middle line
                if (z == 0)
                    continue;

                decayLocations.add(arena.getOrigin().clone().add(x, 0, z));
                oldBlackVal.put(loc, loc.getBlock().getType());
            }

        Collections.shuffle(decayLocations);
    }

    @Override
    public void run() {

        // If the index is > than however many blocks we have we finished
        if (currentIndex >= decayLocations.size()) {
            cancel();
            return;
        }

        Location currLoc = decayLocations.get(currentIndex);

        // Tick 0, 2, 4, 6, 8 flash red
        // Tick 1, 3, 5, 7, 9 flash back
        // Tick 10 disappear, go to next block
        if (currentTick == 10)
            breakBlock(currLoc);
        else if (currentTick > 10) {
            currentIndex++;
            currentTick = 0;
        }
        else if (currentTick % 2 == 0)
            flashRed(currLoc);
        else if (currentTick % 2 == 1)
            flashOld(currLoc);

        currentTick++;

    }

    public void playSound(Location location, Sound sound, float volume, float pitch) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    public void flashRed(Location location) {
        location.clone().getBlock().setType(Material.RED_CARPET, false);
        playSound(location, Sound.BLOCK_LEVER_CLICK, 1, 1.4f);
    }

    public void flashOld(Location location) {
        location.clone().getBlock().setType(oldBlackVal.get(location), false);
        playSound(location, Sound.BLOCK_LEVER_CLICK, 1, 1.2f);
    }

    public void breakBlock(Location location) {
        location.getBlock().setType(Material.AIR, false);
        location.clone().add(0, -1, 0).getBlock().setType(Material.AIR, false);
        playSound(location, Sound.BLOCK_GLASS_BREAK, 1, .5f);
    }
}
