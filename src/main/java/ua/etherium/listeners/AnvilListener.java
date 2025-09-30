package ua.etherium.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.managers.EnchantManager;
import ua.etherium.utils.ItemUtils;

import java.util.Map;

public class AnvilListener implements Listener {

    private final AtheriumEnchants plugin;
    private final EnchantManager enchantManager;

    public AnvilListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantManager();
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getFirstItem();
        ItemStack second = inventory.getSecondItem();

        if (first == null || second == null || first.getType().isAir()) {
            return;
        }

        if (second.getType() == Material.ENCHANTED_BOOK && second.hasItemMeta()) {
            handleBookApplication(event, first, second);
        } else {
            handleItemCombination(event, first, second);
        }
    }

    private void handleBookApplication(PrepareAnvilEvent event, ItemStack targetItem, ItemStack book) {
        Map<CustomEnchant, Integer> bookEnchants = ItemUtils.getAllCustomEnchants(book);
        if (bookEnchants.isEmpty()) return;

        CustomEnchant bookEnchant = bookEnchants.keySet().iterator().next();
        int bookLevel = bookEnchants.get(bookEnchant);

        if (!bookEnchant.canApplyTo(targetItem)) return;
        if (hasConflicts(targetItem, bookEnchant)) return;

        int currentLevel = ItemUtils.getCustomEnchantLevel(targetItem, bookEnchant);
        int finalLevel;

        if (currentLevel > 0) {
            if (currentLevel != bookLevel) return;
            finalLevel = currentLevel + 1;
        } else {
            finalLevel = bookLevel;
        }

        if (finalLevel > bookEnchant.getMaxLevel()) return;

        ItemStack result = targetItem.clone();
        bookEnchant.applyToItem(result, finalLevel);

        event.setResult(result);
        event.getInventory().setRepairCost(calculateCost(bookEnchant, finalLevel));
    }

    private void handleItemCombination(PrepareAnvilEvent event, ItemStack first, ItemStack second) {
        if (first.getType() != second.getType()) return;

        Map<CustomEnchant, Integer> firstEnchants = ItemUtils.getAllCustomEnchants(first);
        Map<CustomEnchant, Integer> secondEnchants = ItemUtils.getAllCustomEnchants(second);

        if (firstEnchants.isEmpty() && secondEnchants.isEmpty()) return;

        ItemStack result = first.clone();
        int cost = 0;
        boolean changed = false;

        for (Map.Entry<CustomEnchant, Integer> entry : secondEnchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int secondLevel = entry.getValue();
            int firstLevel = ItemUtils.getCustomEnchantLevel(result, enchant);

            if (hasConflicts(result, enchant)) continue;

            if (firstLevel == secondLevel && firstLevel < enchant.getMaxLevel()) {
                enchant.applyToItem(result, firstLevel + 1);
                cost += calculateCost(enchant, firstLevel + 1);
                changed = true;
            } else if (firstLevel == 0) {
                enchant.applyToItem(result, secondLevel);
                cost += calculateCost(enchant, secondLevel);
                changed = true;
            }
        }

        if (changed) {
            event.setResult(result);
            event.getInventory().setRepairCost(event.getInventory().getRepairCost() + cost);
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
        return false;
    }

    private int calculateCost(CustomEnchant enchant, int level) {
        int baseCost = 5;
        return baseCost * level;
    }
}