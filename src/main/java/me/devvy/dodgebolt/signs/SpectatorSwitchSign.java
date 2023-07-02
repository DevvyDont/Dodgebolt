package me.devvy.dodgebolt.signs;

import me.devvy.dodgebolt.game.DodgeboltGame;
import me.devvy.dodgebolt.game.DodgeboltGameState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;


public class SpectatorSwitchSign extends InteractableSign implements Listener {

    public SpectatorSwitchSign(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        update();
    }

    public void update() {
        setLine(0, ChatColor.GRAY + "SPECTATE");
        setLine(1, "Punch to leave");
        setLine(2, "your team!");
        setLine(3, ChatColor.ITALIC + "(Sneak = all)");
        updateSign();
    }

    @Override
    public void handlePunched(Player player) {

        if (game.getState() != DodgeboltGameState.WAITING) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "There is a game in progress!");
            return;
        }

        if (player.isSneaking()) {

            if (game.isOnlyAdminsCanEdit() && !player.isOp()) {
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "Only admins can clear teams right now!");
                return;
            }

            for (Player p : game.getAllPlayersInStadium())
                game.setSpectating(p);

            String displayname = (player.isOp() ? ChatColor.RED : ChatColor.DARK_GRAY) + player.getName();
            game.broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "Teams were cleared by " + displayname + ChatColor.GRAY + "!");
        }

        game.setSpectating(player);
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + (game.getPlayerTeam(player) != null ? ChatColor.AQUA + "Switched to spectator!" : ChatColor.RED + "Already spectating!"));

        update();
    }

}
