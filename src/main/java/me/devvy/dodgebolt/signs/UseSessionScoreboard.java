package me.devvy.dodgebolt.signs;

import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class UseSessionScoreboard extends InteractableSign {

    public UseSessionScoreboard(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        update();
    }

    public void update() {
        if (game.useSessionScoreboard())
            whenEnabled();
        else
            whenDisabled();

        updateSign();
    }

    public void whenEnabled() {
        setLine(0, ChatColor.AQUA.toString() + ChatColor.BOLD + "SCOREBOARD:");
        setLine(1, ChatColor.GREEN + "Session");
        setLine(2, "Track stats through");
        setLine(3, "consecutive games");
    }

    public void whenDisabled() {
        setLine(0, ChatColor.AQUA.toString() + ChatColor.BOLD + "SCOREBOARD:");
        setLine(1, ChatColor.GRAY + "Match");
        setLine(2, "Reset stats");
        setLine(3, "between games");
    }

    @Override
    public void handlePunched(Player player) {

        // Only ops can interact with this sign
        if (game.isOnlyAdminsCanEdit() && !player.isOp()) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "Currently only admins can edit settings!");
            return;
        }

        // Invert the setting
        game.setUseSessionScoreboard(!game.useSessionScoreboard());

        if (game.useSessionScoreboard())
            game.broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "Scoreboard will keep tracking consecutive games!");
        else
            game.broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "Scoreboard now tracks one game at a time!");

        update();
    }


}
