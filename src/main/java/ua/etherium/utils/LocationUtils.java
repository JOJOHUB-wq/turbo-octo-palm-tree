package ua.etherium.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.stream.Collectors;

public class LocationUtils {

    /**
     * Gets a collection of nearby entities of a specific type within a given radius.
     *
     * @param location The center location to search from.
     * @param radius   The radius to search within.
     * @param type     The class of the entity to look for (e.g., Player.class).
     * @param <T>      The type of the entity.
     * @return A collection of entities of the specified type.
     */
    public static <T extends Entity> Collection<T> getNearbyEntities(Location location, double radius, Class<T> type) {
        World world = location.getWorld();
        if (world == null) {
            return java.util.Collections.emptyList();
        }

        return world.getNearbyEntities(location, radius, radius, radius, entity -> type.isInstance(entity))
                .stream()
                .map(type::cast)
                .collect(Collectors.toList());
    }

    /**
     * Checks if two locations are within a certain distance of each other.
     * This is more efficient than calculating the exact distance if you only need to check if it's within a range.
     *
     * @param loc1     The first location.
     * @param loc2     The second location.
     * @param distance The distance to check against.
     * @return true if the distance between the locations is less than or equal to the specified distance.
     */
    public static boolean isWithinDistance(Location loc1, Location loc2, double distance) {
        if (loc1.getWorld() != loc2.getWorld()) {
            return false;
        }
        return loc1.distanceSquared(loc2) <= distance * distance;
    }
}