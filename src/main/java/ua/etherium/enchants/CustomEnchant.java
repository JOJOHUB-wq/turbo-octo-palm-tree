package ua.etherium.enchants;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.etherium.AtheriumEnchants;
import ua.etherium.utils.ColorUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public abstract class CustomEnchant {

    protected final String key;
    protected final FileConfiguration config;
    protected final String name;
    protected final String rarityDisplay;
    protected final int maxLevel;
    protected final List<String> targetItems;
    protected final List<String> conflicts;

    public CustomEnchant(String key, FileConfiguration config) {
        this.key = key;
        this.config = config;
        this.name = config.getString(key + ".name", "Unnamed Enchant");
        this.rarityDisplay = config.getString(key + ".rarity_display", "");
        this.maxLevel = config.getInt(key + ".max_level", 1);
        this.targetItems = config.getStringList(key + ".target_items");
        this.conflicts = config.getStringList(key + ".conflicts");
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean canApplyTo(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        return targetItems.contains(item.getType().name());
    }

    public ItemStack applyToItem(ItemStack item, int level) {
        if (item == null || level < 1 || level > maxLevel) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(AtheriumEnchants.getInstance(), key);
        container.set(namespacedKey, PersistentDataType.INTEGER, level);

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(0, ColorUtils.color(rarityDisplay + " " + name + " " + toRoman(level)));
        meta.setLore(lore);

        // Add a vanilla enchant to make it glow
        if (!meta.hasEnchants()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEnchantedBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        meta.setDisplayName(ColorUtils.color(rarityDisplay + " " + name));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(AtheriumEnchants.getInstance(), key);
        container.set(namespacedKey, PersistentDataType.INTEGER, level);

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.color(rarityDisplay + " " + name + " " + toRoman(level)));
        lore.add("");
        lore.add(ColorUtils.color("&7" + config.getString(key + ".description")));
        meta.setLore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        book.setItemMeta(meta);
        return book;
    }

    public static int getEnchantLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AtheriumEnchants.getInstance(), enchant.getKey());
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    protected String toRoman(int number) {
        if (number < 1 || number > 10) {
            return String.valueOf(number);
        }
        String[] r = {"X", "IX", "V", "IV", "I"};
        int[] n = {10, 9, 5, 4, 1};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < n.length; i++) {
            while (number >= n[i]) {
                roman.append(r[i]);
                number -= n[i];
            }
        }
        return roman.toString();
    }
}