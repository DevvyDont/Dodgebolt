package me.devvy.dodgebolt.util;

import me.devvy.dodgebolt.Dodgebolt;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

// Simple functions to get/read configs, might oopify later but this plugin is not very config heavy so maybe at a later time
public class ConfigManager {

    public final static String ARENA_LOCATION = "arena-location";
    public final static String OP_CHANGE_SETTINGS = "op-change-settings";
    public final static String ROUND_WIN_LIMIT = "round-win-limit";
    public final static String WIN_BY_2 = "win-by-2";

    public final static String ONEVSONE_PIGSTEP = "one-vs-one-pigstep";

    public static void setupDefaultConfig() {

        Dodgebolt plugin = Dodgebolt.getPlugin(Dodgebolt.class);

        FileConfiguration cfg = plugin.getConfig();

        cfg.addDefault(OP_CHANGE_SETTINGS, true);  // Require ops to change settings during the game? Useful for private servers
        cfg.addDefault(ROUND_WIN_LIMIT, 5);  // How many rounds do we have to win to win the entire game? Note that the win by 2 rule will still add more rounds if necessary
        cfg.addDefault(WIN_BY_2, true);  // Should we require a team to win by 2 rounds? kinda like tennis
        cfg.addDefault(ONEVSONE_PIGSTEP, true);  // In a 1v1 should we play pigstep

        cfg.options().copyDefaults(true);
        plugin.saveDefaultConfig();


    }

    public static Location getArenaLocation() {
        return Dodgebolt.getInstance().getConfig().getLocation(ARENA_LOCATION, null);
    }

    public static void setArenaLocation(Location location) {
        Dodgebolt.getInstance().getConfig().set(ARENA_LOCATION, location);
    }



}
