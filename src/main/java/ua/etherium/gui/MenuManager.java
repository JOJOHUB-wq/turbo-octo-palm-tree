package ua.etherium.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.etherium.AtheriumEnchants;
import ua.etherium.utils.ColorUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MenuManager {

    private final AtheriumEnchants plugin;

    public MenuManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, String menuName) {
        FileConfiguration menuConfig = plugin.getConfigManager().getMenuConfig(menuName);
        if (menuConfig == null) {
            player.sendMessage(ColorUtils.color("&#FF0000Error: Menu '" + menuName + "' not found!"));
            return;
        }

        String title = ColorUtils.color(menuConfig.getString("title", "Menu"));
        int size = menuConfig.getInt("size", 27);

        CustomMenuHolder holder = new CustomMenuHolder(menuName);
        Inventory menu = Bukkit.createInventory(holder, size, title);

        // Populate menu with items
        if (menuConfig.isConfigurationSection("items")) {
            for (String key : menuConfig.getConfigurationSection("items").getKeys(false)) {
                String path = "items." + key;
                try {
                    Material material = Material.valueOf(menuConfig.getString(path + ".material", "STONE"));
                    int slot = menuConfig.getInt(path + ".slot");
                    String displayName = ColorUtils.color(menuConfig.getString(path + ".display_name", " "));
                    List<String> lore = menuConfig.getStringList(path + ".lore").stream()
                            .map(ColorUtils::color)
                            .collect(Collectors.toList());

                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }

                    menu.setItem(slot, item);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in menu '" + menuName + "' for item '" + key + "'.");
                }
            }
        }

        player.openInventory(menu);
    }

    public List<String> getActions(String menuName, int slot) {
        FileConfiguration menuConfig = plugin.getConfigManager().getMenuConfig(menuName);
        if (menuConfig == null) {
            return null;
        }

        String path = "items";
        if (menuConfig.isConfigurationSection(path)) {
            for (String key : menuConfig.getConfigurationSection(path).getKeys(false)) {
                if (menuConfig.getInt(path + "." + key + ".slot") == slot) {
                    return menuConfig.getStringList(path + "." + key + ".actions");
                }
            }
        }
        return null;
    }

    // Custom InventoryHolder to identify our menus
    public static class CustomMenuHolder implements InventoryHolder {
        private final String menuName;

        public CustomMenuHolder(String menuName) {
            this.menuName = menuName;
        }

        public String getMenuName() {
            return menuName;
        }

        @Override
        public Inventory getInventory() {
            return null; // This is intended, we don't need to return the inventory itself
        }
    }
}