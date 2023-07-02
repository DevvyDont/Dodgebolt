package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.*;
import me.devvy.dodgebolt.hologram.HolographicDynamicScoreboard;
import me.devvy.dodgebolt.map.DodgeboltArrowSpawners;
import me.devvy.dodgebolt.map.DodgeboltStadium;
import me.devvy.dodgebolt.signs.*;
import me.devvy.dodgebolt.statistics.GameStatisticsManager;
import me.devvy.dodgebolt.statistics.MinecraftScoreboardManager;
import me.devvy.dodgebolt.statistics.RoundStatistics;
import me.devvy.dodgebolt.tasks.DodgeboltIngamePhaseTask;
import me.devvy.dodgebolt.tasks.DodgeboltIntermissionPhaseTask;
import me.devvy.dodgebolt.tasks.DodgeboltPhaseTask;
import me.devvy.dodgebolt.tasks.DodgeboltPregamePhaseTask;
import me.devvy.dodgebolt.team.Team;
import me.devvy.dodgebolt.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DodgeboltGame implements Listener {

    private final DodgeboltStadium stadium;
    private final Team team1;
    private final Team team2;
    private DodgeboltPhaseTask currentPhaseTask = null;
    private DodgeboltGameState state;

    private final List<InteractableSign> signs = new ArrayList<>();

    private DodgeboltArrowSpawners arrowSpawners;

    private final MinecraftScoreboardManager scoreboardManager;

    private final GameStatisticsManager gameStatisticsManager;
    private final DodgeboltGameAnnouncements announcer;

    private final HolographicDynamicScoreboard mainHoloScoreboard;
    private boolean useSessionScoreboard;

    private final PlayerInventoryStower inventoryStower;

    private int startingRoundsToWin;
    private int roundsToWin;

    private int bowDamagePercent = 100;

    private boolean onlyAdminsCanEdit;

    public DodgeboltGame() {

        // If a location is not defined we cannot make the arena
        Location arenaLocation = ConfigManager.getArenaLocation();
        if (arenaLocation == null)
            throw new IllegalStateException("Arena location is not set! Not generating arena yet...");

        stadium = new DodgeboltStadium(arenaLocation);
        stadium.generateStadium();

        setOnlyAdminsCanEdit(Dodgebolt.getInstance().getConfig().getBoolean(ConfigManager.OP_CHANGE_SETTINGS, true));
        startingRoundsToWin = Dodgebolt.getPlugin(Dodgebolt.class).getConfig().getInt(ConfigManager.ROUND_WIN_LIMIT);
        roundsToWin = startingRoundsToWin;

        team1 = new Team(ChatColor.BLUE);
        team2 = new Team(ChatColor.LIGHT_PURPLE);

        stadium.changeTeamColors(team1.getTeamColor(), team2.getTeamColor());

        state = DodgeboltGameState.WAITING;
        useSessionScoreboard = false;

        signs.add(new TeamSwitchSign(this, stadium.getSpawn().clone().add(-2-18, 1, 1), BlockFace.EAST, team1));
        signs.add(new TeamSwitchSign(this, stadium.getSpawn().clone().add(-2-18, 1, -1),BlockFace.EAST, team2));
        signs.add(new SpectatorSwitchSign(this, stadium.getSpawn().clone().add( -2-18, 1, 0), BlockFace.EAST));
        signs.add(new StartGameSign(this, stadium.getSpawn().clone().add(-2-18, 2, 0), BlockFace.EAST));

        signs.add(new ScoreLimitSign(this, stadium.getSpawn().clone().add(-2-18, 2, 2), BlockFace.EAST));
        signs.add(new BowDamageSign(this, stadium.getSpawn().clone().add(-2-18, 2, 3), BlockFace.EAST));

        signs.add(new ShuffleTeamsSign(this, stadium.getSpawn().clone().add(-2-18, 2, -2), BlockFace.EAST));
        signs.add(new AdminModeSign(this, stadium.getSpawn().clone().add(-2-18, 2, -4), BlockFace.EAST));
        signs.add(new UseSessionScoreboard(this, stadium.getSpawn().clone().add(-2-18, 1, -4), BlockFace.EAST));

        gameStatisticsManager = new GameStatisticsManager();
        announcer = new DodgeboltGameAnnouncements();
        scoreboardManager = new MinecraftScoreboardManager(this);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().registerEvents(scoreboardManager, Dodgebolt.getPlugin(Dodgebolt.class));
        inventoryStower = new PlayerInventoryStower();

        mainHoloScoreboard = new HolographicDynamicScoreboard(gameStatisticsManager, stadium.getOrigin().clone().add(0, 10, 0));

        for (Player p : getAllPlayersInStadium())
            playerWalkedInStadium(p);
    }

    public GameStatisticsManager getGameStatisticsManager() {
        return gameStatisticsManager;
    }

    public DodgeboltGameAnnouncements getAnnouncer() {
        return announcer;
    }

    public boolean isOnlyAdminsCanEdit() {
        return onlyAdminsCanEdit;
    }

    public void setOnlyAdminsCanEdit(boolean onlyAdminsCanEdit) {
        this.onlyAdminsCanEdit = onlyAdminsCanEdit;
    }

    public boolean useSessionScoreboard() {
        return useSessionScoreboard;
    }

    public void setUseSessionScoreboard(boolean useSessionScoreboard) {
        this.useSessionScoreboard = useSessionScoreboard;
    }

    public void shuffleTeams(boolean includeSpectators) {
        List<Player> activePlayerPool = new ArrayList<>();

        for (Player player : getAllPlayersInStadium()) {

            boolean hasTeam = getPlayerTeam(player) != null;

            // If this person is in creative or spectator mode and not on a team, ignore them no matter what
            if (player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR) && !hasTeam)
                continue;

            // If we ignore spectators and this person doesn't have a team, ignore this person
            if (!includeSpectators && !hasTeam)
                continue;

            activePlayerPool.add(player);
        }

        team1.clearPlayers();
        team2.clearPlayers();

        // Edge case, nobody here?
        if (activePlayerPool.isEmpty())
            return;

        Collections.shuffle(activePlayerPool);

        // Iterate til half and set to team 1, other half team 2
        int partition = activePlayerPool.size() / 2;

        for (int i = 0; i < activePlayerPool.size(); i++) {
            Team t = i < partition ? team2 : team1;
            t.addPlayer(activePlayerPool.get(i));
        }
    }

    public int getCurrentRoundNumber() {
        return team1.getScore() + team2.getScore() + 1;
    }

    public int getRoundsToWin() {
        return roundsToWin;
    }

    public int getStartingRoundsToWin() {
        return startingRoundsToWin;
    }

    public void setStartingRoundsToWin(int startingRoundsToWin) {
        this.startingRoundsToWin = startingRoundsToWin;
    }

    public void setRoundsToWin(int roundsToWin) {
        this.roundsToWin = roundsToWin;
    }

    public int getBowDamagePercent() {
        return bowDamagePercent;
    }

    public void setBowDamagePercent(int bowDamagePercent) {
        this.bowDamagePercent = bowDamagePercent;
    }

    public DodgeboltStadium getStadium() {
        return stadium;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public Team getOpposingTeam(Team team) {

        if (team == team1)
            return team2;
        else if (team == team2)
            return team1;
        return null;

    }

    public Team getPlayerTeam(Player player) {
        if (team1.isMember(player))
            return team1;
        else if (team2.isMember(player))
            return team2;
        return null;
    }

    public void setPlayerTeam(Player player, Team team) {

        if (getPlayerTeam(player) != null)
            getPlayerTeam(player).removePlayer(player);

        team.addPlayer(player);
    }

    public void setSpectating(Player player) {
        getTeam2().removePlayer(player);
        getTeam1().removePlayer(player);
        scoreboardManager.setSpectatingCosmetics(player);
    }

    public void sendTitleToSpectators(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : getAllPlayersInStadium())
            if (getPlayerTeam(player) == null)
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void playSoundToSpectators(Sound sound, float volume, float pitch) {
        for (Player player : getAllPlayersInStadium())
            if (getPlayerTeam(player) == null)
                player.playSound(player, sound, volume, pitch);
    }

    public DodgeboltGameState getState() {
        return state;
    }

    public void setState(DodgeboltGameState state) {
        this.state = state;
    }

    public boolean isInProgress() {
        return DodgeboltGameState.isGameRunning(state);
    }

    public String getBothTeamAliveCountString() {
        return getTeam1().getTeamColor() + "" + ChatColor.BOLD + getTeam1().getElimTracker().getTeamMembersAlive() + ChatColor.GRAY + " v " + getTeam2().getTeamColor() + "" + ChatColor.BOLD + getTeam2().getElimTracker().getTeamMembersAlive();
    }

    /**
     * Called when we want to start a brand new fresh game
     */
    public void startNewGame() {

        if (getState() != DodgeboltGameState.WAITING)
            return;

        setStartingRoundsToWin(roundsToWin);

        // Reset the score
        getTeam1().setScore(0);
        getTeam2().setScore(0);

        gameStatisticsManager.clearMatchAndRoundStats();
        mainHoloScoreboard.delete();

        for (Player p : getAllPlayersInStadium())
            scoreboardManager.showMinecraftScoreboardAttributes(p);

        mainHoloScoreboard.update(useSessionScoreboard());

        startNewRound();
    }

    public void givePlayerKit(Player player) {

        Team team = getPlayerTeam(player);
        if (team == null)
            return;

        player.getInventory().setBoots(Items.getBoots(team));
        player.getInventory().addItem(Items.getDodgeboltBow());
    }

    /**
     * Called when we should start a fresh round, not an entire game
     */
    public void startNewRound() {

        setState(DodgeboltGameState.PREGAME_COUNTDOWN);

        boolean playersAreHere = playersAreHereCheck();
        if (!playersAreHere)
            return;

        // Fix the arena
        stadium.getArena().restoreArena();

        // Enable barriers
        stadium.getArena().setSpawnBarrier(Material.BARRIER);

        // Teleport players to their spawns and give them necessary gear
        toStartingPositions();

        // Get rid of items laying on the ground that are dodgebolt items
        Items.removeDodgeboltEntities(Item.class);
        Items.removeDodgeboltEntities(Arrow.class);

        // Create a task to start the game
        currentPhaseTask = new DodgeboltPregamePhaseTask(this);
        currentPhaseTask.runTaskTimer(Dodgebolt.getPlugin(Dodgebolt.class), 0, DodgeboltPregamePhaseTask.PERIOD);
    }

    private boolean playersAreHereCheck() {
        if (getAllPlayersOnTeams().isEmpty()) {
            endGame(false);
            return false;
        }

        if (getTeam1().getMembersAsPlayers().isEmpty()) {
            getTeam2().setScore(roundsToWin);
            endGame(false);
            return false;
        }

        if (getTeam2().getMembersAsPlayers().isEmpty()) {
            getTeam1().setScore(roundsToWin);
            endGame(false);
            return false;
        }

        return true;
    }

    /**
     * Tps all active players to their spawns right before a round starts
     */
    public void toStartingPositions() {

        // Tp the players to their spots
        for (Team team : new Team[]{team1, team2}) {

            int spawnIndex = 0;
            for (Player player : team.getMembersAsPlayers()) {
                Location spawn = stadium.getArena().getSpawnLocation(spawnIndex, team == team2);
                spawnIndex++;

                if (player.isDead())
                    player.spigot().respawn();

                player.teleport(spawn);
                player.setGlowing(true);
                player.setHealth(20);
                Items.clearDodgeboltItems(player);
                givePlayerKit(player);
                player.setKiller(null);
            }
        }

    }

    public void exitPregamePhase() {
        currentPhaseTask.cancel();

        boolean playersAreHere = playersAreHereCheck();
        if (!playersAreHere)
            return;

        // Get rid of items laying on the ground that are dodgebolt items
        Items.removeDodgeboltEntities(Item.class);
        Items.removeDodgeboltEntities(Arrow.class);

        currentPhaseTask = new DodgeboltIngamePhaseTask(this);
        currentPhaseTask.runTaskTimer(Dodgebolt.getPlugin(Dodgebolt.class), 0, DodgeboltIngamePhaseTask.PERIOD);

        team1.getElimTracker().reset(team1.getMembersAsPlayers().size());
        team2.getElimTracker().reset(team2.getMembersAsPlayers().size());

        setState(DodgeboltGameState.INGAME);

        // Take the barriers down
        stadium.getArena().setSpawnBarrier(Material.AIR);

        for (Player player : getAllPlayersInStadium()) {

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1, 1.5f);
            player.sendTitle("", ChatColor.RED + "Eliminate" + ChatColor.GRAY + " the other team!", 1, 30, 5);

            // If it is a 1v1 play pigstep
            if (team1.getElimTracker().getTeamMembersAlive() == 1 && team2.getElimTracker().getTeamMembersAlive() == 1)
                playPigstep();

            player.setGlowing(false);

            player.setFireTicks(0);
        }

        arrowSpawners = new DodgeboltArrowSpawners(this, Arrays.asList(stadium.getArena().getArrowSpawnLocations()));

    }

    public void exitGameRoundPhase(Team winner) {
        stadium.getArena().stopOvertimeShatter();
        currentPhaseTask.cancel();

        Team loser = getOpposingTeam(winner);
        new RoundEndedEvent(winner, loser).callEvent();

        // First we announce the past round results
        // Check for special win condition, ace > team ace > flawless > clutch
        RoundStatistics roundStatistics = gameStatisticsManager.getRoundStatistics(getCurrentRoundNumber());

        Player acedPlayer = roundStatistics.getWhoAced(winner);

        // Decide what event occured to we can inform the players
        if (acedPlayer != null)
            new DodgeboltPlayerAcedEvent(acedPlayer).callEvent();
        else if (roundStatistics.teamAced(winner))
            new DodgeboltTeamAcedEvent(winner).callEvent();
        else if (roundStatistics.teamFlawlessed(winner))
            new DodgeboltTeamFlawlessedEvent(winner).callEvent();
        else if (roundStatistics.teamClutched(winner))
            new DodgeboltTeamClutchedEvent(winner).callEvent();
        else
            new DodgeboltTeamWonBasicRoundEvent(winner).callEvent();

        // Now that we announced the results completely, we can update score and check for OT or end the game
        setState(DodgeboltGameState.INTERMISSION);

        // Give the winners a point
        winner.setScore(winner.getScore() + 1);
        mainHoloScoreboard.update(useSessionScoreboard());

        // Win by 2 rule? if both teams are on match point
        if (Dodgebolt.getInstance().getConfig().getBoolean(ConfigManager.WIN_BY_2)) {
            // If the teams scores are equal and the next point would be a game winner....
            if (team1.getScore() == team2.getScore() && getRoundsToWin() - 1 == team1.getScore()) {
                broadcast(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + ChatColor.BOLD + "Overtime! " + ChatColor.YELLOW + "Another two rounds will be played!");
                setRoundsToWin(team1.getScore() + 2);
            }
        }

        // Set players to psuedogod mode and clear their inventories
        for (Player player : getAllPlayersInStadium()) {
            player.setGlowing(false);
            player.stopSound(Sound.MUSIC_DISC_PIGSTEP);
        }

        arrowSpawners.delete();
        arrowSpawners = null;

        // If one of the teams hit the score limit end the game
        if (team1.getScore() >= roundsToWin || team2.getScore() >= roundsToWin) {
            endGame(false);
            return;
        }

        // Otherwise start up a new round later
        currentPhaseTask = new DodgeboltIntermissionPhaseTask(this);
        currentPhaseTask.runTaskTimer(Dodgebolt.getPlugin(Dodgebolt.class), 1, DodgeboltIntermissionPhaseTask.PERIOD);

    }

    public void exitIntermissionPhase() {
        currentPhaseTask.cancel();
        currentPhaseTask = null;
        startNewRound();
    }

    public Collection<Player> getAllPlayersInStadium() {
        List<Player> players = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers())
                if (getStadium().isInStadium(p.getLocation()))
                    players.add(p);

        return players;
    }

    public Collection<Player> getAllPlayersOnTeams() {
        List<Player> players = new ArrayList<>();
        players.addAll(getTeam1().getMembersAsPlayers());
        players.addAll(getTeam2().getMembersAsPlayers());
        return players;
    }

    public void broadcast(String message) {
        for (Player p : getAllPlayersInStadium())
            p.sendMessage(message);
    }

    public void broadcast(Component message) {
        for (Player p : getAllPlayersInStadium())
            p.sendMessage(message);
    }

    public void globalBroadcast(String message) {
        for (Player p : Bukkit.getOnlinePlayers())
            p.sendMessage(message);
    }

    public void globalBroadcast(Component message) {
        for (Player p : Bukkit.getOnlinePlayers())
            p.sendMessage(message);
    }

    public void endGame(boolean byForce) {

        for (Player p : getAllPlayersOnTeams())
            Items.clearDodgeboltItems(p);

        if (byForce)
            teleportPlayersToOriginAndReset();
        else
            teleportPlayersToOriginAndResetLater();

        // Tell everyone the results
        announceMatchResults();

    }

    public void teleportPlayersToOriginAndReset() {
        for (Player player : getAllPlayersInStadium()) {

            if (player.isDead())
                player.spigot().respawn();

            player.setGlowing(false);
            player.setFireTicks(0);

            Location loc = stadium.getSpawn().add(Math.random()*5 + 5.5, 0, Math.random()*5 - 2.5);
            player.teleport(loc);
        }

        reset();
    }

    public void teleportPlayersToOriginAndResetLater() {
        // Make a task that will respawn everybody atspawn and restore the game
        new BukkitRunnable() {
            @Override
            public void run() {
                teleportPlayersToOriginAndReset();
            }
        }.runTaskLater(Dodgebolt.getPlugin(Dodgebolt.class), 120);
    }

    /**
     * Called when a match is over. Do things such as announce the winner, give people their crowns, update
     * the scoreboard etc
     */
    public void announceMatchResults() {

        // Handle a draw
        if (team1.getScore() == team2.getScore()) {

            // Announce the entire server that nobody won and do nothing
            for (Player player : getAllPlayersInStadium()) {
                player.sendTitle(ChatColor.GRAY + "DRAW!", ChatColor.WHITE + "Nobody wins today :/", 10, 80, 40);
                player.playSound(player.getEyeLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, .5f);
            }

            globalBroadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "A Dodgebolt game just ended in a draw!");

            return;
        }

        // Figure out the winner
        Team winner = team1.getScore() > team2.getScore() ? team1 : team2;
        globalBroadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + "!" + ChatColor.GRAY + "] " + ChatColor.GRAY + "A Dodgebolt game was just won by " + winner.getCleanMembersString() + ChatColor.GRAY + "!");

        // Loop through the winning players and tell them they won and give them crowns
        for (Player winningPlayer : winner.getMembersAsPlayers()) {
            winningPlayer.playSound(winningPlayer.getEyeLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, .75f, 1);
            winningPlayer.sendTitle(ChatColor.GREEN + "VICTORY", winner.getTeamColor() + winner.getName() + ChatColor.GRAY + " won the game!", 10, 80, 40);
            Items.equipWinnerCrown(winningPlayer, winner, Material.GOLDEN_HELMET, ChatColor.YELLOW + "Winner's Crown", String.format("%swinning %sa match %s%s %s- %s%s%s!", ChatColor.GREEN, ChatColor.GRAY, winner.getTeamColor(), winner.getScore(), ChatColor.GRAY, getOpposingTeam(winner).getTeamColor(), getOpposingTeam(winner).getScore(), ChatColor.GRAY));
            new PlayerObtainedCrownEvent(winningPlayer, PlayerObtainedCrownEvent.CrownType.MATCH_WIN).callEvent();
        }

        // Give the MVP an mvp crown
        Player matchMVP = Bukkit.getPlayer(gameStatisticsManager.getMatchMVP().getOwner());

        if (matchMVP != null) {
            Team mvpTeam = getPlayerTeam(matchMVP);
            Items.equipWinnerCrown(matchMVP, mvpTeam, Material.DIAMOND_HELMET, ChatColor.AQUA + "Match MVP Crown", "achieving " + ChatColor.GOLD + "Match MVP" + ChatColor.GRAY + " in a Dodgebolt game!");
            new PlayerObtainedCrownEvent(matchMVP, PlayerObtainedCrownEvent.CrownType.MATCH_MVP).callEvent();
        }

        // Loop through the losers and tell them they lost
        for (Player losingPlayer : getOpposingTeam(winner).getMembersAsPlayers()) {
            losingPlayer.playSound(losingPlayer.getEyeLocation(), Sound.ENTITY_WITHER_DEATH, .8f, 1);
            losingPlayer.sendTitle(ChatColor.DARK_RED + "DEFEAT", winner.getTeamColor() + winner.getName() + ChatColor.GRAY + " won the game!", 10, 80, 40);
        }

        // Loop through everybody else on the server and tell them who won
        for (Player spectator : getAllPlayersInStadium()) {
            if (getPlayerTeam(spectator) != null)
                continue;

            spectator.playSound(spectator.getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, .5f, 1);
            spectator.sendTitle(ChatColor.AQUA + "GAME OVER", winner.getTeamColor() + winner.getName() + ChatColor.GRAY + " won the game!", 10, 80, 40);
        }

        new GameEndedEvent(this).callEvent();

    }

    /**
     * Sets score to 0, clean stuff up etc
     */
    public void reset() {
        team1.setScore(0);
        team2.setScore(0);
        setRoundsToWin(startingRoundsToWin);
        stadium.getArena().restoreArena();
        setState(DodgeboltGameState.WAITING);
    }

    public void cleanup() {
        stadium.getArena().destroyArena();

        // Destroy signs
        for (InteractableSign sign : signs)
            sign.destroy();

        // Go back to original scoreboard
        for (Player p : getAllPlayersInStadium())
            scoreboardManager.hideMinecraftScoreboardAttributes(p);

        // Delete the hologram scoreboard
        mainHoloScoreboard.delete();

        inventoryStower.restoreAll();

        if (currentPhaseTask != null)
            currentPhaseTask.cancel();

        currentPhaseTask = null;
    }

    private void playerWalkedInStadium(Player player) {

        inventoryStower.storeInventory(player);
        player.setGameMode(GameMode.SURVIVAL);

        scoreboardManager.showMinecraftScoreboardAttributes(player);

        // Default them to spectator
        setSpectating(player);

        player.showTitle(Title.title(Component.text("Dodgebolt Arena", NamedTextColor.AQUA), Component.text("Interact with the signs to play!", NamedTextColor.GRAY)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 1f, 1f);

        Dodgebolt.getInstance().getServer().getPluginManager().callEvent(new PlayerEnterStadiumEvent(this, player));
    }

    private void playerWalkedOutStadium(Player player) {

        Items.clearDodgeboltItems(player);
        inventoryStower.restoreInventory(player);

        scoreboardManager.hideMinecraftScoreboardAttributes(player);

        player.showTitle(Title.title(Component.text("Leaving Dodgebolt", NamedTextColor.RED), Component.text("Come back soon!", NamedTextColor.GRAY)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f);
        player.setGlowing(false);

        // Attempt to register a death if they were on a team and in game
        handleIngameDeath(player, true);

        // Attempt to make them leave a team if they were on one
        team1.removePlayer(player);
        team2.removePlayer(player);

        // Stop playing the 1v1 music
        player.stopSound(Sound.MUSIC_DISC_PIGSTEP);

        Dodgebolt.getInstance().getServer().getPluginManager().callEvent(new PlayerLeaveStadiumEvent(this, player));
    }

    public void enterExitStadiumCheck(Player player, Location from, Location to) {
        // If block did not change then don't care
        if (from.getBlock().equals(to.getBlock()))
            return;

        // If from block is out of the stadium but to block is in, then enter
        if (!getStadium().isInStadium(from.getBlock().getLocation()) && getStadium().isInStadium(to.getBlock().getLocation()))
            playerWalkedInStadium(player);

        // If from block is in stadium but to block is out, then exit
        if (getStadium().isInStadium(from.getBlock().getLocation()) && !getStadium().isInStadium(to.getBlock().getLocation()))
            playerWalkedOutStadium(player);
    }

    @EventHandler
    public void onPlayerEnterExitStadium(PlayerMoveEvent event) {
        enterExitStadiumCheck(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onPlayerTeleportEnterExitStadium(PlayerTeleportEvent event) {
        enterExitStadiumCheck(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        // If they did not join within the stadium, this player should not affect us ignore them
        if (!getStadium().isInStadium(event.getPlayer().getLocation()))
            return;

        // They are in the stadium so we need to do the same thing we would have done if a player walked in
        playerWalkedInStadium(event.getPlayer());

        // Also teleport them to a better spot
        event.getPlayer().teleport(stadium.getSpawn());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {

        // Player in the stadium or playing?
        if (!getStadium().isInStadium(event.getPlayer().getLocation()))
            return;

        // Simulate them walking out
        playerWalkedOutStadium(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        // Why is this a thing
        if (event.getRespawnFlags().contains(PlayerRespawnEvent.RespawnFlag.END_PORTAL))
            return;

        // Since a player died we can guarantee that they have a death location
        assert event.getPlayer().getLastDeathLocation() != null;

        // If death location occurred in the stadium keep them in there
        if (!stadium.isInStadium(event.getPlayer().getLastDeathLocation()))
            return;

        event.setRespawnLocation(stadium.getSpawn());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        // If someone died during waiting time we do not care
        if (!DodgeboltGameState.isGameRunning(state))
            return;

        // If the player wasn't a player we don't care
        if (!getAllPlayersOnTeams().contains(event.getPlayer()))
            return;

        // A player died that was playing
        handleIngameDeath(event.getEntity(), false);
        // handleIngameDeath will send a message for us, so we can empty this
        event.deathMessage(Component.empty());
    }

    public void handleIngameDeath(Player player, boolean wasQuit) {

        // Not on a team or game not running? Ignore
        Team deadPlayersTeam = getPlayerTeam(player);
        if (deadPlayersTeam == null || state != DodgeboltGameState.INGAME)
            return;

        // Somehow died a second time? Ignore
        boolean newDeath = deadPlayersTeam.getElimTracker().teamMemberDied(player);
        if (!newDeath)
            return;

        // Make an event to announce that this player died
        new DodgeboltPlayerEliminatedEvent(player).callEvent();
        // Update the scoreboard
        mainHoloScoreboard.update(useSessionScoreboard());

        // Announce to the stadium that this person died
        for (Player otherPlayers : getAllPlayersInStadium()) {

            if (team1.getElimTracker().getTeamMembersAlive() == 1 && team2.getElimTracker().getTeamMembersAlive() == 1)
                playPigstep();

            if (otherPlayers == player.getKiller())
                otherPlayers.sendTitle(getBothTeamAliveCountString(),ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + deadPlayersTeam.getCleanMemberString(player), 5, 15, 5);
            else
                otherPlayers.sendTitle(getBothTeamAliveCountString(), "", 5, 15, 5);

            Team otherPlayersTeam = getPlayerTeam(otherPlayers);
            if (otherPlayersTeam == null || getOpposingTeam(otherPlayersTeam) == deadPlayersTeam)
                otherPlayers.playSound(otherPlayers.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            else
                otherPlayers.playSound(otherPlayers.getLocation(), Sound.ENTITY_ENDERMAN_HURT, 1, .8f);

        }

        // Send a kill message in chat since we turned it off

        // If they quit, had no killer, or were their own killer it was a suicide
        // If the killer was on the same team, it was a teamkill
        // If the killer was any other scenario, normal kill message
        if (wasQuit || player.getKiller() == null || player == player.getKiller())
            globalBroadcast(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + Phrases.getRandomSuicidePhrase(player));
        else if (player.getKiller() != null && player.getKiller() != player && getPlayerTeam(player) == getPlayerTeam(player.getKiller()))
            globalBroadcast(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + Phrases.getRandomTeamKillPhrase(player, player.getKiller()));
        else
            globalBroadcast(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + (player.getKiller() != null ? Phrases.getRandomKilledPhrase(player, player.getKiller()) : Phrases.getRandomSuicidePhrase(player)));

        // If a team is completely dead, then end the round, otherwise shrink the arena
        if (deadPlayersTeam.getElimTracker().teamIsDead())
            exitGameRoundPhase(getOpposingTeam(deadPlayersTeam));
        else
            stadium.getArena().shrinkArena();

        // Pretty kill fx
        Fireworks.spawnFireworksInstantly(player.getLocation(), ColorTranslator.translateChatColorToColor(deadPlayersTeam.getTeamColor()));
    }

    private void playPigstep() {

        if (!Dodgebolt.getInstance().getConfig().getBoolean(ConfigManager.ONEVSONE_PIGSTEP))
            return;

        for (Player p : getAllPlayersInStadium())
            p.playSound(p.getEyeLocation().add(0, 100, 0), Sound.MUSIC_DISC_PIGSTEP, 1.5f, 1);
    }

    /**
     * Handle prevention of crossing sides and taking/giving bow based on arena position
     *
     * @param event
     */
    @EventHandler
    public void onPlayerMoveDuringGame(PlayerMoveEvent event) {

        if (state != DodgeboltGameState.INGAME)
            return;

        if (getPlayerTeam(event.getPlayer()) == null)
            return;

        if (event.getTo().getBlockZ() == stadium.getOrigin().getBlockZ()) {
            if (event.getTo().getBlockY() < stadium.getOrigin().getBlockY() + 4) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Stay on your side!");
            }
        }

        // If the player is over the foul line take their bow away
        boolean enteredFoulZone = event.getFrom().getBlock() != event.getTo().getBlock() && getStadium().getArena().outOfBowRange(event.getTo()) && !getStadium().getArena().outOfBowRange(event.getFrom());
        boolean leftFoulZone = event.getFrom().getBlock() != event.getTo().getBlock() && getStadium().getArena().outOfBowRange(event.getFrom()) && !getStadium().getArena().outOfBowRange(event.getTo());
        ItemStack bow = Items.getDodgeboltBow();
        ItemStack clay = Items.getInactiveBow();

        if (enteredFoulZone) {
            for (ItemStack is : event.getPlayer().getInventory().getContents())
                if (is != null && is.hasItemMeta() && is.getType().equals(bow.getType())) {
                    is.setType(clay.getType());
                    is.setItemMeta(clay.getItemMeta());
                }
        }
        // If the player went back into play give them the bow back
        else if (leftFoulZone) {
            for (ItemStack is : event.getPlayer().getInventory().getContents())
                if (is != null && is.hasItemMeta() && is.getType().equals(clay.getType())) {
                    is.setType(bow.getType());
                    is.setItemMeta(bow.getItemMeta());
                }
        }
    }

    /**
     * Handle prevention of natural health regeneration in the stadium
     *
     * @param event
     */
    @EventHandler
    public void onRegenDuringGameAttempt(EntityRegainHealthEvent event) {

        // If entity was not in arena don't care
        if (!getStadium().getArena().isInArena(event.getEntity().getLocation()))
            return;

        // If the reason wasn't either peaceful regen or natural regen due to food we don't care
        EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
        if (!(reason.equals(EntityRegainHealthEvent.RegainReason.REGEN) || reason.equals(EntityRegainHealthEvent.RegainReason.SATIATED)))
            return;

        event.setCancelled(true);
    }

    /**
     * Handle prevention of picking up items that are not dodgebolt items in the stadium
     *
     * @param event
     */
    @EventHandler
    public void onPlayerAttemptPickupInStadium(PlayerAttemptPickupItemEvent event) {

        // If player was not in stadium don't care
        if (!getStadium().isInStadium(event.getPlayer().getLocation()))
            return;

        if (event.getPlayer().isOp())
            return;

        // At this point a player is attempting to pickup an item in the stadium, they cannot pickup items unless its from us
        if (!Items.itemIsFromDodgebolt(event.getItem().getItemStack().getItemMeta()))
            event.setCancelled(true);
    }

    /**
     * Handle when a player attempts to open a chest in the stadium (don't let them)
     *
     * @param event
     */
    @EventHandler
    public void onPlayerAttemptOpenChestInStadium(PlayerInteractEvent event) {
        // If player was not in stadium don't care
        if (!getStadium().isInStadium(event.getPlayer().getLocation()))
            return;

        // If this was a right click event on a chest, enderchest, shulker, hopper, cancel it
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        if (event.getClickedBlock() == null)
            return;

        if (event.getPlayer().isOp())
            return;

        if (Arrays.asList(Material.CHEST, Material.HOPPER, Material.SHULKER_BOX, Material.ENDER_CHEST).contains(event.getClickedBlock().getType())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.text("You cannot open storage here!", NamedTextColor.RED));
        }

    }

    /**
     * Handle when a player takes lava damage in the stadium
     *
     * @param event
     */
    @EventHandler
    public void onPlayerTookLavaDamage(EntityDamageEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        if (state == DodgeboltGameState.INTERMISSION || state == DodgeboltGameState.PREGAME_COUNTDOWN || event.getCause() == EntityDamageEvent.DamageCause.FALL)
            event.setCancelled(true);

        else if (state == DodgeboltGameState.INGAME && event.getCause() == EntityDamageEvent.DamageCause.LAVA)
            event.setDamage(10000);
    }

    /**
     * Handle every instance of PVP that we should care about
     * - Smacking each other no matter if we are in game or not
     * - Shooting each other with bows
     *
     * @param event
     */
    @EventHandler
    public void onPlayerPVP(EntityDamageByEntityEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getDamager().getLocation()))
            return;

        // Don't do any damage no matter what the cause or source is, we will override it
        event.setDamage(0);

        // A mouthful but: If both entities are players, the dealer is op, and we are in lobby phase...
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && event.getDamager().isOp() && state == DodgeboltGameState.WAITING) {

            // Sneaking to kick from team?
            if (((Player) event.getDamager()).isSneaking()) {
                setSpectating((Player) event.getEntity());
            } else {
                // Get op's team
                Team opTeam = getPlayerTeam((Player) event.getDamager());

                // If it exists infect the other player to their team
                if (opTeam != null && getPlayerTeam((Player) event.getEntity()) != opTeam)
                    setPlayerTeam((Player) event.getEntity(), opTeam);
            }

            return;


        }

        // If the thing being damaged is not a person ignore them
        if (!(event.getEntity() instanceof Player))
            return;

        // If the thing doing damage is not an arrow ignore
        if (!(event.getDamager() instanceof Arrow))
            return;

        // If the damage being dealt was not a projectile ignore
        if (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE)
            return;

        // If the firer of the projectile did not come from a player cancel the event and ignore
        if (!(((Arrow) event.getDamager()).getShooter() instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        // At this point we should be contained in the stadium, and a player is getting shot by an arrow

        //todo: FIGURE OUT WHY TF THIS GET CALLED SO MANY TIMES AND GIVES PLAYERS GIGA ACCURACY
        Player shooter = (Player) ((Arrow) event.getDamager()).getShooter();

        double damage = bowDamagePercent / 5.0;
        new PlayerHitDodgeboltArrowEvent(shooter, (Player) event.getEntity(), damage).callEvent();

        // At this point an arrow is being shot we can override the damage
        event.setDamage(damage);
    }

    /**
     * Handle when a bow is shot during the game, set arrow color and announce this to the plugin
     *
     * @param event
     */
    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        // If a player did not shoot this bow ignore
        if (!(event.getEntity() instanceof Player))
            return;

        Player entity = (Player) event.getEntity();

        // If this player was not on a team ignore
        Team playersTeam = getPlayerTeam(entity);
        if (playersTeam == null)
            return;

        // If this player was out of bow range don't let them shoot
        if (getStadium().getArena().outOfBowRange(entity.getLocation())) {
            event.setCancelled(true);
            return;
        }

        // If we shot an arrow, set its color
        if (event.getProjectile() instanceof Arrow) {
            ((Arrow) event.getProjectile()).setColor(ColorTranslator.translateChatColorToColor(playersTeam.getTeamColor()));
            event.getProjectile().setGlowing(true);
        }

        // Announce that we shot an arrow
        new PlayerFiredDodgeboltArrowEvent((Player) event.getEntity()).callEvent();
        mainHoloScoreboard.update(useSessionScoreboard());
    }

    /**
     * Handle the conversion process of when an arrow entity hits something, we need to spawn an arrow item stack
     *
     * @param event
     */
    @EventHandler
    public void onArrowCollide(ProjectileHitEvent event) {

        // If this projectile is not in the stadium then ignore them
        if (!stadium.getArena().isInArena(event.getEntity().getLocation()))
            return;

        // Only care about arrows
        if (!(event.getEntity() instanceof Arrow))
            return;

        // Monkey fix for the ceiling
        int yOffset = event.getHitBlockFace() == BlockFace.DOWN ? -1 : 0;

        Item item = event.getEntity().getWorld().dropItem(event.getEntity().getLocation().add(0, yOffset, 0), new ItemStack(Material.ARROW));
        item.setGlowing(true);
        item.setVelocity(event.getEntity().getVelocity().normalize().multiply(-.1));
        Items.tagItemAsDodgeboltItem(item.getItemStack());
        event.getEntity().remove();
    }

    /**
     * Handle changing the color of the entire stadium and arena
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeamColorChange(TeamColorChangeEvent event) {

        ChatColor team1New = team1.getTeamColor();
        ChatColor team2New = team2.getTeamColor();

        if (event.getTeam() == team1)
            team1New = event.getNew();
        else if (event.getTeam() == team2)
            team2New = event.getNew();

        stadium.changeTeamColors(team1New, team2New);
    }

    /**
     * Handle preventing players breaking blocks in the stadium (unless they are op)
     *
     * @param event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        // If either the block or the player is in the stadium, cancel the event
        if (stadium.isInStadium(event.getBlock().getLocation()) || stadium.isInStadium(event.getPlayer().getLocation()))
            event.setCancelled(true);

        // However, if this player is opped then they will always have control
        if (event.getPlayer().isOp())
            event.setCancelled(false);

        if (event.isCancelled())
            event.getPlayer().sendActionBar(Component.text("You cannot break blocks here!", NamedTextColor.RED));
    }

    /**
     * Handle preventing players placing blocks in the stadium (unless they are op)
     *
     * @param event
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        // If either the block or the player is in the stadium, cancel the event
        if (stadium.isInStadium(event.getBlock().getLocation()) || stadium.isInStadium(event.getPlayer().getLocation()))
            event.setCancelled(true);

        // However, if this player is opped then they will always have control
        if (event.getPlayer().isOp())
            event.setCancelled(false);

        if (event.isCancelled())
            event.getPlayer().sendActionBar(Component.text("You cannot place blocks here!", NamedTextColor.RED));
    }

    /**
     * Handle prevention of arrows de spawning during a match
     *
     * @param event
     */
    @EventHandler
    public void onArrowItemDespawnInStadium(ItemDespawnEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        // If an arrow tried despawning stop it
        if (state == DodgeboltGameState.INGAME && event.getEntity().getItemStack().getType() == Material.ARROW)
            event.setCancelled(true);
    }

    /**
     * Handle restricting players dropping items that are not arrows during a game
     *
     * @param event
     */
    @EventHandler
    public void onPlayerAttemptDropItemIngame(PlayerDropItemEvent event) {

        // If this entity is not in the arena then ignore them
        if (!stadium.getArena().isInArena(event.getPlayer().getLocation()))
            return;

        // Don't let anything be dropped BESIDES arrows while the game is in progress
        if (isInProgress() && event.getItemDrop().getItemStack().getType() != Material.ARROW)
            event.setCancelled(true);
    }

    /**
     * Handle making item entities (arrows only) glow when they are spawned within the arena
     *
     * @param event
     */
    @EventHandler
    public void onArrowItemSpawnInArena(ItemSpawnEvent event) {

        // If this entity is not in the arena then ignore them
        if (!stadium.getArena().isInArena(event.getEntity().getLocation()))
            return;

        Items.tagItemAsDodgeboltItem(event.getEntity());

        // If we are in game and an arrow was dropped, make it glow
        if (state == DodgeboltGameState.INGAME && event.getEntity().getItemStack().getType() == Material.ARROW) {
            event.getEntity().setGlowing(true);
            Items.tagItemAsDodgeboltItem(event.getEntity().getItemStack());
        }
    }

}
