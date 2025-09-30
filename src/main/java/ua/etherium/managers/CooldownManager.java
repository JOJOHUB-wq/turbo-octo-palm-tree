package ua.etherium.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    /**
     * Sets a cooldown for a specific player and enchantment.
     *
     * @param player     The player to set the cooldown for.
     * @param key        A unique identifier for the cooldown (e.g., enchant key).
     * @param seconds    The duration of the cooldown in seconds.
     */
    public void setCooldown(Player player, String key, int seconds) {
        if (seconds <= 0) {
            return;
        }
        long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key, expiryTime);
    }

    /**
     * Checks if a player is currently on cooldown for a specific action.
     *
     * @param player The player to check.
     * @param key    The unique identifier for the cooldown.
     * @return True if the player is on cooldown, false otherwise.
     */
    public boolean isOnCooldown(Player player, String key) {
        return getRemainingCooldownMillis(player, key) > 0;
    }

    /**
     * Gets the remaining cooldown time in milliseconds.
     *
     * @param player The player to check.
     * @param key    The unique identifier for the cooldown.
     * @return The remaining time in milliseconds, or 0 if not on cooldown.
     */
    private long getRemainingCooldownMillis(Player player, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null || !playerCooldowns.containsKey(key)) {
            return 0;
        }

        long expiryTime = playerCooldowns.get(key);
        long remaining = expiryTime - System.currentTimeMillis();

        if (remaining <= 0) {
            playerCooldowns.remove(key);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(player.getUniqueId());
            }
            return 0;
        }
        return remaining;
    }

    /**
     * Gets the remaining cooldown time in a specified time unit.
     *
     * @param player   The player to check.
     * @param key      The unique identifier for the cooldown.
     * @param unit     The time unit to return the result in.
     * @return The remaining cooldown time in the specified unit.
     */
    public long getRemainingCooldown(Player player, String key, TimeUnit unit) {
        long millis = getRemainingCooldownMillis(player, key);
        return unit.convert(millis, TimeUnit.MILLISECONDS);
    }
}