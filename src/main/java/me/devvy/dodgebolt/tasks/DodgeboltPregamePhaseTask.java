package me.devvy.dodgebolt.tasks;

import me.devvy.dodgebolt.game.DodgeboltGame;
import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class DodgeboltPregamePhaseTask extends DodgeboltPhaseTask {

    public final static int PERIOD = 20;
    private int TIME = 5;

    public DodgeboltPregamePhaseTask(DodgeboltGame game) {
        super(game);

        if (inOvertime() || onMatchPoint()) {
            TIME = 7;
            for (Player p : game.getAllPlayersInStadium())
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
        }
    }

    private boolean inOvertime() {
        return game.getTeam1().getScore() == game.getTeam2().getScore() && game.getTeam1().getScore() + 1 >= game.getStartingRoundsToWin();
    }

    private boolean onMatchPoint() {
        return game.getTeam1().getScore() + 1 == game.getRoundsToWin() || game.getTeam2().getScore() + 1 == game.getRoundsToWin();
    }

    public float getSoundPitch(int timeLeft) {
        if (timeLeft > 3)
            return 1;
        else
            return 1.6f - .2f * timeLeft;
    }

    public String getTitleText(int timeLeft) {

        if (timeLeft > 4 && inOvertime())
            return ChatColor.RED + "OVERTIME!";

        if (timeLeft > 4 && onMatchPoint())
            return ChatColor.AQUA + "MATCH POINT!";

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
            for (Player player : game.getAllPlayersInStadium()) {
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
