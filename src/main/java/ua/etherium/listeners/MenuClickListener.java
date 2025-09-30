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
        this.menuManager = new MenuManager(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuManager.CustomMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        String menuName = ((MenuManager.CustomMenuHolder) event.getInventory().getHolder()).getMenuName();
        List<String> actions = menuManager.getActions(menuName, event.getSlot());

        if (actions != null && !actions.isEmpty()) {
            executeActions(player, actions);
        }
    }

    private void executeActions(Player player, List<String> actions) {
        for (String action : actions) {
            String[] parts = action.split(":", 2);
            if (parts.length == 0) continue;

            String type = parts[0].toLowerCase();
            String value = parts.length > 1 ? parts[1] : "";

            switch (type) {
                case "open_menu":
                    menuManager.openMenu(player, value.trim());
                    break;
                case "close":
                    player.closeInventory();
                    break;
                case "command":
                    player.performCommand(value.trim());
                    break;
                case "console_command":
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), value.trim().replace("%player%", player.getName()));
                    break;
            }
        }
    }
}