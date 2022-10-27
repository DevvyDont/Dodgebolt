package me.devvy.dodgebolt.tasks;

import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DodgeboltIngamePhaseTask extends DodgeboltPhaseTask {

    public static final int PERIOD = 20;
    public static final int SHRINK_INTERVAL = 20;
    public static final int OVERTIME_SHATTER_DELAY = 150;

    public DodgeboltIngamePhaseTask(DodgeboltGame game) {
        super(game);
    }

    @Override
    protected void runGameLoop() {

        if (unpausedElapsed > 0 && unpausedElapsed % SHRINK_INTERVAL == 0)
            game.getStadium().getArena().shrinkArena();

        if (unpausedElapsed == OVERTIME_SHATTER_DELAY)
            game.getStadium().getArena().startOvertimeShatter();

        for (Player player : Bukkit.getOnlinePlayers())
            player.sendActionBar(game.getBothTeamAliveCountString());
    }

    @Override
    protected void runPauseLoop() {

    }


}
