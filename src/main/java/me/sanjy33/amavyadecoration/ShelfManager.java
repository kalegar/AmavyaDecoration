package me.sanjy33.amavyadecoration;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class ShelfManager {

    private final AmavyaDecoration plugin;
    private final Map<String, Shelf> shelves = new HashMap<>();
    private final Set<String> creatingShelves = new HashSet<>();

    public ShelfManager(AmavyaDecoration plugin) {
        this.plugin = plugin;
    }

    private String locationToString(Location location) {
        return location.getWorld().toString() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    public Shelf getShelfAtLocation(Location location) {
        String key = locationToString(location);
        return shelves.get(key);
    }

    public Shelf createShelfAtLocation(Location location) {
        String key = locationToString(location);
        Shelf shelf = new Shelf(location);
        shelves.put(key, shelf);
        plugin.particleLibHook.addBurstEffect(location, Particle.WAX_OFF, 2, 1, 0.1, 0.5, 6);
        return shelf;
    }

    public void deleteShelfAtLocation(Location location, boolean dropItems) {
        String key = locationToString(location);
        if (!shelves.containsKey(key)) return;
        Shelf shelf = shelves.get(key);
        if (dropItems) {
            for (int i = 0; i < Shelf.MAX_SLOTS; i++) {
                Shelf.ShelfSlot slot = shelf.getSlot(i);
                if (slot != null)
                    shelf.getLocation().getWorld().dropItem(shelf.getLocation(), new ItemStack(slot.getItemStack()));
            }
        }
        shelf.cleanupDisplays();
        shelves.remove(key);
    }

    public boolean isCreatingShelf(UUID uuid, Location location) {
        String key = uuid.toString()+"_"+locationToString(location);
        return creatingShelves.contains(key);
    }

    public void startCreatingShelf(UUID uuid, Location location) {
        final String key = uuid.toString()+"_"+locationToString(location);
        creatingShelves.add(key);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            creatingShelves.remove(key);
        }, 60L);

    }

    public void cleanupDisplays() {
        for (Shelf shelf : shelves.values()) {
            shelf.cleanupDisplays();
        }
    }

    public void refreshDisplays() {
        for (Shelf shelf : shelves.values()) {
            shelf.refreshDisplay();
        }
    }

    public void refreshNearbyDisplays(Location location) {
        for (Shelf shelf : shelves.values()) {
            if (shelf.getLocation().getWorld().getName().equalsIgnoreCase(location.getWorld().getName()) && shelf.getLocation().distanceSquared(location) < 512) {
                shelf.refreshDisplay();
            }
        }
    }

    public void refreshDisplaysInChunk(Chunk chunk) {
        for (Shelf shelf : shelves.values()) {
            Chunk shelfChunk = shelf.getLocation().getChunk();
            if (shelfChunk.getX() == chunk.getX() && shelfChunk.getZ() == chunk.getZ() && shelfChunk.getWorld().getName().equalsIgnoreCase(chunk.getWorld().getName())) {
                shelf.refreshDisplay();
            }
        }
    }

    public Shelf interactWithShelf() {
        return null;
    }

    public void save(ConfigurationSection section) {
        int i = 0;
        for (String key : shelves.keySet()) {
            ConfigurationSection subSection = section.createSection(Integer.toString(i));
            shelves.get(key).save(subSection);
            i++;
        }
    }

    public void load(ConfigurationSection section) {
        if (section == null) return;
        Set<String> keys = section.getKeys(false);
        int loaded = 0;
        for (String key : keys) {
            ConfigurationSection subSection = section.getConfigurationSection(key);
            if (subSection == null) continue;
            Shelf shelf = Shelf.load(subSection);
            shelves.put(locationToString(shelf.getLocation()), shelf);
            loaded++;
        }
        plugin.getLogger().log(Level.INFO, "Loaded " + loaded + " shelves.");
    }


}
