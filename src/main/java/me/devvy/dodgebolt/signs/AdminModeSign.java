package me.devvy.dodgebolt.signs;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.game.DodgeboltGame;
import me.devvy.dodgebolt.game.DodgeboltGameState;
import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class AdminModeSign extends InteractableSign {

    public AdminModeSign(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        update();
    }

    public void update() {
        if (game.isOnlyAdminsCanEdit())
            whenEnabled();
        else
            whenDisabled();

        updateSign();
    }

    public void whenEnabled() {
        setLine(0, ChatColor.RED.toString() + ChatColor.BOLD + "ADMIN MODE:");
        setLine(1, ChatColor.GREEN + "Enabled");
        setLine(2, "Only admins can");
        setLine(3, "edit/start games!");
    }

    public void whenDisabled() {
        setLine(0, ChatColor.RED.toString() + ChatColor.BOLD + "ADMIN MODE:");
        setLine(1, ChatColor.DARK_GRAY + "Disabled");
        setLine(2, "Anybody can");
        setLine(3, "edit/start games!");
    }

    @Override
    public void handlePunched(Player player) {

        // Only ops can interact with this sign
        if (!player.isOp()) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "Only admins may use this sign!");
            return;
        }

        // Invert the setting
        game.setOnlyAdminsCanEdit(!game.isOnlyAdminsCanEdit());
        Dodgebolt.getInstance().getConfig().set(ConfigManager.OP_CHANGE_SETTINGS, game.isOnlyAdminsCanEdit());
        Dodgebolt.getInstance().saveConfig();

        if (game.isOnlyAdminsCanEdit())
            game.broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "Dodgebolt is now in admin config mode!");
        else
            game.broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "Dodgebolt is now in public config mode!");

        update();
    }


}
