package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;

public class VampirismEnchant extends CustomEnchant {

    private final Random random = new Random();

    public VampirismEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    public void applyEffect(Player attacker, int level) {
        if (AtheriumEnchants.getInstance().getCooldownManager().isOnCooldown(attacker, key)) {
            return;
        }

        int chance = config.getInt(key + ".levels." + level + ".chance", 0);
        if (random.nextInt(100) < chance) {
            int durationTicks = config.getInt(key + ".levels." + level + ".duration", 0);
            // The amplifier for potion effects is 0-indexed (e.g., 0 for level I, 1 for level II).
            // Assuming config stores 1 for Regen I, 2 for Regen II, so we subtract 1.
            int amplifier = config.getInt(key + ".levels." + level + ".amplifier", 1) - 1;

            if (durationTicks > 0) {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationTicks, amplifier));

                int cooldown = config.getInt(key + ".levels." + level + ".cooldown", 0);
                AtheriumEnchants.getInstance().getCooldownManager().setCooldown(attacker, key, cooldown);
            }
        }
    }
}