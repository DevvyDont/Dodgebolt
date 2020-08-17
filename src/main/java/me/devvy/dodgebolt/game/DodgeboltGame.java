package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.team.Team;
import me.devvy.dodgebolt.util.Phrases;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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

    public void cleanup() {
        arena.destroyArena();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setDisplayName(ChatColor.DARK_GRAY + "[SPEC] " + ChatColor.stripColor(event.getPlayer().getName()));
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
        event.setDeathMessage(killer != null ? Phrases.getRandomKilledPhrase(event.getEntity(), killer) : Phrases.getRandomSuicidePhrase(event.getEntity()));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setFormat("%s " + ChatColor.WHITE + ">> " + ChatColor.GRAY + "%s");
    }




}
