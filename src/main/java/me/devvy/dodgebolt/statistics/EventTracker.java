package me.devvy.dodgebolt.statistics;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.events.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

public class EventTracker implements Listener {

    public EventTracker() {
        Dodgebolt.getInstance().getServer().getPluginManager().registerEvents(this, Dodgebolt.getInstance());
    }


    /**
     * Ugly message to dump a server message on current session stats
     */
    public void dump() {

        List<PlayerSessionStatContainer> allPlayers = new ArrayList<>(Dodgebolt.getInstance().getGame().getGameStatisticsManager().getAllSessionContainers());

        Bukkit.broadcast(Component.text("EVENT CURRENT STANDINGS", NamedTextColor.GOLD, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text("-----------------------", NamedTextColor.GRAY, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text(" ", NamedTextColor.GRAY, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text("ROUND WINS", NamedTextColor.AQUA, TextDecoration.BOLD));

        // Sort by wins
        allPlayers.sort(Comparator.comparingInt(PlayerMatchStatContainer::getRoundsWon));

        int x = 1;
        for (PlayerSessionStatContainer container : allPlayers) {
            if (x >= 4)
                break;

            String name = Bukkit.getPlayer(container.getOwner()) != null ? Bukkit.getPlayer(container.getOwner()).getName() : "?????";
            Bukkit.broadcast(Component.text(x + ": " + name + " - " + container.getRoundsWon(), NamedTextColor.GRAY));
            x++;
        }

        Bukkit.broadcast(Component.text("-----------------------", NamedTextColor.GRAY, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text(" ", NamedTextColor.GRAY, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text("CROWN OBTAINS", NamedTextColor.AQUA, TextDecoration.BOLD));

        // Sort by Crowns
        allPlayers.sort(Comparator.comparingInt(PlayerSessionStatContainer::getCrownsAchieved));

        x = 1;
        for (PlayerSessionStatContainer container : allPlayers) {
            if (x >= 4)
                break;

            String name = Bukkit.getPlayer(container.getOwner()) != null ? Bukkit.getPlayer(container.getOwner()).getName() : "?????";
            Bukkit.broadcast(Component.text(x + ": " + name + " - " + container.getCrownsAchieved(), NamedTextColor.GRAY));
            x++;
        }

        Bukkit.broadcast(Component.text("-----------------------", NamedTextColor.GRAY, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text(" ", NamedTextColor.GRAY, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text("KILLS", NamedTextColor.AQUA, TextDecoration.BOLD));

        // Sort by Accuracy
        allPlayers.sort(Comparator.comparingDouble(PlayerSessionStatContainer::getRawAccuracy));

        x = 1;
        for (PlayerSessionStatContainer container : allPlayers) {
            if (x >= 4)
                break;

            String name = Bukkit.getPlayer(container.getOwner()) != null ? Bukkit.getPlayer(container.getOwner()).getName() : "?????";
            Bukkit.broadcast(Component.text(x + ": " + name + " - " + container.getAccuracy() + "%", NamedTextColor.GRAY));
            x++;
        }
        Bukkit.broadcast(Component.text("-----------------------", NamedTextColor.GRAY, TextDecoration.BOLD));
    }

    public void cleanup() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onGameEnd(GameEndedEvent event) {
        dump();
    }

}
