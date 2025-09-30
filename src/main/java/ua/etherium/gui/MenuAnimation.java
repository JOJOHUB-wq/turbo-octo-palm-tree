package ua.etherium.gui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ua.etherium.utils.ColorUtils;

public class MenuAnimation extends BukkitRunnable {

    private final Inventory menu;
    private final ItemStack fillItem;
    private int currentSlot = 0;

    public MenuAnimation(Inventory menu, ConfigurationSection fillItemConfig) {
        this.menu = menu;
        this.fillItem = createFillItem(fillItemConfig);
    }

    @Override
    public void run() {
        if (menu.getViewers().isEmpty()) {
            this.cancel();
            return;
        }

        while (currentSlot < menu.getSize()) {
            if (menu.getItem(currentSlot) == null) {
                menu.setItem(currentSlot, fillItem);
                currentSlot++;
                return;
            }
            currentSlot++;
        }

        this.cancel();
    }

    private ItemStack createFillItem(ConfigurationSection config) {
        if (config == null) {
            return new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        }
        try {
            Material material = Material.valueOf(config.getString("material", "BLACK_STAINED_GLASS_PANE").toUpperCase());
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtils.color(config.getString("display_name", " ")));
                item.setItemMeta(meta);
            }
            return item;
        } catch (IllegalArgumentException e) {
            return new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        }
    }
}