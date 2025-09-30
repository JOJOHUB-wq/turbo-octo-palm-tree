package ua.etherium.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.etherium.utils.ColorUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MenuItem {

    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final List<String> actions;
    private final int slot;

    public MenuItem(Material material, String displayName, List<String> lore, List<String> actions, int slot) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.actions = actions;
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public List<String> getActions() {
        return actions;
    }

    /**
     * Creates the visual representation of the menu item.
     * @return The ItemStack to be placed in the inventory.
     */
    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtils.color(displayName));
            if (lore != null) {
                meta.setLore(lore.stream().map(ColorUtils::color).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }

        return item;
    }
}