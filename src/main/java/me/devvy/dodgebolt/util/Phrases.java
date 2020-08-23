package me.devvy.dodgebolt.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Phrases {

    private final static String[] SUICIDE_PHRASES = new String[]{
            "%s" + ChatColor.DARK_GRAY + " couldn't take the heat...",
            "%s" + ChatColor.DARK_GRAY + " was feeling the pressure",
            "%s" + ChatColor.DARK_GRAY + " has fallen and can't get up",
            "%s" + ChatColor.DARK_GRAY + " slipped",
            "%s" + ChatColor.DARK_GRAY + " is burnt toast",
            "%s" + ChatColor.DARK_GRAY + " was BBQ'd",
            "%s" + ChatColor.DARK_GRAY + " took the L",
            "%s" + ChatColor.DARK_GRAY + " played themselves",
            "%s" + ChatColor.DARK_GRAY + " encountered gravity for the first time",
            "%s" + ChatColor.DARK_GRAY + " did not want to live on this planet anymore",
            "%s" + ChatColor.DARK_GRAY + " took the easy way out",
            "%s" + ChatColor.DARK_GRAY + " thought they were in creative mode",
            "%s" + ChatColor.DARK_GRAY + " fought with gravity... and lost",
            "%s" + ChatColor.DARK_GRAY + " wanted to go for a swim",
            "%s" + ChatColor.DARK_GRAY + " should stick to peaceful mode",
            "%s" + ChatColor.DARK_GRAY + " is bad at block game",
            "%s" + ChatColor.DARK_GRAY + " isn't that bright...",
            "%s" + ChatColor.DARK_GRAY + " went 3IQ",
    };

    private final static String[] KILLED_PHRASES = new String[]{
            "%s" + ChatColor.DARK_GRAY + " sniped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " 720 instaswapped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " boatbanged " + "%s",
            "%s" + ChatColor.DARK_GRAY + " clowned " + "%s",
            "%s" + ChatColor.DARK_GRAY + " quickscoped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " destroyed " + "%s",
            "%s" + ChatColor.DARK_GRAY + " outplayed " + "%s",
            "%s" + ChatColor.DARK_GRAY + " shot " + "%s",
            "%s" + ChatColor.DARK_GRAY + " just wanted to be friends with " + "%s",
            "%s" + ChatColor.DARK_GRAY + " reality checked " + "%s",
            "%s" + ChatColor.DARK_GRAY + " ripped a 90 on " + "%s",
            "%s" + ChatColor.DARK_GRAY + " finally killed " + "%s",
            "%s" + ChatColor.DARK_GRAY + " is better at block game than " + "%s",
            "%s" + ChatColor.DARK_GRAY + " schooled " + "%s",
            "%s" + ChatColor.DARK_GRAY + " said no to " + "%s",
            "%s" + ChatColor.DARK_GRAY + " showed " + "%s" + ChatColor.DARK_GRAY + " the way",
            "%s" + ChatColor.DARK_GRAY + " decided it was " + "%s" + ChatColor.DARK_GRAY + "'s time to go",
            "%s" + ChatColor.DARK_GRAY + " left " + "%s" + ChatColor.DARK_GRAY + " on read",
            "%s" + ChatColor.DARK_GRAY + " sent " + "%s" + ChatColor.DARK_GRAY + " to a better place",
            "%s" + ChatColor.DARK_GRAY + " sent " + "%s" + ChatColor.DARK_GRAY + " back to the playground",
    };

    private final static String[] TEAMKILL_PHRASES = new String[]{
            ChatColor.DARK_GRAY + "step bro:  %s" + ChatColor.DARK_GRAY + " didn't know what " + "%s" + ChatColor.DARK_GRAY + " was doing",
            "%s" + ChatColor.DARK_GRAY + " boxed their teammate " + "%s" + ChatColor.DARK_GRAY + " like a fish",
            "%s" + ChatColor.DARK_GRAY + " didn't realize friendly fire was on, sorry " + "%s",
            "%s" + ChatColor.DARK_GRAY + " obliterated " + "%s" + ChatColor.DARK_GRAY + " so the other team didn't have to",
            "%s" + ChatColor.DARK_GRAY + " ????? " + "%s",
            "%s" + ChatColor.DARK_GRAY + " forgot this was a team game. RIP " + "%s",
    };

    public static String getRandomSuicidePhrase(Player player) {
        String phrase = SUICIDE_PHRASES[(int) (Math.random() * SUICIDE_PHRASES.length)];
        return String.format(phrase, player.getDisplayName());
    }

    public static String getRandomKilledPhrase(Player player, Player killer) {
        String phrase = KILLED_PHRASES[(int) (Math.random() * KILLED_PHRASES.length)];
        return String.format(phrase, killer.getDisplayName(), player.getDisplayName());
    }

    public static String getRandomTeamKillPhrase(Player player, Player killer) {
        String phrase = TEAMKILL_PHRASES[(int) (Math.random() * TEAMKILL_PHRASES.length)];
        return String.format(phrase, killer.getDisplayName(), player.getDisplayName());
    }

}
