package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

public class SpringsEnchant extends CustomEnchant {

    public SpringsEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    public void applyEffect(Player player, int level) {
         if (AtheriumEnchants.getInstance().getCooldownManager().isOnCooldown(player, key)) {
            return;
        }

        // The amplifier determines the strength of the jump.
        // A value of 0 might be a standard jump, > 0 for higher.
        double jumpAmplifier = config.getDouble(key + ".levels." + level + ".jump_amplifier", 1.0);

        // Apply an upward velocity boost, and also a boost in the direction the player is looking.
        Vector direction = player.getLocation().getDirection();
        Vector jumpVector = new Vector(direction.getX() * 0.5, 0.8 * jumpAmplifier, direction.getZ() * 0.5);

        player.setVelocity(player.getVelocity().add(jumpVector));

        // Set a short cooldown to prevent spamming jumps.
        int cooldown = config.getInt(key + ".levels." + level + ".cooldown", 1); // Default to 1 second
        AtheriumEnchants.getInstance().getCooldownManager().setCooldown(player, key, cooldown);
    }
}