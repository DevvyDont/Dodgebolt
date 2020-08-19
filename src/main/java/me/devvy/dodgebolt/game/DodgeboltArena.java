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
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DodgeboltArena {


    private final Location origin;
    private final Location spawn;  // Used for knowing where to spawn spectators and tp ppl that died
    private boolean generated = false;

    public static final int X_ARENA_RADIUS = 15;  // From center to side walls
    public static final int Z_ARENA_RADIUS = 17;  // From center to back walls

    private Material currentTeamOneColor = Material.BLUE_CARPET;
    private Material currentTeamTwoColor = Material.ORANGE_CARPET;

    public DodgeboltArena(Location origin) {
        this.origin = origin;
        this.spawn = origin.clone().add(0, 12, 0);
        this.spawn.setYaw(90);
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

    }

    /**
     * Used to reload the arena back to what it is, used for in between rounds
     */
    public void restoreArena() {
        Material tempTeamOneColor = currentTeamOneColor;
        Material tempTeamTwoColor = currentTeamTwoColor;
        generateArena();
        changeTeamCarpetColors(tempTeamOneColor, tempTeamTwoColor);
    }

    public void changeTeamCarpetColors(Material teamOneColor, Material teamTwoColor) {


        // loop on the xz plane and replace the old colors
        for (int x = origin.getBlockX() - X_ARENA_RADIUS; x < origin.getBlockX() + X_ARENA_RADIUS; x++) {
            for (int z = origin.getBlockZ() - Z_ARENA_RADIUS; z < origin.getBlockZ() + Z_ARENA_RADIUS; z++) {
                if (origin.getWorld().getBlockAt(x, origin.getBlockY(), z).getType() == currentTeamOneColor)
                    origin.getWorld().getBlockAt(x, origin.getBlockY(), z).setType(teamOneColor);
                else if (origin.getWorld().getBlockAt(x, origin.getBlockY(), z).getType() == currentTeamTwoColor)
                    origin.getWorld().getBlockAt(x, origin.getBlockY(), z).setType(teamTwoColor);
            }
        }

        // set new colors
        this.currentTeamOneColor = teamOneColor;
        this.currentTeamTwoColor = teamTwoColor;
    }

    public void changeTeamCarpetColors(ChatColor teamOneColor, ChatColor teamTwoColor) {

        Material newTeamOne = ColorTranslator.chatColorToCarpet(teamOneColor);
        Material newTeamTwo = ColorTranslator.chatColorToCarpet(teamTwoColor);

        while (newTeamOne == newTeamTwo)
            newTeamTwo = ColorTranslator.getRandomCarpetColor();

        changeTeamCarpetColors(newTeamOne, newTeamTwo);
    }

    public void destroyArena() {
        // TODO: figure out how to actually do this lol
//        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(origin.getWorld()), -1)) {
//            session.undo(session);
//        }
    }



}
