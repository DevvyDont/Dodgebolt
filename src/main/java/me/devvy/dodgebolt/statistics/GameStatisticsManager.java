package me.devvy.dodgebolt.statistics;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.*;
import me.devvy.dodgebolt.game.DodgeboltGame;
import me.devvy.dodgebolt.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * Handles the four instances where we need to track stats:
 * - During a round (We use this for determining aces/flawlesses etc
 * - During a match (To display a scoreboard and determine a match mvp
 * - Server session (When server resets, this board will reset)
 * - Global stats   (Can be seen on a crown after winning/mvping)
 */
public class GameStatisticsManager implements Listener {

    public GameStatisticsManager() {
        Dodgebolt.getInstance().getServer().getPluginManager().registerEvents(this, Dodgebolt.getInstance());
    }

    /**
     * Returns a set of UUIDs that have stats on file for the session
     *
     * @return
     */
    public Collection<UUID> getTrackedPlayersThisSession() {
        return sessionStats.keySet();
    }

    public Collection<PlayerSessionStatContainer> getAllSessionContainers() {
        return sessionStats.values();
    }

    public void resetSessionStats() {
        sessionStats.clear();
    }

    // Several data structures we will use to store stats

    Map<Integer, RoundStatistics> roundTimeline = new HashMap<>();
    Map<UUID, PlayerMatchStatContainer> matchStats = new HashMap<>();
    Map<UUID, PlayerSessionStatContainer> sessionStats = new HashMap<>();

    /**
     * Retrieve a player's global stats for modification. Remember to save it when you are done!
     *
     * @param player
     * @return
     */
    public PlayerGlobalStatContainer getPlayerGlobalStats(Player player) {
        return PlayerGlobalStatContainer.fromPersistentDataContainer(player);
    }

    /**
     *
     * Retrieve the stats for a player while the server has been online
     *
     * @param player Player instance
     * @return
     */
    public PlayerSessionStatContainer getServerSessionStats(Player player) {
        return getServerSessionStats(player.getUniqueId());
    }

    /**
     *
     * Retrieve the stats for a player while the server has been online
     *
     * @param playerID Unique ID of a player
     * @return
     */
    public PlayerSessionStatContainer getServerSessionStats(UUID playerID) {
        if (sessionStats.containsKey(playerID))
            return sessionStats.get(playerID);

        sessionStats.put(playerID, new PlayerSessionStatContainer(playerID));
        return getServerSessionStats(playerID);
    }

    /**
     * Retrieve the stats for a player during the current match
     *
     * @param player
     * @return
     */
    public PlayerMatchStatContainer getMatchStats(Player player) {
        if (matchStats.containsKey(player.getUniqueId()))
            return matchStats.get(player.getUniqueId());

        matchStats.put(player.getUniqueId(), new PlayerSessionStatContainer(player.getUniqueId()));
        return getMatchStats(player);
    }

    /**
     * Retrieve the stats for a player only for a specific round during the current game
     *
     * @param player
     * @return
     */
    public PlayerMatchStatContainer getRoundStats(Player player, int round) {

        if (!roundTimeline.containsKey(round))
            roundTimeline.put(round, new RoundStatistics());

        return roundTimeline.get(round).getRoundPlayerStats(player.getUniqueId());
    }

    /**
     * Gets the stats of a player during the last round recorded
     *
     * @param player
     * @return
     */
    public PlayerMatchStatContainer getRoundStats(Player player) {
        return getRoundStats(player, Dodgebolt.getInstance().getGame().getCurrentRoundNumber());
    }

    public RoundStatistics getRoundStatistics(int roundNumber) {
        return roundTimeline.get(roundNumber);
    }


    public Collection<PlayerMatchStatContainer> getCurrentMatchStatContainers() {
        return matchStats.values();
    }

    /**
     * Given context, get a list of all stat containers that should be updated when a certain event happens
     * for a specific player
     *
     * @param player
     * @return
     */
    public Collection<PlayerMatchStatContainer> getRelevantStatContainers(Player player) {
        List<PlayerMatchStatContainer> containers = new ArrayList<>();

        // Add the match stat container and the session stat container
        containers.add(getServerSessionStats(player));
        containers.add(getMatchStats(player));
        // Whatever the current round is add that container
        containers.add(getRoundStats(player));
        // Get their global container and add it
        containers.add(getPlayerGlobalStats(player));
        return containers;
    }

    public PlayerMatchStatContainer getMatchMVP() {
        // Get all player stats, sort it by score, and get first entry
        List<PlayerMatchStatContainer> entries = new ArrayList<>(getCurrentMatchStatContainers());
        entries.sort(Comparator.comparing(PlayerMatchStatContainer::calculateScore).reversed());
        return entries.get(0);
    }


    public void clearRoundStats() {
        roundTimeline.clear();
    }

    public void clearMatchStats() {
        matchStats.clear();
    }

    // Completely wipe all stats for a new game
    public void clearMatchAndRoundStats() {
        clearMatchStats();
        clearRoundStats();
    }

    /////////////////////////////////////
    // EVENTS TO MODIFY CONTAINERS
    /////////////////////////////////////

    /**
     * First, we need to listen for when a player is eliminated. The player that died will ALWAYS incur a death and
     * if a killer exists, we need to give them credit for the kill. Also consider the case where teamkills happen
     *
     * @param event
     */
    @EventHandler
    public void onPlayerKill(DodgeboltPlayerEliminatedEvent event) {

        Collection<PlayerMatchStatContainer> deadPlayersContainers = getRelevantStatContainers(event.getPlayer());

        // Add a death
        for (PlayerMatchStatContainer container : deadPlayersContainers) {
            container.addDeath();
            container.save(event.getPlayer());
        }

        // Was there a killer?
        if (event.getPlayer().getKiller() == null)
            return;

        Player killer = event.getPlayer().getKiller();
        Collection<PlayerMatchStatContainer> killerContainers = getRelevantStatContainers(killer);
        DodgeboltGame game = Dodgebolt.getInstance().getGame();

        // Get the people who deserve an assist
        for (UUID assister : getRoundStatistics(game.getCurrentRoundNumber()).getPlayersEligibleForAssist(event.getPlayer())) {

            // Ignore the person who got the killing blow
            if (assister.equals(killer.getUniqueId()))
                continue;

            Player assisterAsPlayer = Bukkit.getPlayer(assister);
            if (assisterAsPlayer == null)
                continue;

            Collection<PlayerMatchStatContainer> assistersContainers = getRelevantStatContainers(assisterAsPlayer);

            // Add an assist
            for (PlayerMatchStatContainer container : assistersContainers) {
                container.addAssist();
                container.save(assisterAsPlayer);
            }

        }

        for (PlayerMatchStatContainer container : killerContainers) {

            // If they are on the same team, add TK, otherwise add a kill
            if (game.getPlayerTeam(killer) == game.getPlayerTeam(event.getPlayer()))
                container.addTeamKill();
            else
                container.addKill();

            container.save(killer);
        }

    }

    /**
     * Listen for when a player shoots a dodgebolt arrow
     *
     * @param event
     */
    @EventHandler
    public void onDodgeboltArrowFired(PlayerFiredDodgeboltArrowEvent event) {

        Collection<PlayerMatchStatContainer> shootersContainers = getRelevantStatContainers(event.getShooter());

        // Add a shot
        for (PlayerMatchStatContainer container : shootersContainers) {
            container.addArrowFired();
            container.save(event.getShooter());
        }

    }

    /**
     * Listen for when a player lands a dodgebolt arrow
     *
     * @param event
     */
    @EventHandler
    public void onDodgeboltArrowHitPlayer(PlayerHitDodgeboltArrowEvent event) {

        // Completely ignore events where the teams of the player and receiver are on the same team.
        // This will also include suicides as a side effect (we want that)
        if (Dodgebolt.getInstance().getGame().getPlayerTeam(event.getShooter()) == Dodgebolt.getInstance().getGame().getPlayerTeam(event.getVictim()))
            return;

        // Tell the round stat holder that the shooter is eligible for an assist
        getRoundStatistics(Dodgebolt.getInstance().getGame().getCurrentRoundNumber()).registerEligibleForAssist(event.getShooter(), event.getVictim());

        Collection<PlayerMatchStatContainer> shootersContainers = getRelevantStatContainers(event.getShooter());

        // Add a land
        for (PlayerMatchStatContainer container : shootersContainers) {
            container.addArrowLanded();
            container.addDamageDealt((int) event.getDamage());
            container.save(event.getShooter());
        }

    }

    @EventHandler
    public void onPlayerAced(DodgeboltPlayerAcedEvent event) {

        Collection<PlayerMatchStatContainer> acerContainers = getRelevantStatContainers(event.getPlayer());

        // Add an ace
        for (PlayerMatchStatContainer container : acerContainers) {
            container.addAce();
            container.save(event.getPlayer());
        }

    }

    @EventHandler
    public void onPlayerClutched(DodgeboltTeamClutchedEvent event) {

        Collection<PlayerMatchStatContainer> acerContainers = getRelevantStatContainers(event.getClutchPlayer());

        // Add a clutch
        for (PlayerMatchStatContainer container : acerContainers) {
            container.addClutch();
            container.save(event.getClutchPlayer());
        }

    }

    @EventHandler
    public void onTeamFlawlessed(DodgeboltTeamFlawlessedEvent event) {

        for (Player p : event.getTeam().getMembersAsPlayers()) {

            Collection<PlayerMatchStatContainer> acerContainers = getRelevantStatContainers(p);

            // Add a flawless
            for (PlayerMatchStatContainer container : acerContainers) {
                container.addFlawless();
                container.save(p);
            }

        }

    }

    /**
     * Listen for when a round ends, we need to add round wins and round plays
     *
     * @param event
     */
    @EventHandler
    public void onRoundEnded(RoundEndedEvent event) {

        // Loop through both teams and add a round played and round win for winners
        for (Player player : event.getLoser().getMembersAsPlayers()) {

            Collection<PlayerMatchStatContainer> losersContainers = getRelevantStatContainers(player);

            // Add a round play
            for (PlayerMatchStatContainer container : losersContainers) {
                container.addRoundPlay();
                container.save(player);
            }

        }

        for (Player player : event.getWinner().getMembersAsPlayers()) {

            Collection<PlayerMatchStatContainer> winnersContainers = getRelevantStatContainers(player);

            // Add a round play
            for (PlayerMatchStatContainer container : winnersContainers) {
                container.addRoundPlay();
                container.addRoundWin();
                container.save(player);
            }

        }

    }

    /**
     * Listen for when a game ends, we need to add game plays and game wins
     *
     * @param event
     */
    @EventHandler
    public void onGameEnded(GameEndedEvent event) {

        // Determine if there is a winner
        Team winner = null;
        if (event.getGame().getTeam1().getScore() > event.getGame().getTeam2().getScore())
            winner = event.getGame().getTeam1();
        else if (event.getGame().getTeam2().getScore() > event.getGame().getTeam1().getScore())
            winner = event.getGame().getTeam2();

        // Loop through everyone that played and give them a match play
        for (Player player : event.getGame().getAllPlayersOnTeams()) {

            Collection<PlayerMatchStatContainer> playerContainer = getRelevantStatContainers(player);

            // Add a round play
            for (PlayerMatchStatContainer container : playerContainer) {

                if (!(container instanceof PlayerSessionStatContainer))
                    continue;

                ((PlayerSessionStatContainer) container).addMatchPlayed();

                // If this player is on the winning team, give them a win
                if (winner != null && winner.isMember(player))
                    ((PlayerSessionStatContainer) container).addMatchWin();

                container.save(player);
            }

        }

    }

    @EventHandler
    public void onCrownObtained(PlayerObtainedCrownEvent event) {

        Collection<PlayerMatchStatContainer> crownWearerContainers = getRelevantStatContainers(event.getPlayer());

        for (PlayerMatchStatContainer container : crownWearerContainers) {

            if (!(container instanceof PlayerSessionStatContainer))
                continue;

            ((PlayerSessionStatContainer) container).addCrownAchieved();

            if (event.getCrown().equals(PlayerObtainedCrownEvent.CrownType.MATCH_MVP))
                ((PlayerSessionStatContainer) container).addMatchMVPs();

            container.save(event.getPlayer());
        }

    }



}
