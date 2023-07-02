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

public class BowDamageSign extends InteractableSign {

    private final int[] allowedValues = {10, 15, 20, 25, 35, 50, 100};


    public BowDamageSign(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        updateTopLine();
        setLine(2, ChatColor.GREEN + "Punch to +!");
        setLine(3, ChatColor.RED + "SNEAK to -!", true);
    }

    public void updateTopLine() {
        setLine(0, ChatColor.BLACK + "Bow Dmg: " + ChatColor.AQUA + ChatColor.BOLD + game.getBowDamagePercent() + "%", true);
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

        int shift = player.isSneaking() ? -1 : 1;
        int currIndex = -1;

        for (int i = 0; i < allowedValues.length; i++)
            if (allowedValues[i] == game.getBowDamagePercent()) {
                currIndex = i;
                break;
            }

        currIndex += shift;
        currIndex = Math.min(Math.max(0, currIndex), allowedValues.length-1);

        game.setBowDamagePercent(allowedValues[currIndex]);
        updateTopLine();
    }


}
