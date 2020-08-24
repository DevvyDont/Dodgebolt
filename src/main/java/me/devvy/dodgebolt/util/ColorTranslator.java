package me.devvy.dodgebolt.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

public class ColorTranslator {

    public static final ChatColor[] ALLOWED_TEAM_COLORS = new ChatColor[] {
            ChatColor.GREEN,
            ChatColor.DARK_GREEN,
            ChatColor.BLUE,
            ChatColor.DARK_RED,
            ChatColor.YELLOW,
            ChatColor.GOLD,
            ChatColor.AQUA,
            ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_AQUA
    };

    public static String chatColorToActualName(ChatColor color) {

        switch (color) {

            case GREEN:
                return "Lime";
            case DARK_GREEN:
                return "Green";
            case BLUE:
                return "Blue";
            case DARK_RED:
                return "Red";
            case YELLOW:
                return "Yellow";
            case GOLD:
                return "Orange";
            case AQUA:
                return "Aqua";
            case LIGHT_PURPLE:
                return "Pink";
            case DARK_PURPLE:
                return "Purple";
            case DARK_AQUA:
                return "Cyan";

            default:
                throw new IllegalArgumentException("Not a color! " + color);

        }

    }

    public static Material chatColorToCarpet(ChatColor color) {

        switch (color) {
            case BLUE:
                return Material.BLUE_CARPET;
            case LIGHT_PURPLE:
                return Material.MAGENTA_CARPET;
            case DARK_PURPLE:
                return Material.PURPLE_CARPET;
            case AQUA:
                return Material.LIGHT_BLUE_CARPET;
            case GOLD:
                return Material.ORANGE_CARPET;
            case YELLOW:
                return Material.YELLOW_CARPET;
            case DARK_RED:
                return Material.RED_CARPET;
            case DARK_GREEN:
                return Material.GREEN_CARPET;
            case GREEN:
                return Material.LIME_CARPET;
            case DARK_AQUA:
                return Material.CYAN_CARPET;
            default:
                throw new IllegalArgumentException("Invalid color " + color + "!");
        }

    }

    public static Material chatColorToWool(ChatColor color) {
        return Material.valueOf(chatColorToCarpet(color).toString().replace("CARPET", "WOOL"));
    }

    public static Material chatColorToConcrete(ChatColor color) {
        return Material.valueOf(chatColorToCarpet(color).toString().replace("CARPET", "CONCRETE"));
    }

    public static Material chatColorToGlass(ChatColor color) {
        return Material.valueOf(chatColorToCarpet(color).toString().replace("CARPET", "STAINED_GLASS"));
    }

    public static Material chatColorToGlassPane(ChatColor color) {
        return Material.valueOf(chatColorToCarpet(color).toString().replace("CARPET", "STAINED_GLASS_PANE"));
    }

    public static Material chatColorToConcretePowder(ChatColor color) {
        return Material.valueOf(chatColorToCarpet(color).toString().replace("CARPET", "CONCRETE_POWDER"));
    }

    public static Material chatColorToTerracotta(ChatColor color) {
        return Material.valueOf(chatColorToCarpet(color).toString().replace("CARPET", "TERRACOTTA"));
    }

    public static boolean isTeamBlock(Material material) {

        String name = material.toString();

        if (name.contains("CONCRETE") || name.contains("STAINED_GLASS") || name.contains("TERRACOTTA") || name.contains("CARPET") )
            return !(name.contains("BLACK") || name.contains("WHITE") || name.contains("PINK") || name.contains("GRAY") || name.contains("BROWN"));
        return false;
    }

    public static Material getTranslatedTeamBlock(Material oldMaterial, ChatColor teamColor) {

        if (!isTeamBlock(oldMaterial))
            throw new IllegalArgumentException( oldMaterial + " is not a team block!");

        if (oldMaterial.toString().contains("CONCRETE_POWDER"))
            return chatColorToConcretePowder(teamColor);
        else if (oldMaterial.toString().contains("STAINED_GLASS_PANE"))
            return chatColorToGlassPane(teamColor);
        else if (oldMaterial.toString().contains("STAINED_GLASS"))
            return chatColorToGlass(teamColor);
        else if (oldMaterial.toString().contains("TERRACOTTA"))
            return chatColorToTerracotta(teamColor);
        else if (oldMaterial.toString().contains("CONCRETE"))
            return chatColorToConcrete(teamColor);
        else if (oldMaterial.toString().contains("WOOL"))
            return chatColorToWool(teamColor);
        else if (oldMaterial.toString().contains("CARPET"))
            return chatColorToCarpet(teamColor);
        else
            throw new IllegalArgumentException("Unknown team block " + oldMaterial);

    }

    public static ChatColor getRandomValidTeamColor() {
        return ALLOWED_TEAM_COLORS[(int) (Math.random() * ALLOWED_TEAM_COLORS.length)];
    }

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

}
