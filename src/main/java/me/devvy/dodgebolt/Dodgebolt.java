package me.devvy.dodgebolt;

import me.devvy.dodgebolt.game.DodgeboltGame;

import me.devvy.dodgebolt.util.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Dodgebolt extends JavaPlugin {

    private DodgeboltGame game;

    @Override
    public void onEnable() {
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
