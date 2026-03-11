package me.sanjy33.amavyadecoration.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationKey {

    public static String locationToString(Location location) {
        return location.getWorld().getName() + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ();
    }

    public static Location stringToLocation(String key) {
        String[] parts = key.split("\\|");
        assert parts.length == 4;
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }

}
