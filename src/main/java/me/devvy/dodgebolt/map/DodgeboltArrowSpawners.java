package me.devvy.dodgebolt.map;

import me.devvy.dodgebolt.Dodgebolt;
import me.devvy.dodgebolt.game.DodgeboltGame;
import me.devvy.dodgebolt.game.DodgeboltGameState;
import me.devvy.dodgebolt.util.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class DodgeboltArrowSpawners implements Listener {

    private final DodgeboltGame game;
    private final List<Location> spawnLocations;

    public DodgeboltArrowSpawners(DodgeboltGame game, List<Location> spawnLocation) {
        this.game = game;
        this.spawnLocations = spawnLocation;

        for (Location location : spawnLocations)
            spawnArrow(location);
        Dodgebolt.getPlugin(Dodgebolt.class).getServer().getPluginManager().registerEvents(this, Dodgebolt.getPlugin(Dodgebolt.class));
    }

    public void spawnArrow(Location location) {
        Arrow arrow = location.getWorld().spawnArrow(location, new Vector(0, 1, 0), .4f, 0);
        setupArrow(arrow);
    }

    public void setupArrow(Arrow arrow) {
        arrow.setGlowing(true);
        arrow.setInvulnerable(true);
    }

    public void delete() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerShotArrow(ProjectileLaunchEvent event) {

        if (!(event.getEntity().getShooter() instanceof Player))
            return;

        setupArrow((Arrow) event.getEntity());
    }

    @EventHandler
    public void onArrowStackMerge(ItemMergeEvent event) {
        if (game.getState() == DodgeboltGameState.INGAME && event.getEntity().getItemStack().getType() == Material.ARROW)
            event.setCancelled(true);
    }

    @EventHandler
    public void onArrowBurned(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.DROPPED_ITEM && event.getCause() == EntityDamageEvent.DamageCause.LAVA) {

            if (((Item)event.getEntity()).getItemStack().getType() == Material.ARROW) {
                Location closest = spawnLocations.get(0);
                for (Location location : spawnLocations)
                    if (event.getEntity().getLocation().distance(location) < event.getEntity().getLocation().distance(closest))
                        closest = location;

                int amount = ((Item) event.getEntity()).getItemStack().getAmount();
                for (int i = 0; i < amount; i++)
                    spawnArrow(closest);

                event.getEntity().remove();
            }


        }

    }

}
