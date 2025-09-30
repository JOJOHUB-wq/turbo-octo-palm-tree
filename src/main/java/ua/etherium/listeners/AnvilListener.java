package ua.etherium.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Map;

public class AnvilListener implements Listener {

    private final AtheriumEnchants plugin;

    public AnvilListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getFirstItem();
        ItemStack second = inventory.getSecondItem();

        if (first == null || second == null) {
            return;
        }

        // Logic for applying a custom book to an item
        if (second.getType() == Material.ENCHANTED_BOOK && second.hasItemMeta()) {
            CustomEnchant bookEnchant = getCustomEnchantFromBook(second);
            if (bookEnchant == null) return;

            int bookLevel = CustomEnchant.getEnchantLevel(second, bookEnchant);

            // Check if the enchant can be applied to the item
            if (!bookEnchant.canApplyTo(first)) {
                return;
            }

            ItemStack result = first.clone();
            int currentLevel = CustomEnchant.getEnchantLevel(result, bookEnchant);
            int newLevel;

            if (currentLevel == bookLevel) {
                newLevel = currentLevel + 1;
            } else if (currentLevel == 0) {
                newLevel = bookLevel;
            } else {
                return; // Can't combine different levels (for now)
            }

            if (newLevel > bookEnchant.getMaxLevel()) {
                return; // Exceeds max level
            }

            // TODO: Check for conflicting enchantments

            bookEnchant.applyToItem(result, newLevel);
            event.setResult(result);
            // This is a simplified cost, a real implementation would be more complex
            inventory.setRepairCost(newLevel * 5);
        }
    }

    private CustomEnchant getCustomEnchantFromBook(ItemStack book) {
        if (book == null || book.getType() != Material.ENCHANTED_BOOK || !book.hasItemMeta()) {
            return null;
        }

        for(CustomEnchant enchant : plugin.getEnchantManager().getAllEnchants()){
            if(CustomEnchant.getEnchantLevel(book, enchant) > 0){
                return enchant;
            }
        }
        return null;
    }
}