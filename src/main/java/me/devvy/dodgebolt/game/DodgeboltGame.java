package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.team.Team;
import me.devvy.dodgebolt.util.Phrases;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class DodgeboltGame implements Listener {

    private final DodgeboltArena arena;
    private final Team team1;
    private final Team team2;

    private final MinecraftScoreboardManager scoreboardManager;

    public DodgeboltGame() {
        World arenaWorld = Bukkit.getWorld("world");
        if (arenaWorld == null)
            throw new IllegalStateException("There must be a world named 'world'!");

        arenaWorld.setGameRule(GameRule.DO_FIRE_TICK, false);  // Important so lava doesn't destroy arena

        arena = new DodgeboltArena(new Location(arenaWorld, 50, 100, 50));
        arena.generateArena();

        team1 = new Team("Team 1", ChatColor.BLUE);
        team2 = new Team("Team 2", ChatColor.LIGHT_PURPLE);

        arena.changeTeamCarpetColors(team1.getTeamColor(), team2.getTeamColor());

        new TeamSwitchSign(this, new Location(arenaWorld, 41, 112, 52), BlockFace.EAST, team1);
        new TeamSwitchSign(this, new Location(arenaWorld, 41, 112, 48),BlockFace.EAST, team2);
        new SpectatorSwitchSign(this, new Location(arenaWorld, 41, 112, 50), BlockFace.EAST);
        new StartGameSign(this, new Location(arenaWorld, 41, 113, 50), BlockFace.EAST);

        scoreboardManager = new MinecraftScoreboardManager(this);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().registerEvents(scoreboardManager, Dodgebolt.getPlugin(Dodgebolt.class));
    }

    public DodgeboltArena getArena() {
        return arena;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public Team getPlayerTeam(Player player) {
        if (team1.isMember(player))
            return team1;
        else if (team2.isMember(player))
            return team2;
        return null;
    }

    public void setPlayerTeam(Player player, Team team) {

        if (getPlayerTeam(player) != null)
            getPlayerTeam(player).removePlayer(player);

        team.addPlayer(player);
    }

    public void setSpectating(Player player) {
        getTeam2().removePlayer(player);
        getTeam1().removePlayer(player);
        player.setDisplayName(ChatColor.DARK_GRAY + "[SPEC] " + ChatColor.stripColor(player.getName()));
    }

    public void cleanup() {
        arena.destroyArena();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setSpectating(event.getPlayer());
        event.getPlayer().teleport(arena.getSpawn());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {

        // TODO: kill players that leave if they're in game
        team1.removePlayer(event.getPlayer());
        team2.removePlayer(event.getPlayer());

        event.getPlayer().teleport(arena.getSpawn());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(arena.getSpawn());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Team playerTeam = getPlayerTeam(event.getEntity());
        if (playerTeam == null) {
            event.setDeathMessage("");
            return;
        }

        Player killer = event.getEntity().getKiller();
        event.setDeathMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + (killer != null ? Phrases.getRandomKilledPhrase(event.getEntity(), killer) : Phrases.getRandomSuicidePhrase(event.getEntity())));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setFormat("%s " + ChatColor.WHITE + ">> " + ChatColor.GRAY + "%s");
    }

    @EventHandler
    public void onPlayerFellInVoid(PlayerMoveEvent event) {
        if (event.getTo().getY() < 0)
            event.getPlayer().teleport(arena.getSpawn());
    }




}
