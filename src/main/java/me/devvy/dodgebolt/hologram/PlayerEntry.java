package me.devvy.dodgebolt.hologram;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerEntry {

    private UUID uuid;
    private int kills;
    private int deaths;
    private int arrowsFired;
    private int arrowsHit;

    public PlayerEntry(UUID uuid, int kills, int deaths, int arrowsFired, int arrowsHit) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
        this.arrowsFired = arrowsFired;
        this.arrowsHit = arrowsHit;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public float getAccuracy() {
        if (arrowsFired <= 0)
            return 0;

        return (float) arrowsHit / (float) arrowsFired;
    }

    public String getFormattedAccuracy() {
        return String.format("%d%%", Math.round(getAccuracy() * 100));
    }

    public String getStatString() {
        return String.format("%s / %s / %s", kills, deaths, getFormattedAccuracy());
    }
}
