package me.devvy.dodgebolt.util;

import me.devvy.dodgebolt.Dodgebolt;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Fireworks {

    public static Color translateChatColorToColor(ChatColor chatColor) {

        switch (chatColor) {
            case DARK_GRAY:
            case GRAY:
                return Color.GRAY;

            case DARK_AQUA:
                return Color.TEAL;

            case DARK_BLUE:

            case BLUE:
                return Color.BLUE;

            case WHITE:
                return Color.WHITE;

            case GREEN:

            case DARK_GREEN:
                return Color.GREEN;

            case DARK_RED:

            case RED:
                return Color.RED;

            case YELLOW:
                return Color.YELLOW;

            case BLACK:
                return Color.BLACK;

            case GOLD:
                return Color.ORANGE;

            case AQUA:
                return Color.AQUA;

            case LIGHT_PURPLE:

            case DARK_PURPLE:
                return Color.PURPLE;

            default:
                throw new IllegalArgumentException("Not a color!");
        }

    }

    public static void spawnFireworksInstantly(Location location, Color color) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location.add(0, .5, 0), EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
        effectBuilder.with(FireworkEffect.Type.BALL_LARGE);
        effectBuilder.withColor(color);
        meta.addEffect(effectBuilder.build());
        firework.setFireworkMeta(meta);
        firework.detonate();
    }

    public static void spawnVictoryFireworks(Player player, Color teamColor) {

        for (int i = 0; i < 3; i++) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation().add(Math.random() - .5, 0, Math.random() - .5), EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.setPower((int) (Math.random() * 4));
                    FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
                    effectBuilder.with(FireworkEffect.Type.STAR);
                    effectBuilder.withColor(teamColor);
                    effectBuilder.trail(true);
                    effectBuilder.withFade(Color.LIME);
                    meta.addEffect(effectBuilder.build());
                    firework.setFireworkMeta(meta);
                }
            }.runTaskLater(Dodgebolt.getPlugin(Dodgebolt.class), i * 5);

        }
    }

}
