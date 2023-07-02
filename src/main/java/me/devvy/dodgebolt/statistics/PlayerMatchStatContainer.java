package me.devvy.dodgebolt.statistics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Keeps track of stats such as
 * - Damage dealt to enemies
 * - Kills
 * - Team kills
 * - Assists
 * - Deaths
 * - Arrows fired
 * - Arrows hit against enemies
 * - Rounds won
 * - Aces
 * - Clutches
 * - Flawlesses
 */
public class PlayerMatchStatContainer {

    protected final UUID owner;
    protected String name = "Unknown Player";

    public PlayerMatchStatContainer(UUID owner) {
        this.owner = owner;
    }

    protected int damageDealt = 0;
    protected int kills = 0;
    protected int teamKills = 0;
    protected int assists = 0;
    protected int deaths = 0;
    protected int arrowsFired = 0;
    protected int arrowsLanded = 0;
    protected int roundsPlayed = 0;
    protected int roundsWon = 0;
    protected int aces = 0;
    protected int clutches = 0;
    protected int flawlesses = 0;

    public UUID getOwner() {
        return owner;
    }

    public String getName() {

        Player p = Bukkit.getPlayer(owner);

        if (p != null)
            name = p.getName();

        return name;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void addDamageDealt(int damage) {
        this.damageDealt += damage;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills += 1;
    }

    public int getTeamKills() {
        return teamKills;
    }

    public void addTeamKill() {
        this.teamKills += 1;
    }

    public int getAssists() {
        return assists;
    }

    public void addAssist() {
        this.assists += 1;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths += 1;
    }

    public int getArrowsFired() {
        return arrowsFired;
    }

    public void addArrowFired() {
        arrowsFired += 1;
    }

    public int getArrowsLanded() {
        return arrowsLanded;
    }

    public void addArrowLanded() {
        this.arrowsLanded += 1;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void addRoundPlay() {
        roundsPlayed += 1;
    }

    public int getRoundsWon() {
        return roundsWon;
    }

    public void addRoundWin() {
        roundsWon += 1;
    }

    public int getAces() {
        return aces;
    }

    public void addAce() {
        aces += 1;
    }

    public int getClutches() {
        return clutches;
    }

    public void addClutch() {
        clutches += 1;
    }

    public int getFlawlesses() {
        return flawlesses;
    }

    public void addFlawless() {
        flawlesses += 1;
    }

    /**
     * Gets accuracy as a decimal from 0-1 with no rounding
     *
     * @return
     */
    public double getRawAccuracy() {
        if (getArrowsFired() <= 0)
            return 0d;

        return (double)getArrowsLanded() / (double)getArrowsFired();
    }

    /**
     * Get clean accuracy formated as an int from 0-100
     *
     * @return
     */
    public int getAccuracy() {
        double accAsInt = getRawAccuracy()*100;  // Multiply accuracy by 100 to go from 0-100
        return (int)accAsInt;  // Drop numbers after the decimal
    }


    /**
     * Calculate a score based on the stats we have stored, we use this as a leaderboard sorting system
     *
     * @return
     */
    public int calculateScore() {
        int total = 0;

        // Add 4 points per damage dealt (a 1 shot kill will be 80 points)
        total += (damageDealt*4);

        // Add 20 points for every finishing blow
        total += (kills*20);

        // Add 5 points per assist
        total += (assists*5);

        // Add 50 points for an ace
        total += (aces*50);

        // Add 30 points for a clutch
        total += (clutches*30);

        // Add 20 points for a flawless
        total += (flawlesses*20);

        return total;
    }

    @Override
    public String toString() {
        return "PlayerMatchStatContainer[UUID="+owner+"]{" +
                "damageDealt=" + damageDealt +
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

    /**
     * Overridable method just in case we want to do anything
     */
    public void save(Player player) {
    }
}
