package me.devvy.dodgebolt.map;

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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DodgeboltArena {


    private final Location origin;
    private final Location spawn;  // Used for knowing where to spawn spectators and tp ppl that died
    private final String schematicPath = "/WorldEdit/schematics/dodgebolt_arena.schem";
    private boolean generated = false;

    public static final int X_ARENA_RADIUS = 15;  // From center to side walls
    public static final int Z_ARENA_RADIUS = 17;  // From center to back walls

    private ChatColor currentTeamOneColor = ChatColor.BLUE;
    private ChatColor currentTeamTwoColor = ChatColor.LIGHT_PURPLE;

    private List<DodgeboltArenaDisappearingRing> rings = new ArrayList<>();

    private int currentRing = 0;

    private DodgeboltArenaOvertimeShatterTask overtimeShatterTask;

    public DodgeboltArena(Location origin) {
        this.origin = origin;
        this.spawn = origin.clone().add(0, 6, 0);
        this.spawn.setYaw(90);

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

        overtimeShatterTask = null;
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

        Clipboard clipboard;

        String path = Dodgebolt.getPlugin(Dodgebolt.class).getDataFolder().getParent() + schematicPath;
        File file = new File(path);

        if (Dodgebolt.getInstance().getServer().getPluginManager().getPlugin("WorldEdit") == null)
            throw new IllegalStateException("WorldEdit must be installed alongside this plugin! Arena generation depends on it");

        if (!file.exists())
            throw new IllegalStateException("Could not find the arena schematic!!! It should be located at '" + path + "', download given schematic from the github releases tab");

        ClipboardFormat format = ClipboardFormats.findByFile(file);

        if (format == null)
            throw new IllegalStateException("Could not find the arena schematic!!! It should be located at '" + path + "', download given schematic from the github releases tab");

        try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
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
        generateArena();
        changeTeamColors(currentTeamOneColor, currentTeamTwoColor);
        currentRing = 0;
    }

    public void changeTeamColors(ChatColor teamOneColor, ChatColor teamTwoColor) {

        // replace the walls on the outside
        for (int x = origin.getBlockX() - X_ARENA_RADIUS - 1; x <= origin.getBlockX() + X_ARENA_RADIUS; x++) {
            for (int y = spawn.getBlockY() - 14; y < spawn.getBlockY() - 1; y++) {
                for (int z = origin.getBlockZ() - Z_ARENA_RADIUS - 1; z <= origin.getBlockZ() + Z_ARENA_RADIUS; z++){

                    Block b = origin.getWorld().getBlockAt(x, y, z);

                    if (!ColorTranslator.isTeamBlock(b.getType()))
                        continue;

                    ChatColor color = z - origin.getBlockZ() > 0 ?  teamOneColor : teamTwoColor;
                    b.setType(ColorTranslator.getTranslatedTeamBlock(b.getType(), color), false);
                }
            }
        }

        // set new colors
        this.currentTeamOneColor = teamOneColor;
        this.currentTeamTwoColor = teamTwoColor;
        for (DodgeboltArenaDisappearingRing ring : rings)
            ring.updateStoredBlocks();
    }

    /**
     * Returns an array of spawn locations relative to the origin of the arena for one side
     *
     * @param mirror if you want to get the locations for team 2, pass in true
     * @return an array of Locations representing spawns
     */
    public Location[] getSpawnLocations(boolean mirror) {

        int mirrorMult = mirror ? -1 : 1;

        Location originClone = origin.clone();
        originClone.setYaw(0);
        if (!mirror)
            originClone.setYaw(originClone.getYaw()+180);

        return new Location[]{
                originClone.clone().add(-3 * mirrorMult, 0, 12 * mirrorMult).toCenterLocation(),  // Mid left circle
                originClone.clone().add(3 * mirrorMult, 0, 12 * mirrorMult).toCenterLocation(),  // Mid right circle
                originClone.clone().add(-9 * mirrorMult, 0, 9 * mirrorMult).toCenterLocation(),  // Far left circle
                originClone.clone().add(9 * mirrorMult, 0, 9 * mirrorMult).toCenterLocation(),  // Far right circle
                originClone.clone().add(0 * mirrorMult, 0, 14 * mirrorMult).toCenterLocation(),  // Mid backish
                originClone.clone().add(7 * mirrorMult, 0, 14 * mirrorMult).toCenterLocation(),  // Mid backleftish
                originClone.clone().add(-7 * mirrorMult, 0, 13 * mirrorMult).toCenterLocation(),  // Mid backrightish
        };
    }

    /**
     * Simply returns both team 1 and team 2 spawn locations
     *
     * @return an array of Locations representing ALL spawns
     */
    public Location[] getAllSpawnLocations() {

        // Grab the two sides' spawns
        Location[] sideOne = getSpawnLocations(false);
        Location[] sideTwo = getSpawnLocations(true);

        // Make a new array to store all the locations in together
        Location[] allSpawns = new Location[sideOne.length + sideTwo.length];

        // Copy the elements in
        System.arraycopy(sideOne, 0, allSpawns, 0, sideOne.length);
        System.arraycopy(sideTwo, 0, allSpawns, sideOne.length, sideTwo.length);

        return allSpawns;

    }

    /**
     * Gets a spawn location of the arena based on index of player
     *
     * @param i Index of the player we should spawn them at
     * @param mirror Whether we should negate the Z coordinate, we do this if we want to get a spawn for the other side of the field
     */
    public Location getSpawnLocation(int i, boolean mirror) {

        Location[] spawns = getSpawnLocations(mirror);
        int spawnIndex = i % spawns.length;
        return spawns[spawnIndex];

    }

    /**
     * Set the block type that blocks players in, typically either barrier or air
     *
     * @param material the block type to set
     */
    public void setSpawnBarrier(Material material) {

        for (Location spawn : getAllSpawnLocations()) {
            // We need to add a barrier block 1 in every direction
            spawn.clone().add(1, 1, 0).getBlock().setType(material);
            spawn.clone().add(0, 1, 1).getBlock().setType(material);
            spawn.clone().add(-1, 1, 0).getBlock().setType(material);
            spawn.clone().add(0, 1, -1).getBlock().setType(material);
        }

    }

    public void shrinkArena () {

        // Arena is already shrank
        if (currentRing > 5)
            return;

        rings.get(currentRing).startFullDisappearingTask();
        currentRing++;
    }

    public void startOvertimeShatter() {

        if (overtimeShatterTask != null)
            stopOvertimeShatter();

        // After all rings are destroyed start slowly breaking the entire arena
        overtimeShatterTask = new DodgeboltArenaOvertimeShatterTask(this);
        overtimeShatterTask.runTaskTimer(Dodgebolt.getInstance(), 0, 2);

    }

    public void stopOvertimeShatter() {

        if (overtimeShatterTask == null)
            return;

        // Stop breaking blocks
        overtimeShatterTask.cancel();
        overtimeShatterTask = null;
    }

    public Location[] getArrowSpawnLocations() {
        return new Location[]{origin.clone().toCenterLocation().add(0, 0, 6), origin.clone().toCenterLocation().add(0, 0, -6)};
    }

    public boolean isInArena(Location location) {

        if (!location.getWorld().equals(origin.getWorld()))
            return false;

        int MAX_X = X_ARENA_RADIUS + 1;
        int MAX_Z = Z_ARENA_RADIUS + 1;
        int MAX_PLUS_Y = 4;
        int MAX_MINUS_Y = -10;

        boolean inXBounds = Math.abs(location.getBlockX() - origin.getBlockX()) <= MAX_X;
        int yDif = location.getBlockY() - origin.getBlockY();
        boolean inYBounds = yDif <= MAX_PLUS_Y && yDif >= MAX_MINUS_Y;
        boolean inZBounds = Math.abs(location.getBlockZ() - origin.getBlockZ()) <= MAX_Z;

        return inXBounds && inYBounds && inZBounds;
    }

    public boolean outOfBowRange(Location location) {

        if (Math.abs(location.getBlockY() - origin.getBlockY()) > 3)
            return false;

        int offset = Math.abs(origin.getBlockX() - location.getBlockX()) > 5 ? 0 : 1;
        return Math.abs(origin.getBlockZ() - location.getBlockZ()) < 3 + offset;
    }

    public void destroyArena() {
        // TODO: figure out how to actually do this lol
//        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(origin.getWorld()), -1)) {
//            session.undo(session);
//        }
    }



}
