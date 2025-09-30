package ua.etherium.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public void setCooldown(Player player, String key, int seconds) {
        if (seconds <= 0) {
            return;
        }
        long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key, expiryTime);
    }

    public boolean isOnCooldown(Player player, String key) {
        return getRemainingCooldownMillis(player.getUniqueId(), key) > 0;
    }

    private long getRemainingCooldownMillis(UUID uuid, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null || !playerCooldowns.containsKey(key)) {
            return 0;
        }

        long expiryTime = playerCooldowns.get(key);
        long remaining = expiryTime - System.currentTimeMillis();

        if (remaining <= 0) {
            playerCooldowns.remove(key);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(uuid);
            }
            return 0;
        }
        return remaining;
    }
}