package ua.etherium.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.HashMap;
import java.util.Map;

public class ItemUtils {

    /**
     * Checks if an item has a specific custom enchantment.
     *
     * @param item The item to check.
     * @param enchant The custom enchantment to look for.
     * @return true if the item has the enchantment, false otherwise.
     */
    public static boolean hasCustomEnchant(ItemStack item, CustomEnchant enchant) {
        return getCustomEnchantLevel(item, enchant) > 0;
    }

    /**
     * Gets the level of a specific custom enchantment on an item.
     *
     * @param item The item to check.
     * @param enchant The custom enchantment.
     * @return The level of the enchantment, or 0 if it's not present.
     */
    public static int getCustomEnchantLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || !item.hasItemMeta() || enchant == null) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AtheriumEnchants.getInstance(), enchant.getKey());
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    /**
     * Gets all custom enchantments present on an item.
     *
     * @param item The item to check.
     * @return A map where the key is the CustomEnchant and the value is its level.
     */
    public static Map<CustomEnchant, Integer> getAllCustomEnchants(ItemStack item) {
        Map<CustomEnchant, Integer> enchants = new HashMap<>();
        if (item == null || !item.hasItemMeta()) {
            return enchants;
        }

        AtheriumEnchants plugin = AtheriumEnchants.getInstance();
        for (CustomEnchant enchant : plugin.getEnchantManager().getAllEnchants()) {
            int level = getCustomEnchantLevel(item, enchant);
            if (level > 0) {
                enchants.put(enchant, level);
            }
        }
        return enchants;
    }
}