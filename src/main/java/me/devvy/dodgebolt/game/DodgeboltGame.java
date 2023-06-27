package me.devvy.dodgebolt.game;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.TeamColorChangeEvent;
import me.devvy.dodgebolt.hologram.HolographicDynamicScoreboard;
import me.devvy.dodgebolt.map.DodgeboltStadium;
import me.devvy.dodgebolt.signs.*;
import me.devvy.dodgebolt.tasks.DodgeboltIngamePhaseTask;
import me.devvy.dodgebolt.tasks.DodgeboltIntermissionPhaseTask;
import me.devvy.dodgebolt.tasks.DodgeboltPhaseTask;
import me.devvy.dodgebolt.tasks.DodgeboltPregamePhaseTask;
import me.devvy.dodgebolt.team.Team;
import me.devvy.dodgebolt.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.projectiles.ProjectileSource;
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

    private final HolographicDynamicScoreboard mainHoloScoreboard;

    private int startingRoundsToWin;
    private int roundsToWin;

    private int bowDamagePercent = 100;

    public DodgeboltGame() {

        // If a location is not defined we cannot make the arena
        Location arenaLocation = ConfigManager.getArenaLocation();
        if (arenaLocation == null)
            throw new IllegalStateException("Arena location is not set! Not generating arena yet...");

        stadium = new DodgeboltStadium(arenaLocation);
        stadium.generateStadium();

        startingRoundsToWin = Dodgebolt.getPlugin(Dodgebolt.class).getConfig().getInt(ConfigManager.ROUND_WIN_LIMIT);
        roundsToWin = startingRoundsToWin;

        team1 = new Team(ChatColor.BLUE);
        team2 = new Team(ChatColor.LIGHT_PURPLE);

        stadium.changeTeamColors(team1.getTeamColor(), team2.getTeamColor());

        state = DodgeboltGameState.WAITING;

        signs.add(new TeamSwitchSign(this, stadium.getSpawn().clone().add(-2-18, 1, 1), BlockFace.EAST, team1));
        signs.add(new TeamSwitchSign(this, stadium.getSpawn().clone().add(-2-18, 1, -1),BlockFace.EAST, team2));
        signs.add(new SpectatorSwitchSign(this, stadium.getSpawn().clone().add( -2-18, 1, 0), BlockFace.EAST));
        signs.add(new StartGameSign(this, stadium.getSpawn().clone().add(-2-18, 2, 0), BlockFace.EAST));

        signs.add(new ShuffleTeamsSign(this, stadium.getSpawn().clone().add(-2-18, 2, -2), BlockFace.EAST));
        signs.add(new ScoreLimitSign(this, stadium.getSpawn().clone().add(-2-18, 2, 2), BlockFace.EAST));
        signs.add(new BowDamageSign(this, stadium.getSpawn().clone().add(-2-18, 2, 3), BlockFace.EAST));

        gameStatisticsManager = new GameStatisticsManager();
        scoreboardManager = new MinecraftScoreboardManager(this);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().registerEvents(scoreboardManager, Dodgebolt.getPlugin(Dodgebolt.class));

        mainHoloScoreboard = new HolographicDynamicScoreboard(gameStatisticsManager, stadium.getOrigin().clone().add(0, 10, 0));
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

        gameStatisticsManager.clear();
        mainHoloScoreboard.hide();
        mainHoloScoreboard.clearPlayers();

        // Track all players for the scoreboard
        for (Player p : getTeam1().getMembersAsPlayers())
            mainHoloScoreboard.trackPlayer(p.getUniqueId());
        for (Player p : getTeam2().getMembersAsPlayers())
            mainHoloScoreboard.trackPlayer(p.getUniqueId());

        mainHoloScoreboard.update();

        startNewRound();
    }

    public void givePlayerKit(Player player) {

        Team team = getPlayerTeam(player);
        if (team == null)
            return;

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootMeta.setColor(ColorTranslator.translateChatColorToColor(team.getTeamColor()));
        bootMeta.setUnbreakable(true);
        bootMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        bootMeta.setDisplayName(team.getTeamColor() + team.getName() + " Boots");
        boots.setItemMeta(bootMeta);

        player.getInventory().setBoots(boots);
        player.getInventory().addItem(Items.getDodgeboltBow());
    }

    /**
     * Called when we should start a fresh round, not an entire game
     */
    public void startNewRound() {

        setState(DodgeboltGameState.PREGAME_COUNTDOWN);

        // Set the time of the world based on how far the game is
//        float gamePercent = (team1.getScore() + team2.getScore() + 1f) / (roundsToWin * 2f - 1f);
//        int newTick = (int) (14000 * gamePercent);
//        stadium.getOrigin().getWorld().setTime(newTick);

        gameStatisticsManager.newRound(getCurrentRoundNumber());

        // Fix the arena
        stadium.getArena().restoreArena();

        // Enable barriers
        stadium.getArena().setSpawnBarrier(Material.BARRIER);

        // Teleport players to their spawns and give them necessary gear
        toStartingPositions();

        // Create a task to start the game
        currentPhaseTask = new DodgeboltPregamePhaseTask(this);
        currentPhaseTask.runTaskTimer(Dodgebolt.getPlugin(Dodgebolt.class), 0, DodgeboltPregamePhaseTask.PERIOD);
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
                givePlayerKit(player);
            }
        }

    }

    public void exitPregamePhase() {
        currentPhaseTask.cancel();
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

        // First we announce the past round results
        // Check for special win condition, ace > team ace > flawless > clutch
        RoundStatistics roundStatistics = gameStatisticsManager.getRoundStatistics(getCurrentRoundNumber());

        Player acedPlayer = roundStatistics.getWhoAced(winner);
        Team loser = getOpposingTeam(winner);

        if (acedPlayer != null) {
            // Announce an ace win
            Bukkit.broadcastMessage(String.format("%s[%s!%s] %s%s %sACED %sand won the round for %s%s%s!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, winner.getTeamColor(), acedPlayer.getName(), ChatColor.GOLD, ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY));
            winner.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, .75f,1);
            playSoundToSpectators(Sound.UI_TOAST_CHALLENGE_COMPLETE, .75f,1);
            loser.playSound(Sound.ENTITY_WITHER_DEATH, .75f, 1);

            winner.sendTitle(String.format("%s%sACE", ChatColor.GOLD, ChatColor.BOLD), String.format("%s%s %skilled the entire enemy team!", winner.getTeamColor(), acedPlayer.getName(), ChatColor.GRAY), 10, 100, 40);
            sendTitleToSpectators(String.format("%s%sACE", ChatColor.GOLD, ChatColor.BOLD), String.format("%s%s %skilled the entire enemy team!", winner.getTeamColor(), acedPlayer.getName(), ChatColor.GRAY), 10, 100, 40);
            loser.sendTitle(String.format("%s%sACE", ChatColor.DARK_PURPLE, ChatColor.BOLD), String.format("%s%s %skilled the entire enemy team!", winner.getTeamColor(), acedPlayer.getName(), ChatColor.GRAY), 10, 100, 40);

            PlayerStats.addStatistic(acedPlayer, PlayerStats.ACES);

        } else if (roundStatistics.teamAced(winner)) {
            // Announce a team ace win
            Bukkit.broadcastMessage(String.format("%s[%s!%s] All members of %s%s %sgot a kill and won the round!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY));
            winner.playSound(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
            playSoundToSpectators(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
            loser.playSound(Sound.ENTITY_ENDERMAN_DEATH, .75f, .75f);

            winner.sendTitle(String.format("%s%sTEAM ACE", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall got one kill!", ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY), 10, 100, 40);
            sendTitleToSpectators(String.format("%s%sTEAM ACE", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall got one kill!", ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY), 10, 100, 40);
            loser.sendTitle(String.format("%s%sTEAM ACE", ChatColor.RED, ChatColor.BOLD), String.format("%sTeam %s%s %sall got one kill!", ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY), 10, 100, 40);
        } else if (roundStatistics.teamFlawlessed(winner)) {
            // Announce a flawless win
            Bukkit.broadcastMessage(String.format("%s[%s!%s] All members of %s%s %slived and won the round!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY));
            winner.playSound(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
            playSoundToSpectators(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
            loser.playSound(Sound.ENTITY_ENDERMAN_DEATH, .75f, .75f);

            winner.sendTitle(String.format("%s%sFLAWLESS", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall stayed alive!", ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY), 10, 100, 40);
            sendTitleToSpectators(String.format("%s%sFLAWLESS", ChatColor.GREEN, ChatColor.BOLD), String.format("%sTeam %s%s %sall stayed alive!", ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY), 10, 100, 40);
            loser.sendTitle(String.format("%s%sFLAWLESS", ChatColor.RED, ChatColor.BOLD), String.format("%sTeam %s%s %sall stayed alive!", ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY), 10, 100, 40);
        } else if (roundStatistics.teamClutched(winner)) {
            // Announce a clutch win
            Bukkit.broadcastMessage(String.format("%s[%s!%s] %s%s %sclutched and won the round for %s%s%s!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, winner.getTeamColor(), winner.getPlayersAlive().iterator().next().getName(), ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY));
            winner.playSound(Sound.ENTITY_VILLAGER_CELEBRATE, .75f,1);
            playSoundToSpectators(Sound.ENTITY_VILLAGER_CELEBRATE, .75f,1);
            loser.playSound(Sound.ENTITY_ENDERMAN_DEATH, .75f, 1.5f);

            winner.sendTitle(String.format("%s%sCLUTCH", ChatColor.GREEN, ChatColor.BOLD), String.format("%s%s %swon the round!", winner.getTeamColor(), winner.getPlayersAlive().iterator().next().getName(), ChatColor.GRAY), 10, 100, 40);
            sendTitleToSpectators(String.format("%s%sCLUTCH", ChatColor.GREEN, ChatColor.BOLD), String.format("%s%s %swon the round!", winner.getTeamColor(), winner.getPlayersAlive().iterator().next().getName(), ChatColor.GRAY), 10, 100, 40);
            loser.sendTitle(String.format("%s%sCLUTCH", ChatColor.RED, ChatColor.BOLD), String.format("%s%s %swon the round!", winner.getTeamColor(), winner.getPlayersAlive().iterator().next().getName(), ChatColor.GRAY), 10, 100, 40);
        } else {
            // Announce a normal round win
            Bukkit.broadcastMessage(String.format("%s[%s!%s] %s%s %swon the round!", ChatColor.GRAY, ChatColor.YELLOW, ChatColor.GRAY, winner.getTeamColor(), winner.getName(), ChatColor.GRAY));
            winner.playSound(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
            playSoundToSpectators(Sound.ENTITY_PLAYER_LEVELUP, .75f,1);
            loser.playSound(Sound.ENTITY_VILLAGER_NO, .75f, .75f);

            winner.sendTitle(String.format("%sROUND WIN", ChatColor.GREEN), "", 10, 100, 40);
            sendTitleToSpectators(String.format("%sROUND WIN", ChatColor.GREEN), "", 10, 100, 40);
            loser.sendTitle(String.format("%sROUND LOSS", ChatColor.RED), "", 10, 100, 40);
        }

        // Other stat tracking stuff we have to do
        for (Player winningMember : winner.getMembersAsPlayers()) {
            Fireworks.spawnVictoryFireworks(winningMember, ColorTranslator.translateChatColorToColor(winner.getTeamColor()));
            PlayerStats.addStatistic(winningMember, PlayerStats.PLAYER_ROUND_WINS);
        }

        // Now that we announced the results completely, we can update score and check for OT or end the game
        setState(DodgeboltGameState.INTERMISSION);

        // Give the winners a point
        winner.setScore(winner.getScore() + 1);
        mainHoloScoreboard.update();

        // Win by 2 rule? if both teams are on match point
        if (Dodgebolt.getPlugin(Dodgebolt.class).getConfig().getBoolean(ConfigManager.WIN_BY_2)) {
            // If the teams scores are equal and the next point would be a game winner....
            if (team1.getScore() == team2.getScore() && getRoundsToWin() - 1 == team1.getScore()) {
                Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.GRAY + "] " + ChatColor.RED + ChatColor.BOLD + "Overtime! " + ChatColor.YELLOW + "Another two rounds will be played!");
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



    public void endGame(boolean byForce) {

        // Tell everyone the results
        announceMatchResults();

        if (byForce)
            teleportPlayersToOriginAndReset();
        else
            teleportPlayersToOriginAndResetLater();

    }

    public void teleportPlayersToOriginAndReset() {
        for (Player player : getAllPlayersInStadium()) {

            if (player.isDead())
                player.spigot().respawn();

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

            return;
        }

        // Figure out the winner
        Team winner = team1.getScore() > team2.getScore() ? team1 : team2;

        // Loop through the winning players and tell them they won and give them crowns
        for (Player winningPlayer : winner.getMembersAsPlayers()) {
            winningPlayer.playSound(winningPlayer.getEyeLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, .75f, 1);
            winningPlayer.sendTitle(ChatColor.GREEN + "VICTORY", winner.getTeamColor() + winner.getName() + ChatColor.GRAY + " won the game!", 10, 80, 40);
            PlayerStats.addStatistic(winningPlayer, PlayerStats.PLAYER_WINS);
            Items.equipWinnerCrown(winningPlayer, Material.GOLDEN_HELMET, ChatColor.YELLOW + "Winner's Crown", String.format("%swinning %sa match %s%s %s- %s%s%s!", ChatColor.GREEN, ChatColor.GRAY, winner.getTeamColor(), winner.getScore(), ChatColor.GRAY, getOpposingTeam(winner).getTeamColor(), getOpposingTeam(winner).getScore(), ChatColor.GRAY));
        }

        // Give the MVP an mvp crown
        Player matchMVP = mainHoloScoreboard.getMVP().getPlayer();
        PlayerStats.addStatistic(matchMVP, PlayerStats.MATCH_MVPS);
        Items.equipWinnerCrown(matchMVP, Material.DIAMOND_HELMET, ChatColor.AQUA + "Match MVP Crown", "achieving " + ChatColor.GOLD + "Match MVP" + ChatColor.GRAY + "!");

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
        mainHoloScoreboard.hide();

        if (currentPhaseTask != null)
            currentPhaseTask.cancel();

        currentPhaseTask = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        if (!getStadium().isInStadium(event.getPlayer().getLocation()))
            return;

        setSpectating(event.getPlayer());
        event.getPlayer().teleport(stadium.getSpawn());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {

        // Player in the stadium or playing?
        if (!getStadium().isInStadium(event.getPlayer().getLocation()))
            return;

        if (!(team1.isMember(event.getPlayer()) || team2.isMember(event.getPlayer())))
            return;

        if (state == DodgeboltGameState.INGAME) {
            event.getPlayer().setHealth(0);
            handleIngameDeath(event.getPlayer(), true);
        }

        team1.removePlayer(event.getPlayer());
        team2.removePlayer(event.getPlayer());

        event.getPlayer().teleport(stadium.getSpawn());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

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

        event.setDeathMessage("");
        Team playerTeam = getPlayerTeam(event.getEntity());
        if (playerTeam == null || state != DodgeboltGameState.INGAME)
            return;

        handleIngameDeath(event.getEntity(), false);
    }

    public boolean outOfBowRange(Location location) {

        if (Math.abs(location.getBlockY() - stadium.getOrigin().getBlockY()) > 3)
            return false;

        int offset = Math.abs(stadium.getOrigin().getBlockX() - location.getBlockX()) > 5 ? 0 : 1;
        return Math.abs(stadium.getOrigin().getBlockZ() - location.getBlockZ()) < 3 + offset;
    }

    public void handleIngameDeath(Player player, boolean wasQuit) {
        Team team = getPlayerTeam(player);
        if (team == null || state != DodgeboltGameState.INGAME)
            return;

        boolean newDeath = team.getElimTracker().teamMemberDied(player);
        if (!newDeath)
            return;

        Player killer = player.getKiller();
        if (killer != null && getPlayerTeam(killer) != team)
            PlayerStats.addStatistic(killer, PlayerStats.PLAYER_KILLS);
        PlayerStats.addStatistic(player, PlayerStats.PLAYER_DEATHS);

        // Keep track for game stats, if killer is null its a suicide so player killed player otherwise killer is killer
        gameStatisticsManager.registerKill(killer == null ? player : killer, player);
        mainHoloScoreboard.update();

        for (Player otherPlayers : getAllPlayersInStadium()) {

            if (team1.getElimTracker().getTeamMembersAlive() == 1 && team2.getElimTracker().getTeamMembersAlive() == 1)
                playPigstep();

            if (otherPlayers == killer)
                otherPlayers.sendTitle(getBothTeamAliveCountString(),ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + player.getDisplayName(), 5, 15, 5);
            else
                otherPlayers.sendTitle(getBothTeamAliveCountString(), "", 5, 15, 5);

            Team otherPlayersTeam = getPlayerTeam(otherPlayers);
            if (otherPlayersTeam == null || getOpposingTeam(otherPlayersTeam) == team)
                otherPlayers.playSound(otherPlayers.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            else
                otherPlayers.playSound(otherPlayers.getLocation(), Sound.ENTITY_ENDERMAN_HURT, 1, .8f);

        }

        if (wasQuit)
            Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + Phrases.getRandomSuicidePhrase(player));
        else if (killer != null && killer != player && getPlayerTeam(player) == getPlayerTeam(killer))
            Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + Phrases.getRandomTeamKillPhrase(player, killer));
        else
            Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] " + (killer != null ? Phrases.getRandomKilledPhrase(player, killer) : Phrases.getRandomSuicidePhrase(player)));

        if (team.getElimTracker().teamIsDead())
            exitGameRoundPhase(getOpposingTeam(team));
        else
            stadium.getArena().shrinkArena();

        Fireworks.spawnFireworksInstantly(player.getLocation(), ColorTranslator.translateChatColorToColor(team.getTeamColor()));
    }

    private void playPigstep() {

        if (!Dodgebolt.getInstance().getConfig().getBoolean(ConfigManager.ONEVSONE_PIGSTEP))
            return;

        for (Player p : getAllPlayersInStadium())
            p.playSound(p.getEyeLocation().add(0, 60, 0), Sound.MUSIC_DISC_PIGSTEP, .75f, 1);
    }

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
        if (event.getFrom().getBlock() != event.getTo().getBlock() && outOfBowRange(event.getTo()) && !outOfBowRange(event.getFrom()))
            event.getPlayer().getInventory().setItem(0, Items.getInactiveBow());
        // If the player went back into play give them the bow back
        else if (event.getFrom().getBlock() != event.getTo().getBlock() && outOfBowRange(event.getFrom()) && !outOfBowRange(event.getTo()))
            event.getPlayer().getInventory().setItem(0, Items.getDodgeboltBow());
    }

    @EventHandler
    public void onPlayerClickBowInInventory(InventoryClickEvent event) {

        if (state == DodgeboltGameState.WAITING)
            return;

        if (getPlayerTeam((Player) event.getWhoClicked()) == null)
            return;

        // If the inventory slot is slot 0 don't let them do anything
        if (event.getSlot() == 0)
            event.setCancelled(true);

    }

    @EventHandler
    public void onPlayerAttemptOffhandSlotOne(PlayerSwapHandItemsEvent event) {

        if (state == DodgeboltGameState.WAITING)
            return;

        if (getPlayerTeam(event.getPlayer()) == null)
            return;

        if (event.getPlayer().getInventory().getHeldItemSlot() == 0)
            event.setCancelled(true);
    }

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

    @EventHandler
    public void onPlayerPVP(EntityDamageByEntityEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getDamager().getLocation()))
            return;


        // Don't do any damage no matter what the cause or source is
        event.setDamage(0);

        if (!(event.getEntity() instanceof Player))
            return;

        if (!(event.getDamager() instanceof Arrow))
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE)
            return;

        // At this point an arrow is being shot we can override the damage
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, bowDamagePercent / 5.0);
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        if (state != DodgeboltGameState.INGAME)
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        Team playersTeam = getPlayerTeam((Player) event.getEntity());
        if (playersTeam == null)
            return;

        if (event.getProjectile() instanceof Arrow) {
            ((Arrow) event.getProjectile()).setColor(ColorTranslator.translateChatColorToColor(playersTeam.getTeamColor()));
            event.getProjectile().setGlowing(true);
        }

        if (outOfBowRange(event.getEntity().getLocation())) {
            event.setCancelled(true);
        }

        PlayerStats.addStatistic((Player) event.getEntity(), PlayerStats.ARROWS_FIRED);
        gameStatisticsManager.registerArrowShot(((Player) event.getEntity()));
        mainHoloScoreboard.update();
    }

    @EventHandler
    public void onArrowHitPlayer(EntityDamageByEntityEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        // Specifically find when a player is hit by an arrow fired by another player
        if (state != DodgeboltGameState.INGAME)
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        if (!(event.getDamager() instanceof Projectile))
            return;

        ProjectileSource src = ((Projectile) event.getDamager()).getShooter();

        if (!(src instanceof Player))
            return;

        Player playerSrc = (Player) src;

        if (getPlayerTeam(playerSrc) == null)
            return;

        gameStatisticsManager.registerArrowHit(playerSrc);
        PlayerStats.addStatistic(playerSrc, PlayerStats.ARROWS_HIT);
        mainHoloScoreboard.update();

    }

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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getPlayer().getLocation()))
            return;

        // If this player is not opped then they cannot break blocks
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        if (state == DodgeboltGameState.INGAME && event.getEntity().getItemStack().getType() == Material.ARROW)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getPlayer().getLocation()))
            return;

        if (isInProgress() && event.getItemDrop().getItemStack().getType() != Material.ARROW)
            event.setCancelled(true);
        else if (state == DodgeboltGameState.INGAME)
            event.getItemDrop().setGlowing(true);
    }

    @EventHandler
    public void onPlayerPostDeath(ItemSpawnEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getEntity().getLocation()))
            return;

        if (state == DodgeboltGameState.INGAME && event.getEntity().getItemStack().getType() == Material.ARROW)
            event.getEntity().setGlowing(true);
    }

    @EventHandler
    public void onClickedArmor(InventoryClickEvent event) {

        // If this entity is not in the stadium then ignore them
        if (!stadium.isInStadium(event.getWhoClicked().getLocation()))
            return;

        if (isInProgress() && event.getSlotType() == InventoryType.SlotType.ARMOR)
            event.setCancelled(true);
    }

}
