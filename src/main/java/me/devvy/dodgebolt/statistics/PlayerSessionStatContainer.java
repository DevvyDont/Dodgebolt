package me.devvy.dodgebolt.statistics;

import java.util.UUID;

/**
 * Keeps track of stats within a match container and additionally
 * - Matches won
 * - Crowns achieved (MVP + win counts as 2)
 */
public class PlayerSessionStatContainer extends PlayerMatchStatContainer {

    public PlayerSessionStatContainer(UUID owner) {
        super(owner);
    }

    protected int matchesPlayed = 0;
    protected int matchesWon = 0;
    protected int crownsAchieved = 0;
    protected int matchMVPs = 0;

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void addMatchPlayed() {
        matchesPlayed += 1;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void addMatchWin() {
        matchesWon += 1;
    }

    public int getCrownsAchieved() {
        return crownsAchieved;
    }

    public void addCrownAchieved() {
        crownsAchieved += 1;
    }

    public int getMatchMVPs() {
        return matchMVPs;
    }

    public void addMatchMVPs() {
        matchMVPs += 1;
    }

    @Override
    public String toString() {
        return "PlayerSessionStatContainer[UUID="+owner+"]{" +
                "matchesPlayed=" + matchesPlayed +
                "matchesWon=" + matchesWon +
                ", crownsAchieved=" + crownsAchieved +
                ", matchMVPs=" + matchMVPs +
                ", damageDealt=" + damageDealt +
                ", kills=" + kills +
                ", teamKills=" + teamKills +
                ", assists=" + assists +
                ", deaths=" + deaths +
                ", arrowsFired=" + arrowsFired +
                ", arrowsLanded=" + arrowsLanded +
                ", roundsPlayed=" + roundsPlayed +
                ", roundsWon=" + roundsWon +
                ", aces=" + aces +
                ", clutches=" + clutches +
                ", flawlesses=" + flawlesses +
                '}';
    }
}
