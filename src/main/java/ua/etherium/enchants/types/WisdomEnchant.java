package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class WisdomEnchant extends CustomEnchant {

    private final Random random = new Random();

    public WisdomEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    /**
     * Calculates the bonus experience to award.
     *
     * @param level The level of the enchantment.
     * @return The amount of bonus experience, or 0 if the effect doesn't trigger.
     */
    public int getBonusExperience(int level) {
        int chance = config.getInt(key + ".levels." + level + ".chance", 0);

        if (random.nextInt(100) < chance) {
            int minExp = config.getInt(key + ".levels." + level + ".min_exp", 0);
            int maxExp = config.getInt(key + ".levels." + level + ".max_exp", 0);

            if (maxExp > minExp) {
                return ThreadLocalRandom.current().nextInt(minExp, maxExp + 1);
            }
            return minExp;
        }

        return 0;
    }
}