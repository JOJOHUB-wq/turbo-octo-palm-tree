package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;

public class ToxicEnchant extends CustomEnchant {

    private final Random random = new Random();

    public ToxicEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    public void applyEffect(Player attacker, Player victim, int level) {
        if (AtheriumEnchants.getInstance().getCooldownManager().isOnCooldown(attacker, key)) {
            return;
        }

        int chance = config.getInt(key + ".levels." + level + ".chance", 0);
        if (random.nextInt(100) < chance) {
            int durationTicks = config.getInt(key + ".levels." + level + ".duration", 0);
            // Amplifier is 0-indexed. Config stores 1 for Poison I, so we subtract 1.
            int amplifier = config.getInt(key + ".levels." + level + ".amplifier", 1) - 1;

            if (durationTicks > 0) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, durationTicks, amplifier));

                int cooldown = config.getInt(key + ".levels." + level + ".cooldown", 0);
                AtheriumEnchants.getInstance().getCooldownManager().setCooldown(attacker, key, cooldown);
            }
        }
    }
}