package me.devvy.dodgebolt.tasks;

import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class DodgeboltPhaseTask extends BukkitRunnable {

    protected final DodgeboltGame game;
    protected boolean paused = false;
    protected int totalElapsed = 0;  // Total amount of ticks this has been running
    protected int unpausedElapsed = 0;  // Total amount of ticks this has been running NOT Paused

    public DodgeboltPhaseTask(DodgeboltGame game) {
        this.game = game;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getTotalElapsed() {
        return totalElapsed;
    }

    @Override
    public void run() {
        if (!paused) {
            runGameLoop();
            unpausedElapsed++;
        }
        else
            runPauseLoop();

        totalElapsed++;

    }

    /**
     * What should we do when the game timer is running?
     */
    protected abstract void runGameLoop();

    /**
     * What should we do when the game timer is paused?
     */
    protected abstract void runPauseLoop();
}
