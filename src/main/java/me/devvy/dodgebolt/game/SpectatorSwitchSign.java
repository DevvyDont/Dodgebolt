package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.PlayerJoinTeamEvent;
import me.devvy.dodgebolt.events.PlayerLeaveTeamEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorSwitchSign extends InteractableSign implements Listener {

    public SpectatorSwitchSign(DodgeboltGame game, Location location, BlockFace direction) {
        super(game, location, direction);
        doDelayedUpdate(5);
    }

    public void update() {
        setLine(0, ChatColor.GRAY + "SPECTATE");

        int actualPlaying = game.getTeam1().getMembers().size() + game.getTeam2().getMembers().size();
        int spectatingAmount = Bukkit.getOnlinePlayers().size() - actualPlaying;

        setLine(1, ChatColor.AQUA.toString() + spectatingAmount + ChatColor.DARK_GRAY +" watching!");
        setLine(3, "Punch to spectate!");
        updateSign();
    }

    @Override
    public void handlePunched(Player player) {
        game.setSpectating(player);
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + (game.getPlayerTeam(player) != null ? ChatColor.AQUA + "Switched to spectator!" : ChatColor.RED + "Already spectating!"));
        update();
    }

    public void doDelayedUpdate(int ticks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskLater(Dodgebolt.getPlugin(Dodgebolt.class), ticks);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        doDelayedUpdate(5);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        doDelayedUpdate(5);
    }

    @EventHandler
    public void onPlayerLeaveTeam(PlayerLeaveTeamEvent event) {
        doDelayedUpdate(5);
    }

    @EventHandler
    public void onPlayerJoinTeam(PlayerJoinTeamEvent event) {
        doDelayedUpdate(5);
    }
}
