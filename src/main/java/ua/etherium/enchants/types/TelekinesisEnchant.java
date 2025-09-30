package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.etherium.enchants.CustomEnchant;

import java.util.Collection;
import java.util.HashMap;

public class TelekinesisEnchant extends CustomEnchant {

    public TelekinesisEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    /**
     * Adds a collection of items directly to the player's inventory.
     * Items that don't fit are returned in a map.
     *
     * @param player The player whose inventory to add the items to.
     * @param drops  The collection of items to add.
     * @return A HashMap containing items that did not fit in the inventory.
     */
    public HashMap<Integer, ItemStack> addItemsToInventory(Player player, Collection<ItemStack> drops) {
        return player.getInventory().addItem(drops.toArray(new ItemStack[0]));
    }
}