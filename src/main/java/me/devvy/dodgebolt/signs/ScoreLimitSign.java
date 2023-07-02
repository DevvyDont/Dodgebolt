package me.devvy.dodgebolt.signs;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.game.DodgeboltGame;
import me.devvy.dodgebolt.game.DodgeboltGameState;
import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class ScoreLimitSign extends InteractableSign {

    private final int MIN_ROUND_LIMIT = 2;
    private final int MAX_ROUND_LIMIT = 30;

    public ScoreLimitSign(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        updateTopLine();
        setLine(2, ChatColor.GREEN + "Punch to +1!");
        setLine(3, ChatColor.RED + "SNEAK to -1!", true);
    }

    public void updateTopLine() {
        setLine(0, ChatColor.BLACK + "Score Limit: " + ChatColor.AQUA + ChatColor.BOLD + game.getRoundsToWin(), true);
    }

    @Override
    public void handlePunched(Player player) {

        if (game.isOnlyAdminsCanEdit() && !player.isOp()){
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "You must be op to change settings!");
            return;
        }

        if (game.getState() != DodgeboltGameState.WAITING) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "The game is already going!");
            return;
        }

        int inc = player.isSneaking() ? -1 : 1;
        int newRounds = Math.max(Math.min(game.getRoundsToWin() + inc, MAX_ROUND_LIMIT), MIN_ROUND_LIMIT);

        game.setRoundsToWin(newRounds);
        updateTopLine();
    }


}
