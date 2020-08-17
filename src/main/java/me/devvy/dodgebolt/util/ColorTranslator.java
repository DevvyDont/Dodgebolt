package me.devvy.dodgebolt.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ColorTranslator {

    public static Material chatColorToCarpet(ChatColor color) {

        switch (color) {

            case BLUE:
            case DARK_BLUE:
                return Material.BLUE_CARPET;
            case LIGHT_PURPLE:
                return Material.MAGENTA_CARPET;
            case DARK_PURPLE:
                return Material.PURPLE_CARPET;
            case RED:
                return Material.PINK_CARPET;
            case AQUA:
                return Material.LIGHT_BLUE_CARPET;
            case GOLD:
                return Material.ORANGE_CARPET;
            case GRAY:
            case DARK_GRAY:
                return Material.GRAY_CARPET;
            case BLACK:
                return Material.BROWN_CARPET;
            case YELLOW:
                return Material.YELLOW_CARPET;
            case DARK_RED:
                return Material.RED_CARPET;
            case DARK_GREEN:
                return Material.GREEN_CARPET;
            case GREEN:
                return Material.LIME_CARPET;
            case WHITE:
                return Material.LIGHT_GRAY_CARPET;
            case DARK_AQUA:
                return Material.CYAN_CARPET;
            default:
                throw new IllegalArgumentException("Invalid color " + color + "!");

        }

    }

    public static Material getRandomCarpetColor() {

        Material carpet = null;

        while (carpet == null) {

            try {
                carpet = chatColorToCarpet(ChatColor.values()[(int) (Math.random() * ChatColor.values().length)]);
            } catch (IllegalArgumentException ignored) { }

        }

        return carpet;
    }

}
