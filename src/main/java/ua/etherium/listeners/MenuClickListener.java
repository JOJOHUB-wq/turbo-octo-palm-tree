package ua.etherium.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.etherium.AtheriumEnchants;
import ua.etherium.gui.MenuManager;

import java.util.List;

public class MenuClickListener implements Listener {

    private final AtheriumEnchants plugin;
    private final MenuManager menuManager;

    public MenuClickListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.menuManager = new MenuManager(plugin); // Re-use MenuManager logic
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory is one of our custom menus
        if (event.getInventory().getHolder() instanceof MenuManager.CustomMenuHolder) {
            // Prevent players from taking items from the menu
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            // Get the actions associated with the clicked item
            String menuName = ((MenuManager.CustomMenuHolder) event.getInventory().getHolder()).getMenuName();
            List<String> actions = menuManager.getActions(menuName, event.getSlot());

            if (actions != null) {
                executeActions(player, actions);
            }
        }
    }

    private void executeActions(Player player, List<String> actions) {
        for (String action : actions) {
            String[] parts = action.split(":", 2);
            String actionType = parts[0].toLowerCase();
            String actionValue = parts.length > 1 ? parts[1] : "";

            switch (actionType) {
                case "open_menu":
                    menuManager.openMenu(player, actionValue);
                    break;
                case "close":
                    player.closeInventory();
                    break;
                case "command":
                    player.performCommand(actionValue);
                    break;
                case "console_command":
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), actionValue);
                    break;
                // Add more action types here as needed
            }
        }
    }
}