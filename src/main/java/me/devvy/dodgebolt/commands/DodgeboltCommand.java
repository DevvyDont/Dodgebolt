package me.devvy.dodgebolt.commands;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.statistics.EventTracker;
import me.devvy.dodgebolt.util.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DodgeboltCommand implements CommandExecutor, TabCompleter {

    public static final String[] OP_SUBCOMMANDS = {"setlocation", "reload", "event", "eventstop", "clearsession"};
    public static final String[] NORMAL_SUBCOMMANDS = {};

    private EventTracker eventTracker;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        // Ignore console
        if (!(commandSender instanceof Player))
            return true;

        Player sender = (Player) commandSender;

        // No args? Tell them
        if (strings.length == 0) {
            sender.sendMessage(Component.text("Arguments needed!", NamedTextColor.RED));
            return true;
        }

        // 1 arg?
        if (strings.length == 1) {

            String cmd = strings[0].toLowerCase();

            // Setting spawn? (OP command)
            if (sender.isOp() && cmd.equals("setlocation"))
                return handleSetLocation(sender);

            // Reloading? (OP command)
            if (sender.isOp() && cmd.equals("reload"))
                return handleReload(sender);

            // Leaderboard spawn? (OP command)
            if (sender.isOp() && cmd.equals("event"))
                return handleEvent(sender);

            // Stop Leaderboard? (OP command)
            if (sender.isOp() && cmd.equals("eventstop"))
                return handleEventStop(sender);

            // Clear session stats? (OP command)
            if (sender.isOp() && cmd.equals("clearsession"))
                return handleClearSession(sender);


        }


        return false;
    }

    private boolean handleClearSession(Player sender) {
        sender.sendMessage(Component.text("Cleared Dodgebolt session stats!", NamedTextColor.GREEN));
        Dodgebolt.getInstance().getGame().getGameStatisticsManager().resetSessionStats();
        return true;
    }

    private boolean handleEventStop(Player sender) {

        if (eventTracker == null)
            return true;

        eventTracker.cleanup();
        eventTracker = null;
        sender.sendMessage("Disabled the event tracker");
        return true;
    }

    private boolean handleEvent(Player sender) {

        if (eventTracker != null)
            return handleEventStop(sender);

        eventTracker = new EventTracker();

        sender.sendMessage("Enabled the event tracker");

        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        List<String> validArgs = new ArrayList<>();

        if (strings.length == 0)
            return null;

        if (strings.length == 1) {

            String soFar = strings[0];
            List<String> availableCommands = new ArrayList<>(List.of(NORMAL_SUBCOMMANDS));
            if (commandSender.isOp())
                availableCommands.addAll(List.of(OP_SUBCOMMANDS));

            for (String availCmd : availableCommands)
                if (availCmd.toLowerCase().contains(soFar.toLowerCase()))
                    validArgs.add(availCmd);

            return validArgs;

        }

        return null;
    }

    private boolean handleSetLocation(Player sender) {

        ConfigManager.setArenaLocation(sender.getLocation());
        Dodgebolt.getInstance().saveConfig();

        sender.sendMessage(
                Component.text("Set Dodgebolt arena origin!", TextColor.color(0, 200, 0))
        );
        sender.playSound(sender.getEyeLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f);
        return true;

    }

    private boolean handleReload(Player sender) {
        Dodgebolt.getInstance().reload();

        sender.sendMessage(
                Component.text("Successfully regenerated arena!", TextColor.color(0, 200, 0))
        );

        sender.playSound(sender.getEyeLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f);
        return true;
    }



}
