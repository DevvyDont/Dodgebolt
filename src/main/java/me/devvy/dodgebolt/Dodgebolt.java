package me.devvy.dodgebolt;

import me.devvy.dodgebolt.game.DodgeboltGame;

import org.bukkit.plugin.java.JavaPlugin;

public final class Dodgebolt extends JavaPlugin {

    private DodgeboltGame game;

    @Override
    public void onEnable() {
        game = new DodgeboltGame();
        getServer().getPluginManager().registerEvents(game, this);
    }

    @Override
    public void onDisable() {
        game.cleanup();
    }
}