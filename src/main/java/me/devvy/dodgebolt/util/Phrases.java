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
    };

    private final static String[] KILLED_PHRASES = new String[]{
            "%s" + ChatColor.DARK_GRAY + " sniped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " 720 instaswapped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " clowned " + "%s",
            "%s" + ChatColor.DARK_GRAY + " quickscoped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " destroyed " + "%s",
            "%s" + ChatColor.DARK_GRAY + " outplayed " + "%s",
            "%s" + ChatColor.DARK_GRAY + " shot " + "%s",
    };

    public static String getRandomSuicidePhrase(Player player) {
        String phrase = SUICIDE_PHRASES[(int) (Math.random() * SUICIDE_PHRASES.length)];
        return String.format(phrase, player.getDisplayName());
    }

    public static String getRandomKilledPhrase(Player player, Player killer) {
        String phrase = KILLED_PHRASES[(int) (Math.random() * KILLED_PHRASES.length)];
        return String.format(phrase, killer.getDisplayName(), player.getDisplayName());
    }

}
