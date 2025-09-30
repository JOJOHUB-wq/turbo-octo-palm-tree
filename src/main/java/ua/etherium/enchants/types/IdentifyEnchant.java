package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;

public class IdentifyEnchant extends CustomEnchant {

    private final Random random = new Random();

    public IdentifyEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    public void applyEffect(Player attacker, Player victim, int level) {
        if (AtheriumEnchants.getInstance().getCooldownManager().isOnCooldown(attacker, key)) {
            return;
        }

        int chance = config.getInt(key + ".levels." + level + ".chance", 0);
        if (random.nextInt(100) < chance) {
            int durationTicks = config.getInt(key + ".levels." + level + ".duration", 0);

            if (durationTicks > 0) {
                // Glowing effect has no amplifier, so it's always 0.
                victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationTicks, 0));

                int cooldown = config.getInt(key + ".levels." + level + ".cooldown", 0);
                AtheriumEnchants.getInstance().getCooldownManager().setCooldown(attacker, key, cooldown);
            }
        }
    }
}