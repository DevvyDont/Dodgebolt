package me.devvy.dodgebolt.tasks;

import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DodgeboltIntermissionPhaseTask extends DodgeboltPhaseTask {

    public static final int PERIOD = 20;
    private final int TIME = 7;

    public DodgeboltIntermissionPhaseTask(DodgeboltGame game) {
        super(game);
    }

    @Override
    protected void runGameLoop() {

        for (Player player : Bukkit.getOnlinePlayers())
            player.sendActionBar(ChatColor.GRAY + "Next round in " + ChatColor.RED + (TIME - unpausedElapsed) + "s");

        if (TIME - unpausedElapsed <= 3)
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isDead())
                    player.spigot().respawn();

        if (unpausedElapsed >= TIME) {
            game.exitIntermissionPhase();
            cancel();
        }
    }

    @Override
    protected void runPauseLoop() {

    }
}
