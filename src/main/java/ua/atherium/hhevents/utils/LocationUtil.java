package ua.atherium.hhevents.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class LocationUtil {

    private LocationUtil() {}

    public static CompletableFuture<Location> findSafeLocation(World world, int radius) {
        return CompletableFuture.supplyAsync(() -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < 1000; i++) {
                int x = random.nextInt(-radius, radius);
                int z = random.nextInt(-radius, radius);

                int highestY = getHighestY(world, x, z);
                if (highestY <= 0) continue;

                Location loc = new Location(world, x, highestY, z);
                if (isSafe(loc)) {
                    return loc;
                }
            }
            return null;
        });
    }

    private static int getHighestY(World world, int x, int z) {
        for (int y = 255; y > 0; y--) {
            Block b = world.getBlockAt(x, y, z);
            if (!b.isEmpty() && !b.isLiquid() && !b.getType().isAir()) {
                return y;
            }
        }
        return -1;
    }

    private static boolean isSafe(Location loc) {
        Block floor = loc.getBlock();
        Material mat = floor.getType();

        if (mat.name().contains("WATER") || mat.name().contains("LAVA") || mat.name().contains("MAGMA")
                || mat.name().contains("LEAVES") || mat.name().contains("ICE")) {
            return false;
        }

        Block above = loc.clone().add(0, 1, 0).getBlock();
        Block above2 = loc.clone().add(0, 2, 0).getBlock();

        return above.isPassable() && above2.isPassable();
    }
}
