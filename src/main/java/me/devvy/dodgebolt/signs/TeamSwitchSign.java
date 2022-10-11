package me.devvy.dodgebolt.signs;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.PlayerJoinTeamEvent;
import me.devvy.dodgebolt.events.PlayerLeaveTeamEvent;
import me.devvy.dodgebolt.events.TeamColorChangeEvent;
import me.devvy.dodgebolt.game.DodgeboltGame;
import me.devvy.dodgebolt.game.DodgeboltGameState;
import me.devvy.dodgebolt.team.Team;
import me.devvy.dodgebolt.util.ColorTranslator;
import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

        setLine(1, team.getTeamColor().toString() + team.getMembers().size() + ChatColor.BLACK + " Player(s)");
        setLine(3, "Punch to join!");
        updateSign();
    }

    @Override
    public void handlePunched(Player player) {

        if (game.getState() != DodgeboltGameState.WAITING) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "There is a game in progress!");
            return;
        }

        boolean opColorChangeRule = Dodgebolt.getPlugin(Dodgebolt.class).getConfig().getBoolean(ConfigManager.OP_CHANGE_COLOR);

        if (player.isSneaking()) {

            if (opColorChangeRule && !player.isOp()) {
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "You must be op to change team color!");
                return;
            }

            int currentIndex = -1;
            for (int i = 0; i < ColorTranslator.ALLOWED_TEAM_COLORS.length; i++) {
                if (ColorTranslator.ALLOWED_TEAM_COLORS[i] == team.getTeamColor()) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == ColorTranslator.ALLOWED_TEAM_COLORS.length - 1)
                currentIndex = -1;

            currentIndex++;

            if (game.getOpposingTeam(team).getTeamColor() == ColorTranslator.ALLOWED_TEAM_COLORS[currentIndex])
                currentIndex++;

            if (currentIndex >= ColorTranslator.ALLOWED_TEAM_COLORS.length)
                currentIndex = 0;

            team.setTeamColor(ColorTranslator.ALLOWED_TEAM_COLORS[currentIndex]);
            player.sendActionBar(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "Changed team color to " + team.getTeamColor() + team.getTeamColor().name() + ChatColor.GRAY + "!");
            return;
        }

        if (team.isMember(player)) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.RED + "You are already on this team!");
            return;
        }

        boolean showColorMessage = !opColorChangeRule || player.isOp();

        game.setPlayerTeam(player, team);
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.AQUA + "You joined " + team.getTeamColor() + team.getName() + ChatColor.AQUA + (showColorMessage ? "! Punch the sign while sneaking to change the team color!" : ""));
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
