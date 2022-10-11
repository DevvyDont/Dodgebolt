package me.devvy.dodgebolt;

import me.devvy.dodgebolt.game.DodgeboltGame;

import me.devvy.dodgebolt.hologram.HolographicDynamicScoreboard;
import me.devvy.dodgebolt.util.ConfigManager;
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

        game = new DodgeboltGame();
        getServer().getPluginManager().registerEvents(game, this);
    }

    @Override
    public void onDisable() {
        if (game != null)
            game.cleanup();
    }
}
