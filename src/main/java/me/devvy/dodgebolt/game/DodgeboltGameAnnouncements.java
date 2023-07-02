package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.*;
import me.devvy.dodgebolt.team.Team;
import me.devvy.dodgebolt.util.ColorTranslator;
import me.devvy.dodgebolt.util.Fireworks;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DodgeboltGameAnnouncements implements Listener {

    public DodgeboltGameAnnouncements() {
        Dodgebolt.getInstance().getServer().getPluginManager().registerEvents(this, Dodgebolt.getInstance());
    }

    /**
     * What should we do in all instances where a team won a round with no specific conditions?
     *
     * @param event
     */
    @EventHandler
    public void OnRoundFinished(RoundEndedEvent event) {

        // Pretty winning fx
        for (Player winningMember : event.getWinner().getMembersAsPlayers())
            Fireworks.spawnVictoryFireworks(winningMember, ColorTranslator.translateChatColorToColor(event.getWinner().getTeamColor()));
    }

    @EventHandler
    public void onPlayerAce(DodgeboltPlayerAcedEvent event) {
        // Announce an ace win
        DodgeboltGame game = Dodgebolt.getInstance().getGame();
        Team winningTeam = game.getPlayerTeam(event.getPlayer());
        Team losingTeam = game.getOpposingTeam(winningTeam);


        game.broadcast(String.format("%s[%s!%s] %s%s %sACED %sand won the round for %s%s%s!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, winningTeam.getTeamColor(), event.getPlayer().getName(), ChatColor.GOLD, ChatColor.GRAY, winningTeam.getTeamColor(), winningTeam.getName(), ChatColor.GRAY));
        game.getPlayerTeam(event.getPlayer()).playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, .75f,1);
        game.playSoundToSpectators(Sound.UI_TOAST_CHALLENGE_COMPLETE, .75f,1);
        losingTeam.playSound(Sound.ENTITY_WITHER_DEATH, .75f, 1);

        winningTeam.sendTitle(String.format("%s%sACE", ChatColor.GOLD, ChatColor.BOLD), String.format("%s%s %skilled the entire enemy team!", winningTeam.getTeamColor(), event.getPlayer().getName(), ChatColor.GRAY), 10, 100, 40);
        game.sendTitleToSpectators(String.format("%s%sACE", ChatColor.GOLD, ChatColor.BOLD), String.format("%s%s %skilled the entire enemy team!", winningTeam.getTeamColor(), event.getPlayer().getName(), ChatColor.GRAY), 10, 100, 40);
        losingTeam.sendTitle(String.format("%s%sACE", ChatColor.DARK_PURPLE, ChatColor.BOLD), String.format("%s%s %skilled the entire enemy team!", winningTeam.getTeamColor(), event.getPlayer().getName(), ChatColor.GRAY), 10, 100, 40);
    }

    @EventHandler
    public void onTeamAce(DodgeboltTeamAcedEvent event) {

        DodgeboltGame game = Dodgebolt.getInstance().getGame();
        Team losingTeam = game.getOpposingTeam(event.getTeam());

        game.broadcast(String.format("%s[%s!%s] All members of %s%s %sgot a kill and won the round!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY));
        event.getTeam().playSound(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
        game.playSoundToSpectators(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
        losingTeam.playSound(Sound.ENTITY_ENDERMAN_DEATH, .75f, .75f);

        event.getTeam().sendTitle(String.format("%s%sTEAM ACE", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall got one kill!", ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY), 10, 100, 40);
        game.sendTitleToSpectators(String.format("%s%sTEAM ACE", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall got one kill!", ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY), 10, 100, 40);
        losingTeam.sendTitle(String.format("%s%sTEAM ACE", ChatColor.RED, ChatColor.BOLD), String.format("%sTeam %s%s %sall got one kill!", ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY), 10, 100, 40);

    }

    @EventHandler
    public void onTeamFlawless(DodgeboltTeamFlawlessedEvent event) {

        DodgeboltGame game = Dodgebolt.getInstance().getGame();
        Team losingTeam = game.getOpposingTeam(event.getTeam());

        game.broadcast(String.format("%s[%s!%s] All members of %s%s %slived and won the round!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY));
        event.getTeam().playSound(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
        game.playSoundToSpectators(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
        losingTeam.playSound(Sound.ENTITY_ENDERMAN_DEATH, .75f, .75f);

        event.getTeam().sendTitle(String.format("%s%sFLAWLESS", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall stayed alive!", ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY), 10, 100, 40);
        game.sendTitleToSpectators(String.format("%s%sFLAWLESS", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall stayed alive!", ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY), 10, 100, 40);
        losingTeam.sendTitle(String.format("%s%sFLAWLESS", ChatColor.RED, ChatColor.BOLD), String.format("%sTeam %s%s %sall stayed alive!", ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY), 10, 100, 40);
    }

    @EventHandler
    public void onTeamClutched(DodgeboltTeamClutchedEvent event) {

        DodgeboltGame game = Dodgebolt.getInstance().getGame();
        Team losingTeam = game.getOpposingTeam(event.getTeam());

        game.broadcast(String.format("%s[%s!%s] %s%s %sclutched and won the round for %s%s%s!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, event.getTeam().getTeamColor(), event.getClutchPlayer().getName(), ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY));
        event.getTeam().playSound(Sound.ENTITY_VILLAGER_CELEBRATE, .75f,1);
        game.playSoundToSpectators(Sound.ENTITY_VILLAGER_CELEBRATE, .75f,1);
        losingTeam.playSound(Sound.ENTITY_ENDERMAN_DEATH, .75f, 1.5f);

        event.getTeam().sendTitle(String.format("%s%sCLUTCH", ChatColor.GREEN, ChatColor.BOLD), String.format("%s%s %swon the round!", event.getTeam().getTeamColor(), event.getClutchPlayer().getName(), ChatColor.GRAY), 10, 100, 40);
        game.sendTitleToSpectators(String.format("%s%sCLUTCH", ChatColor.GREEN, ChatColor.BOLD), String.format("%s%s %swon the round!", event.getTeam().getTeamColor(), event.getClutchPlayer().getName(), ChatColor.GRAY), 10, 100, 40);
        losingTeam.sendTitle(String.format("%s%sCLUTCH", ChatColor.RED, ChatColor.BOLD), String.format("%s%s %swon the round!", event.getTeam().getTeamColor(), event.getClutchPlayer().getName(), ChatColor.GRAY), 10, 100, 40);
    }

    @EventHandler
    public void onTeamWonNormalRound(DodgeboltTeamWonBasicRoundEvent event) {
        DodgeboltGame game = Dodgebolt.getInstance().getGame();
        Team losingTeam = game.getOpposingTeam(event.getTeam());

        game.broadcast(String.format("%s[%s!%s] %s%s %swon the round!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, event.getTeam().getTeamColor(), event.getTeam().getName(), ChatColor.GRAY));
        event.getTeam().playSound(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
        game.playSoundToSpectators(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
        losingTeam.playSound(Sound.ENTITY_VILLAGER_NO, .75f, .75f);

        event.getTeam().sendTitle(String.format("%sROUND WIN", ChatColor.GREEN), "", 10, 100, 40);
        game.sendTitleToSpectators(String.format("%sROUND WIN", ChatColor.GREEN), "", 10, 100, 40);
        losingTeam.sendTitle(String.format("%sROUND LOSS", ChatColor.RED), "", 10, 100, 40);
    }
}
