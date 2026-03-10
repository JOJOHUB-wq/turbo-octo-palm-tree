package ua.atherium.hhevents.managers;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ProtectionManager {

    private final List<Location> protectedLocations = new ArrayList<>();
    private static final int PROTECTION_RADIUS = 10;

    public void addProtection(Location location) {
        protectedLocations.add(location);
    }

    public void removeProtection(Location location) {
        protectedLocations.remove(location);
    }

    public boolean isProtected(Location checkLoc) {
        if (checkLoc == null || checkLoc.getWorld() == null) return false;

        for (Location center : protectedLocations) {
            if (center == null || center.getWorld() == null || !center.getWorld().equals(checkLoc.getWorld())) {
                continue;
            }

            if (checkLoc.distanceSquared(center) <= (PROTECTION_RADIUS * PROTECTION_RADIUS)) {
                return true;
            }
        }
        return false;
    }
}
