package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class ShuffleTeamsSign extends InteractableSign {

    public ShuffleTeamsSign(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        setLine(0, ChatColor.AQUA + "Shuffle Teams!");
        setLine(1, "Punch to shuffle!");
        setLine(2, "SNEAK to include");
        setLine(3, "Spectators!", true);
    }

    @Override
    public void handlePunched(Player player) {

        if (Dodgebolt.getPlugin(Dodgebolt.class).getConfig().getBoolean(ConfigManager.OP_CHANGE_SETTINGS) && !player.isOp()){
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "You must be op to change settings!");
            return;
        }

        if (game.getState() != DodgeboltGameState.WAITING) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "The game is already going!");
            return;
        }

        boolean includeSpecs = player.isSneaking();

        game.shuffleTeams(includeSpecs);
        String specSwitchAlert = includeSpecs ? " and " + ChatColor.LIGHT_PURPLE + "Spectators" + ChatColor.AQUA + " have been auto assigned" : "";
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.AQUA + "Teams have been shuffled" + specSwitchAlert + ChatColor.AQUA + "!");
    }

}
