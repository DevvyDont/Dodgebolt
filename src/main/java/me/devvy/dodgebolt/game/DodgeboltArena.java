package me.devvy.dodgebolt.game;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.util.ColorTranslator;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DodgeboltArena {


    private final Location origin;
    private final Location spawn;  // Used for knowing where to spawn spectators and tp ppl that died
    private boolean generated = false;

    public static final int X_ARENA_RADIUS = 15;  // From center to side walls
    public static final int Z_ARENA_RADIUS = 17;  // From center to back walls

    private Material currentTeamOneColor = Material.BLUE_CARPET;
    private Material currentTeamTwoColor = Material.ORANGE_CARPET;

    private List<DodgeboltArenaDisappearingRing> rings = new ArrayList<>();

    private int currentRing = 0;

    public DodgeboltArena(Location origin) {
        this.origin = origin;
        this.spawn = origin.clone().add(0, 5, 0);
        this.spawn.setYaw(90);

        this.origin.getWorld().setDifficulty(Difficulty.PEACEFUL);

        ArrayList<Location> ring1Locs = new ArrayList<>();
        ArrayList<Location> ring2locs = new ArrayList<>();
        ArrayList<Location> ring3locs = new ArrayList<>();
        ArrayList<Location> ring4locs = new ArrayList<>();
        ArrayList<Location> ring5locs = new ArrayList<>();
        ArrayList<Location> ring6locs = new ArrayList<>();

        for (int x = -14; x <= 14; x++) {
            for (int z = -16; z <= 16; z++) {

                if (Math.abs(x) <= 6 && Math.abs(z) <= 8)
                    continue;

                // z >= 15 is the back 2 rows
                if (Math.abs(z) >= 15)
                    ring1Locs.add(origin.clone().add(x, 0, z));
                // x >= 13 is the far 2 right and left cols
                else if (Math.abs(x) >= 13)
                    ring1Locs.add(origin.clone().add(x, 0, z));
                // z >= 13 is the next 2 rows
                else if (Math.abs(z) >= 13)
                    ring2locs.add(origin.clone().add(x, 0, z));
                    // x >= 11 is the next far 2 right and left cols
                else if (Math.abs(x) >= 11)
                    ring2locs.add(origin.clone().add(x, 0, z));
                    // z >= 12 is the next 2 rows
                else if (Math.abs(z) >= 12)
                    ring3locs.add(origin.clone().add(x, 0, z));
                    // x >= 10 is the next far 2 right and left cols
                else if (Math.abs(x) >= 10)
                    ring3locs.add(origin.clone().add(x, 0, z));
                    // z >= 11 is the next 1 rows
                else if (Math.abs(z) >= 11)
                    ring4locs.add(origin.clone().add(x, 0, z));
                    // x >= 9 is the next far 2 right and left cols
                else if (Math.abs(x) >= 9)
                    ring4locs.add(origin.clone().add(x, 0, z));
                    // z >= 10 is the next 1 rows
                else if (Math.abs(z) >= 10)
                    ring5locs.add(origin.clone().add(x, 0, z));
                    // x >= 8 is the next far 2 right and left cols
                else if (Math.abs(x) >= 8)
                    ring5locs.add(origin.clone().add(x, 0, z));
                    // z >= 9 is the next 1 rows
                else if (Math.abs(z) >= 9)
                    ring6locs.add(origin.clone().add(x, 0, z));
                    // x >= 7 is the next far 2 right and left cols
                else if (Math.abs(x) >= 7)
                    ring6locs.add(origin.clone().add(x, 0, z));
            }
        }

        rings.add(new DodgeboltArenaDisappearingRing(this, ring1Locs));
        rings.add(new DodgeboltArenaDisappearingRing(this, ring2locs));
        rings.add(new DodgeboltArenaDisappearingRing(this, ring3locs));
        rings.add(new DodgeboltArenaDisappearingRing(this, ring4locs));
        rings.add(new DodgeboltArenaDisappearingRing(this, ring5locs));
        rings.add(new DodgeboltArenaDisappearingRing(this, ring6locs));
    }

    public boolean isGenerated() {
        return generated;
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getSpawn() {
        return spawn.clone();
    }

    public void generateArena() {

        currentTeamOneColor = Material.BLUE_CARPET;
        currentTeamTwoColor = Material.ORANGE_CARPET;

        Clipboard clipboard;

        File file = new File(Dodgebolt.getPlugin(Dodgebolt.class).getDataFolder().getParent() + "/WorldEdit/schematics/dodgebolt_arena.schem");
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        if (format == null)
            throw new IllegalStateException("Could not find the arena schematic!!!");

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        assert clipboard != null;

        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(origin.getWorld()), -1)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(session)
                    .to(BlockVector3.at(origin.getX(), origin.getY(), origin.getZ()))
                    .build();
            Operations.complete(operation);

        } catch (WorldEditException e) {
            Dodgebolt.getPlugin(Dodgebolt.class).getLogger().severe("Could not generate the arena!");
            e.printStackTrace();
            return;
        }

        this.generated = true;
        currentRing = 0;

    }

    /**
     * Used to reload the arena back to what it is, used for in between rounds
     */
    public void restoreArena() {
        Material tempTeamOneColor = currentTeamOneColor;
        Material tempTeamTwoColor = currentTeamTwoColor;
        generateArena();
        changeTeamCarpetColors(tempTeamOneColor, tempTeamTwoColor);
        currentRing = 0;

        for (Entity entity : getOrigin().getWorld().getEntities())
            if (entity instanceof Item || entity instanceof Arrow)
                entity.remove();
    }

    public void changeTeamCarpetColors(Material teamOneColor, Material teamTwoColor) {


        // loop on the xz plane and replace the old colors
        for (int x = origin.getBlockX() - X_ARENA_RADIUS + 1; x < origin.getBlockX() + X_ARENA_RADIUS; x++) {
            for (int z = origin.getBlockZ() - Z_ARENA_RADIUS + 1; z < origin.getBlockZ() + Z_ARENA_RADIUS; z++) {
                Block b = origin.getWorld().getBlockAt(x, origin.getBlockY(), z);

                if (b.getType() == Material.WHITE_CARPET || b.getType() == Material.BLACK_CARPET)
                    continue;

                Material newMat = z - origin.getBlockZ() > 0 ? teamOneColor : teamTwoColor;
                b.setType(newMat);
            }
        }

        // replace the walls on the outside
        for (int x = origin.getBlockX() - X_ARENA_RADIUS - 1; x <= origin.getBlockX() + X_ARENA_RADIUS; x++) {
            for (int y = spawn.getBlockY() - 14; y < spawn.getBlockY() - 1; y++) {
                for (int z = origin.getBlockZ() - Z_ARENA_RADIUS - 1; z <= origin.getBlockZ() + Z_ARENA_RADIUS; z++){

                    if (origin.getWorld().getBlockAt(x, y, z).getType() == Material.WHITE_CONCRETE || origin.getWorld().getBlockAt(x, y, z).getType() == Material.BLACK_CONCRETE)
                        continue;

                    if (origin.getWorld().getBlockAt(x, y, z).getType().toString().toLowerCase().contains("concrete")) {
                        Material color = z - origin.getBlockZ() > 0 ? teamOneColor : teamTwoColor;
                        origin.getWorld().getBlockAt(x, y, z).setType(Material.valueOf(color.toString().replace("CARPET", "CONCRETE")));
                    }

                }
            }
        }

        // set new colors
        this.currentTeamOneColor = teamOneColor;
        this.currentTeamTwoColor = teamTwoColor;
        for (DodgeboltArenaDisappearingRing ring : rings)
            ring.updateStoredBlocks();
    }

    public void changeTeamCarpetColors(ChatColor teamOneColor, ChatColor teamTwoColor) {

        Material newTeamOne = ColorTranslator.chatColorToCarpet(teamOneColor);
        Material newTeamTwo = ColorTranslator.chatColorToCarpet(teamTwoColor);

        while (newTeamOne == newTeamTwo)
            newTeamTwo = ColorTranslator.getRandomCarpetColor();

        changeTeamCarpetColors(newTeamOne, newTeamTwo);
    }

    /**
     * Gets a spawn location of the arena, i should be 0-3
     *
     * @param i Index of the player we should spawn them at
     * @param negateZ Whether or not we should negate the Z coordinate, we do this if we want to get a spawn for the other side of the field
     */
    public Location getSpawnLocation(int i, boolean negateZ) {
        int spawnIndex = i % 4;

        Location location = origin.clone();
        if (!negateZ)
            location.setYaw(180);

        switch (spawnIndex) {
            case 0:
                return location.add(9 * (negateZ ? -1 : 1) + .5, 0, 9 * (negateZ ? -1 : 1) +  + .5);
            case 1:
                return location.add(-9 * (negateZ ? -1 : 1) + .5, 0, 9 * (negateZ ? -1 : 1) + .5);
            case 2:
                return location.add(3 * (negateZ ? -1 : 1) + .5, 0, 12 * (negateZ ? -1 : 1) + .5);
            case 3:
                return location.add(-3 * (negateZ ? -1 : 1) + .5, 0, 12 * (negateZ ? -1 : 1) + .5);
        }

        throw new IllegalStateException("If this throws then i don't know what to tell you chief");
    }

    /**
     * Turn on barriers that restrict players from moving
     */
    public void enableBarriers() {

        for (int i = 0; i < 8; i++) {
            Location loc = getSpawnLocation(i, i >= 4);

            // We need to add a barrier block 1 in every direction
            loc.clone().add(1, 1, 0).getBlock().setType(Material.BARRIER);
            loc.clone().add(0, 1, 1).getBlock().setType(Material.BARRIER);
            loc.clone().add(-1, 1, 0).getBlock().setType(Material.BARRIER);
            loc.clone().add(0, 1, -1).getBlock().setType(Material.BARRIER);
        }

    }

    public void disableBarriers() {

        for (int i = 0; i < 8; i++) {
            Location loc = getSpawnLocation(i, i >= 4);

            // We need to add a barrier block 1 in every direction
            loc.clone().add(1, 1, 0).getBlock().setType(Material.AIR);
            loc.clone().add(0, 1, 1).getBlock().setType(Material.AIR);
            loc.clone().add(-1, 1, 0).getBlock().setType(Material.AIR);
            loc.clone().add(0, 1, -1).getBlock().setType(Material.AIR);
        }

    }

    public void shrinkArena () {

        // Arena is already shrank
        if (currentRing > 5)
            return;

        rings.get(currentRing).startFullDisappearingTask();
        currentRing++;
    }

    public Location[] getArrowSpawnLocations() {
        return new Location[]{origin.clone().toCenterLocation().add(0, 0, 6), origin.clone().toCenterLocation().add(0, 0, -6)};
    }

    public void destroyArena() {
        // TODO: figure out how to actually do this lol
//        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(origin.getWorld()), -1)) {
//            session.undo(session);
//        }
    }



}
