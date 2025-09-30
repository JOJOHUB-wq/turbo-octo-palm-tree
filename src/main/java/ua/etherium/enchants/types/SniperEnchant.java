package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;

public class SniperEnchant extends CustomEnchant {

    private final Random random = new Random();

    public SniperEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    /**
     * Calculates the bonus damage based on distance.
     *
     * @param shooter  The player who shot the arrow.
     * @param distance The distance in blocks to the target.
     * @param level    The level of the enchantment.
     * @return The calculated bonus damage, or 0 if the effect doesn't trigger.
     */
    public double getBonusDamage(Player shooter, double distance, int level) {
        if (AtheriumEnchants.getInstance().getCooldownManager().isOnCooldown(shooter, key)) {
            return 0.0;
        }

        int chance = config.getInt(key + ".levels." + level + ".chance", 0);
        if (random.nextInt(100) < chance) {
            double damagePerBlock = config.getDouble(key + ".levels." + level + ".damage_per_block", 0.0);
            double bonusDamage = distance * damagePerBlock;

            int cooldown = config.getInt(key + ".levels." + level + ".cooldown", 0);
            AtheriumEnchants.getInstance().getCooldownManager().setCooldown(shooter, key, cooldown);

            return bonusDamage;
        }

        return 0.0;
    }
}