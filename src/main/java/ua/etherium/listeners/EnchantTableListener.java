package ua.etherium.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;

public class EnchantTableListener implements Listener {

    private final AtheriumEnchants plugin;
    private final Random random = new Random();

    public EnchantTableListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        int cost = event.getExpLevelCost(); // Use the enchanting level as a factor

        for (CustomEnchant enchant : plugin.getEnchantManager().getAllEnchants()) {
            if (enchant.canApplyTo(item)) {
                // Check if the enchantment is enabled for the table
                boolean tableEnabled = plugin.getConfigManager().getEnchantsConfig().getBoolean(enchant.getKey() + ".table.enabled", false);
                if (!tableEnabled) {
                    continue;
                }

                // TODO: Check for conflicts with existing vanilla and custom enchants

                // Calculate chance
                double chance = plugin.getConfigManager().getEnchantsConfig().getDouble(enchant.getKey() + ".table.chance", 0.0);
                // Slightly increase chance with higher level enchanting
                double finalChance = chance + (cost / 2.0);

                if (random.nextDouble() * 100 < finalChance) {
                    // Determine level (simple logic for now, could be more complex)
                    int level = 1;
                    if (cost > 20 && enchant.getMaxLevel() > 1) {
                         level = random.nextInt(enchant.getMaxLevel()) + 1;
                    }

                    enchant.applyToItem(item, level);
                }
            }
        }
    }
}