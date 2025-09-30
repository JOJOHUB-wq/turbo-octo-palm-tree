package ua.etherium.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.managers.EnchantManager;
import ua.etherium.utils.ItemUtils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantTableListener implements Listener {

    private final AtheriumEnchants plugin;
    private final EnchantManager enchantManager;

    public EnchantTableListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantManager();
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        int cost = event.getExpLevelCost();
        Random random = ThreadLocalRandom.current();

        for (CustomEnchant enchant : enchantManager.getAllEnchants()) {
            if (!enchant.canApplyTo(item)) {
                continue;
            }

            boolean tableEnabled = plugin.getConfigManager().getEnchantsConfig().getBoolean(enchant.getKey() + ".table.enabled", false);
            if (!tableEnabled) {
                continue;
            }

            if (hasConflicts(item, enchant)) {
                continue;
            }

            double chance = plugin.getConfigManager().getEnchantsConfig().getDouble(enchant.getKey() + ".table.chance", 0.0);
            double finalChance = chance + (cost / 2.0);

            if (random.nextDouble() * 100 < finalChance) {
                int level = 1;
                if (enchant.getMaxLevel() > 1) {
                    if (cost > 25) {
                        level = random.nextInt(enchant.getMaxLevel()) + 1;
                    } else if (cost > 15) {
                        level = random.nextInt(Math.min(2, enchant.getMaxLevel())) + 1;
                    }
                }
                enchant.applyToItem(item, level);
            }
        }
    }

    private boolean hasConflicts(ItemStack item, CustomEnchant newEnchant) {
        for (String conflictKey : newEnchant.getConflicts()) {
            Enchantment vanillaConflict = Enchantment.getByKey(NamespacedKey.minecraft(conflictKey.toLowerCase()));
            if (vanillaConflict != null && item.getEnchantmentLevel(vanillaConflict) > 0) {
                return true;
            }

            CustomEnchant customConflict = enchantManager.getEnchant(conflictKey);
            if (customConflict != null && ItemUtils.hasCustomEnchant(item, customConflict)) {
                return true;
            }
        }

        for (CustomEnchant existingEnchant : ItemUtils.getAllCustomEnchants(item).keySet()) {
            if (existingEnchant.getConflicts().contains(newEnchant.getKey())) {
                return true;
            }
        }
        return false;
    }
}