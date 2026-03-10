package ua.atherium.hhevents.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class BaseGUI implements InventoryHolder {

    protected Inventory inventory;

    public BaseGUI(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public abstract void handleClick(InventoryClickEvent event);

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
