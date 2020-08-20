package me.devvy.dodgebolt.tasks;

import me.devvy.dodgebolt.game.DodgeboltGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class DodgeboltPregamePhaseTask extends DodgeboltPhaseTask {

    public final static int PERIOD = 20;
    private final int TIME = 5;

    public DodgeboltPregamePhaseTask(DodgeboltGame game) {
        super(game);
    }

    public float getSoundPitch(int timeLeft) {
        if (timeLeft > 3)
            return 1;
        else
            return 1.6f - .2f * timeLeft;
    }

    public String getTitleText(int timeLeft) {
        if (timeLeft > 2)
            return ChatColor.AQUA + "Starting...";
        else if (timeLeft == 2)
            return ChatColor.YELLOW + "Ready...";
        else if (timeLeft == 1)
            return ChatColor.GOLD + "Set...";
        else
            return "";

    }

    @Override
    protected void runGameLoop() {

        int secondsLeft = TIME - unpausedElapsed;

        if (secondsLeft > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, getSoundPitch(secondsLeft));
                player.sendTitle(getTitleText(secondsLeft), ChatColor.YELLOW + "> " + ChatColor.RED + secondsLeft +  ChatColor.YELLOW + " <", 1, 40, 20);
            }
        }

        if (unpausedElapsed >= TIME)
            game.exitPregamePhase();
    }

    @Override
    protected void runPauseLoop() {

    }
}
