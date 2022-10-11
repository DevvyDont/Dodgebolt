package me.devvy.dodgebolt.signs;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class InteractableSign implements Listener {

    protected final DodgeboltGame game;
    protected Location location;
    protected BlockFace direction;
    protected String[] lines = new String[]{"", "", "", ""};

    public InteractableSign(DodgeboltGame game, Location location, BlockFace direction) {
        this.game = game;
        this.location = location;
        this.direction = direction;
        setLocation(location);
        Dodgebolt plugin = Dodgebolt.getPlugin(Dodgebolt.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setLocation(Location location) {
        this.location.getBlock().setType(Material.AIR);
        this.location = location;
        location.getBlock().setType(Material.OAK_WALL_SIGN);
        setDirection(direction);
    }

    public void setDirection(BlockFace direction) {
        Sign signState = (Sign) location.getBlock().getState();
        WallSign wallSignData = (WallSign) signState.getBlockData();
        wallSignData.setFacing(direction);
        signState.setBlockData(wallSignData);
        location.getBlock().setBlockData(signState.getBlockData());
        signState.update();

    }


    public abstract void handlePunched(Player player);

    public void setLine(int index, String message) {
        setLine(index, message, false);
    }

    public void setLine(int index, String message, boolean update) {
        lines[index] = message;

        if (update)
            updateSign();
    }

    public void updateSign() {
        try {
            Sign sign = (Sign) location.getBlock().getState();
            for (int i = 0; i < lines.length; i++)
                sign.setLine(i, lines[i]);
            sign.update();
        } catch (ClassCastException ignored) {
            location.getBlock().setType(Material.OAK_WALL_SIGN);
            updateSign();
        }
    }

    @EventHandler
    public void onPlayerInteracted(PlayerInteractEvent event) {

        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock() == null)
            return;

        if (event.getClickedBlock().equals(location.getBlock()))
            handlePunched(event.getPlayer());

    }

    @EventHandler
    public void onPlayerBroke(BlockBreakEvent event) {

        if (location.getBlock() != event.getBlock())
            return;

        event.setCancelled(true);

        if (event.getPlayer().isOp() && event.getPlayer().isSneaking())
            event.setCancelled(false);
        else if (event.getPlayer().isOp())
            event.getPlayer().sendMessage(ChatColor.RED + "To break this sign break while sneaking.");
    }

}
