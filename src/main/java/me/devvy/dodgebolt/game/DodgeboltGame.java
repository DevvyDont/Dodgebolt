package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.team.Team;
import org.bukkit.*;

public class DodgeboltGame {

    private DodgeboltArena arena;
    private Team team1;
    private Team team2;

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
    }

    public void cleanup() {
        arena.destroyArena();
    }
}
