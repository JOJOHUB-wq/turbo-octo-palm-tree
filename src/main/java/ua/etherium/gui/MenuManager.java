package ua.etherium.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.utils.ColorUtils;

import java.util.ArrayList;
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
            player.sendMessage(ColorUtils.color("&cМеню '" + menuName + "' не найдено!"));
            return;
        }

        String title = ColorUtils.color(menuConfig.getString("title", "Menu"));
        int size = menuConfig.getInt("size", 27);

        CustomMenuHolder holder = new CustomMenuHolder(menuName);
        Inventory menu = Bukkit.createInventory(holder, size, title);

        populateStaticItems(menu, menuConfig);
        populateDynamicEnchants(menu, menuName);

        if (menuConfig.isConfigurationSection("fill_item")) {
            new MenuAnimation(menu, menuConfig.getConfigurationSection("fill_item")).runTaskTimer(plugin, 0L, 2L);
        }


        player.openInventory(menu);
    }

    private void populateStaticItems(Inventory menu, FileConfiguration menuConfig) {
        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
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
                plugin.getLogger().warning("Неверный материал в меню '" + menuConfig.getName() + "' для предмета '" + key + "'.");
            }
        }
    }

    private void populateDynamicEnchants(Inventory menu, String menuName) {
        List<CustomEnchant> enchantsToShow = new ArrayList<>();
        for (CustomEnchant enchant : plugin.getEnchantManager().getAllEnchants()) {
            boolean match = false;
            switch (menuName.toLowerCase()) {
                case "weapon":
                    if (enchant.canApplyTo(new ItemStack(Material.DIAMOND_SWORD)) && !enchant.canApplyTo(new ItemStack(Material.BOW))) match = true;
                    break;
                case "tools":
                    if (enchant.canApplyTo(new ItemStack(Material.DIAMOND_PICKAXE))) match = true;
                    break;
                case "armor":
                    if (enchant.canApplyTo(new ItemStack(Material.DIAMOND_CHESTPLATE))) match = true;
                    break;
                case "bow":
                    if (enchant.canApplyTo(new ItemStack(Material.BOW))) match = true;
                    break;
            }
            if (match) {
                enchantsToShow.add(enchant);
            }
        }

        int slot = 0;
        for (CustomEnchant enchant : enchantsToShow) {
            if (slot >= menu.getSize()) break;
            if (menu.getItem(slot) == null) {
                menu.setItem(slot, enchant.createEnchantedBook(1));
            }
            slot++;
        }
    }


    public List<String> getActions(String menuName, int slot) {
        FileConfiguration menuConfig = plugin.getConfigManager().getMenuConfig(menuName);
        if (menuConfig == null) return null;

        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                if (itemsSection.getInt(key + ".slot") == slot) {
                    return itemsSection.getStringList(key + ".actions");
                }
            }
        }
        return null;
    }

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
            return null;
        }
    }
}