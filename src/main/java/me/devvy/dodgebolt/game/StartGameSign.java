package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class StartGameSign extends InteractableSign {

    public StartGameSign(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        setLine(1, ChatColor.GREEN + "Start Game!", true);
    }

    @Override
    public void handlePunched(Player player) {

        if (Dodgebolt.getPlugin(Dodgebolt.class).getConfig().getBoolean(ConfigManager.OP_START_GAME) && !player.isOp()){
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "You must be op to start a game!");
            return;
        }

        if (game.getTeam1().getMembers().size() == 0 || game.getTeam2().getMembers().size() == 0) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "One of the teams are empty!");
            return;
        }

        if (game.getState() != DodgeboltGameState.WAITING) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "The game is already going!");
            return;
        }

        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Starting the game!");
        game.startNewGame();
    }
}
