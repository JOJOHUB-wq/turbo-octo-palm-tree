package ua.etherium.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import ua.etherium.AtheriumEnchants;

public class MenuAnimation extends BukkitRunnable {

    private final Inventory menu;
    private int frame = 0;

    public MenuAnimation(Inventory menu) {
        this.menu = menu;
    }

    @Override
    public void run() {
        // This is a placeholder for future animation logic.
        // For example, you could cycle the color of a specific item's title
        // or change the material of decorative glass panes.

        // Example:
        // ItemStack item = menu.getItem(0);
        // if (item != null) {
        //     ItemMeta meta = item.getItemMeta();
        //     meta.setDisplayName(ColorUtils.color("Frame: " + frame));
        //     item.setItemMeta(meta);
        // }
        // frame++;

        // If the menu is no longer being viewed, cancel the animation task.
        if (menu.getViewers().isEmpty()) {
            this.cancel();
        }
    }

    public void start(AtheriumEnchants plugin, long delay, long period) {
        this.runTaskTimer(plugin, delay, period);
    }
}