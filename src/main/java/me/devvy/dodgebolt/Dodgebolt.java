package me.devvy.dodgebolt;

import me.devvy.dodgebolt.commands.DodgeboltCommand;
import me.devvy.dodgebolt.game.DodgeboltGame;

import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Dodgebolt extends JavaPlugin {

    private static Dodgebolt INSTANCE;

    public static Dodgebolt getInstance() {
        return INSTANCE;
    }

    private DodgeboltGame game;

    public DodgeboltGame getGame() {
        return game;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        ConfigManager.setupDefaultConfig();

        try {
            reload();
        } catch (IllegalStateException e) {
            getLogger().warning(e.getMessage());
            getLogger().warning("When the issue is resolved, be sure to do /dodgebolt reload");
        }

        DodgeboltCommand dodgeboltCommand = new DodgeboltCommand();
        getCommand("dodgebolt").setExecutor(dodgeboltCommand);
        getCommand("dodgebolt").setTabCompleter(dodgeboltCommand);

    }

    public void unload() {
        if (game != null) {
            game.endGame(true);
            HandlerList.unregisterAll(game);
            game.reset();
            game.cleanup();
        }
    }

    public void reload() throws IllegalStateException {
        unload();
        game = new DodgeboltGame();
        getServer().getPluginManager().registerEvents(game, this);
    }

    @Override
    public void onDisable() {
        unload();
    }
}
