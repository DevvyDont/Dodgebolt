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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DodgeboltStadium {


    private final Location origin;
    private final Location spawn;  // Used for knowing where to spawn spectators and tp ppl that died
    private final DodgeboltArena arena;
    private final String schematicPath = "/WorldEdit/schematics/dodgebolt_stadium.schem";
    private boolean generated = false;

    public static final int X_STADIUM_RADIUS = 24;  // From center to side walls
    public static final int Z_STADIUM_RADIUS = 26;  // From center to back walls

    public DodgeboltStadium(Location origin) {
        this.origin = origin;
        this.spawn = origin.clone().add(-18, 6, 0);
        this.spawn.setYaw(90);
        this.origin.getWorld().setDifficulty(Difficulty.PEACEFUL);
        this.arena = new DodgeboltArena(origin);
    }

    public boolean isGenerated() {
        return generated;
    }

    public Location getOrigin() {
        return origin.clone();
    }

    public Location getSpawn() {
        return spawn.clone();
    }

    public DodgeboltArena getArena() {
        return arena;
    }

    public void generateStadium() {

        Clipboard clipboard;

        String path = Dodgebolt.getPlugin(Dodgebolt.class).getDataFolder().getParent() + schematicPath;
        File file = new File(path);
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        if (format == null)
            throw new IllegalStateException("Could not find the stadium schematic!!! It should be located at '" + path + "', download given schematic from the github releases tab");

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
            Dodgebolt.getPlugin(Dodgebolt.class).getLogger().severe("Could not generate the stadium!");
            e.printStackTrace();
            return;
        }

        this.generated = true;

        arena.generateArena();
    }

    public void changeTeamColors(ChatColor teamOneColor, ChatColor teamTwoColor) {

        // replace the walls on the outside
        for (int x = origin.getBlockX() - X_STADIUM_RADIUS - 1; x <= origin.getBlockX() + X_STADIUM_RADIUS; x++) {
            for (int z = origin.getBlockZ() - Z_STADIUM_RADIUS - 1; z <= origin.getBlockZ() + Z_STADIUM_RADIUS; z++){
                for (int y = origin.getBlockZ() - 15; y <= origin.getWorld().getHighestBlockAt(x, z).getY(); y++) {

                    Block b = origin.getWorld().getBlockAt(x, y, z);

                    if (b.getType() == Material.AIR)
                        continue;

                    if (arena.isInArena(b.getLocation()))
                        continue;

                    if (!ColorTranslator.isTeamBlock(b.getType()))
                        continue;

                    ChatColor color = z - origin.getBlockZ() > 0 ? teamOneColor : teamTwoColor;
                    b.setType(ColorTranslator.getTranslatedTeamBlock(b.getType(), color), false);

                }
            }
        }

        arena.changeTeamColors(teamOneColor, teamTwoColor);
    }

    public void destroyStadium() {
        // TODO: figure out how to actually do this lol
//        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(origin.getWorld()), -1)) {
//            session.undo(session);
//        }
    }



}
