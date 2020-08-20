package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.PlayerJoinTeamEvent;
import me.devvy.dodgebolt.events.PlayerLeaveTeamEvent;
import me.devvy.dodgebolt.events.TeamColorChangeEvent;
import me.devvy.dodgebolt.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamSwitchSign extends InteractableSign {

    private final Team team;

    public TeamSwitchSign(DodgeboltGame game, Location location, BlockFace direction, Team team) {
        super(game, location, direction);
        this.team = team;
        update();
    }

    public void update() {
        setLine(0, team.getTeamColor() + team.getName());

        setLine(1, team.getMembers().size() + "/4");
        setLine(3, team.isFull() ? "FULL!" : "Punch to join!");
        updateSign();
    }

    @Override
    public void handlePunched(Player player) {

        if (game.getState() != DodgeboltGameState.WAITING) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "There is a game in progress!");
            return;
        }

        if (player.isSneaking()) {
            int currentIndex = 0;
            for (int i = 0; i < ChatColor.values().length; i++) {
                if (ChatColor.values()[i] == team.getTeamColor()) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex >= 15)
                currentIndex = -1;

            currentIndex++;

            if (game.getOpposingTeam(team).getTeamColor() == ChatColor.values()[currentIndex])
                currentIndex++;

            if (currentIndex >= 15)
                currentIndex = 0;

            team.setTeamColor(ChatColor.values()[currentIndex]);
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "Changed team color to " + team.getTeamColor() + team.getTeamColor().name() + ChatColor.GRAY + "!");
            return;
        }

        if (team.isMember(player)) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "You are already on this team!");
            return;
        }

        if (team.isFull()) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "That team is full!");
            return;
        }

        game.setPlayerTeam(player, team);
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.AQUA + "You joined " + team.getTeamColor() + team.getName() + ChatColor.AQUA + "!");
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeamColorChange(TeamColorChangeEvent event) {
        doDelayedUpdate(5);
    }
}
