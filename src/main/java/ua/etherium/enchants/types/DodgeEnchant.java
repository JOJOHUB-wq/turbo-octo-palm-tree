package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;

public class DodgeEnchant extends CustomEnchant {

    private final Random random = new Random();

    public DodgeEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    /**
     * Attempts to apply the dodge effect.
     *
     * @param victim The player who might dodge.
     * @param level  The level of the enchantment.
     * @return true if the attack was dodged, false otherwise.
     */
    public boolean tryToDodge(Player victim, int level) {
        if (AtheriumEnchants.getInstance().getCooldownManager().isOnCooldown(victim, key)) {
            return false;
        }

        int chance = config.getInt(key + ".levels." + level + ".chance", 0);
        if (victim.isSneaking()) {
            chance += config.getInt(key + ".levels." + level + ".sneak_bonus", 0);
        }

        if (random.nextInt(100) < chance) {
            // Dodge successful
            int cooldown = config.getInt(key + ".levels." + level + ".cooldown", 0);
            AtheriumEnchants.getInstance().getCooldownManager().setCooldown(victim, key, cooldown);
            // Optionally, play a sound or particle effect here
            // victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5F, 2.0F);
            return true;
        }

        return false;
    }
}