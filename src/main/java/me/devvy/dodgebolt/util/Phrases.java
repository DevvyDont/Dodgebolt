package me.devvy.dodgebolt.util;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.team.Team;
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
            "%s" + ChatColor.DARK_GRAY + " 200 pumped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " finally killed " + "%s",
            "%s" + ChatColor.DARK_GRAY + " is better at block game than " + "%s",
            "%s" + ChatColor.DARK_GRAY + " schooled " + "%s",
            "%s" + ChatColor.DARK_GRAY + " rejected " + "%s",
            "%s" + ChatColor.DARK_GRAY + " friendzoned " + "%s",
            "%s" + ChatColor.DARK_GRAY + " showed " + "%s" + ChatColor.DARK_GRAY + " the way",
            "%s" + ChatColor.DARK_GRAY + " decided it was " + "%s" + ChatColor.DARK_GRAY + "'s time to go",
            "%s" + ChatColor.DARK_GRAY + " left " + "%s" + ChatColor.DARK_GRAY + " on read",
            "%s" + ChatColor.DARK_GRAY + " sent " + "%s" + ChatColor.DARK_GRAY + " to a better place",
            "%s" + ChatColor.DARK_GRAY + " sent " + "%s" + ChatColor.DARK_GRAY + " back to the playground",
            "%s" + ChatColor.DARK_GRAY + " read " + "%s" + ChatColor.DARK_GRAY + " like a book",
            "%s" + ChatColor.DARK_GRAY + " put " + "%s" + ChatColor.DARK_GRAY + " to sleep",
            "%s" + ChatColor.DARK_GRAY + " clipped on " + "%s",
            "%s" + ChatColor.DARK_GRAY + " steve diffed " + "%s",
            "%s" + ChatColor.DARK_GRAY + " 1 tapped " + "%s",
            "%s" + ChatColor.DARK_GRAY + " ratio " + "%s",
            "%s" + ChatColor.DARK_GRAY + " unfollowed " + "%s" + ChatColor.DARK_GRAY + " on twitter",
    };

    private final static String[] TEAMKILL_PHRASES = new String[]{
            "%s" + ChatColor.DARK_GRAY + " boxed their teammate " + "%s" + ChatColor.DARK_GRAY + " like a fish",
            "%s" + ChatColor.DARK_GRAY + " didn't realize friendly fire was on, sorry " + "%s",
            "%s" + ChatColor.DARK_GRAY + " obliterated " + "%s" + ChatColor.DARK_GRAY + " so the other team didn't have to",
            "%s" + ChatColor.DARK_GRAY + " ????????? " + "%s",
            "%s" + ChatColor.DARK_GRAY + " forgot this was a team game. RIP " + "%s",
    };

    public static String getRandomSuicidePhrase(Player player) {
        Team pTeam = Dodgebolt.getInstance().getGame().getPlayerTeam(player);
        String displayName = pTeam != null ? pTeam.getCleanMemberString(player) : ChatColor.WHITE + player.getName();
        String phrase = SUICIDE_PHRASES[(int) (Math.random() * SUICIDE_PHRASES.length)];
        return String.format(phrase, displayName);
    }

    public static String getRandomKilledPhrase(Player player, Player killer) {
        Team pTeam = Dodgebolt.getInstance().getGame().getPlayerTeam(player);
        Team kTeam = Dodgebolt.getInstance().getGame().getPlayerTeam(killer);

        String pDisplayName = pTeam != null ? pTeam.getCleanMemberString(player) : ChatColor.WHITE + player.getName();
        String kDisplayName = kTeam != null ? kTeam.getCleanMemberString(killer) : ChatColor.WHITE + killer.getName();

        String phrase = KILLED_PHRASES[(int) (Math.random() * KILLED_PHRASES.length)];
        return String.format(phrase, kDisplayName, pDisplayName);
    }

    public static String getRandomTeamKillPhrase(Player player, Player killer) {

        Team pTeam = Dodgebolt.getInstance().getGame().getPlayerTeam(player);

        String pDisplayName = pTeam != null ? pTeam.getCleanMemberString(player) : ChatColor.WHITE + player.getName();
        String kDisplayName = pTeam != null ? pTeam.getCleanMemberString(killer) : ChatColor.WHITE + killer.getName();

        String phrase = TEAMKILL_PHRASES[(int) (Math.random() * TEAMKILL_PHRASES.length)];
        return String.format(phrase, kDisplayName, pDisplayName);
    }

}
