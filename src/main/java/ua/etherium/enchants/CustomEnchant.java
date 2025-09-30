package ua.etherium.enchants;

import com.google.common.collect.Sets;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CustomEnchant {

    protected final String key;
    protected final FileConfiguration config;
    protected final String name;
    protected final String description;
    protected final String rarityDisplay;
    protected final int maxLevel;
    protected final List<String> targetItemKeywords;
    protected final List<String> conflicts;
    private final Set<Material> applicableItems;

    public CustomEnchant(String key, FileConfiguration config) {
        this.key = key;
        this.config = config;
        this.name = config.getString(key + ".name", "Unnamed Enchant");
        this.description = config.getString(key + ".description", "");
        this.rarityDisplay = config.getString(key + ".rarity_display", "");
        this.maxLevel = config.getInt(key + ".max_level", 1);
        this.targetItemKeywords = config.getStringList(key + ".target_items");
        this.conflicts = config.getStringList(key + ".conflicts");
        this.applicableItems = parseTargetItems(targetItemKeywords);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public boolean canApplyTo(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        return applicableItems.contains(item.getType());
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
        lore.removeIf(line -> ColorUtils.strip(line).startsWith(ColorUtils.strip(rarityDisplay + " " + name)));
        lore.add(0, ColorUtils.color(getDisplayName(level)));
        meta.setLore(lore);

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

        NamespacedKey namespacedKey = new NamespacedKey(AtheriumEnchants.getInstance(), key);
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, level);

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.color(getDisplayName(level)));
        lore.add("");
        lore.add(ColorUtils.color("&7" + this.description));
        meta.setLore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        book.setItemMeta(meta);
        return book;
    }

    public static int getEnchantLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || !item.hasItemMeta() || enchant == null) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AtheriumEnchants.getInstance(), enchant.getKey());
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    private String getDisplayName(int level) {
        if (level <= 1 && maxLevel == 1) {
            return rarityDisplay + " " + name;
        }
        return rarityDisplay + " " + name + " " + toRoman(level);
    }

    private String toRoman(int number) {
        if (number < 1) return "";
        if (number >= 4000) return String.valueOf(number);
        final String[] r = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final int[] n = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < n.length; i++) {
            while (number >= n[i]) {
                roman.append(r[i]);
                number -= n[i];
            }
        }
        return roman.toString();
    }

    private Set<Material> parseTargetItems(List<String> keywords) {
        Set<Material> materials = Sets.newHashSet();
        for (String keyword : keywords) {
            String upperKeyword = keyword.toUpperCase();
            if (upperKeyword.startsWith("ALL_")) {
                String type = upperKeyword.replace("ALL_", "");
                materials.addAll(getMaterialsByType(type));
            } else {
                try {
                    materials.add(Material.valueOf(upperKeyword));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return materials;
    }

    private Set<Material> getMaterialsByType(String type) {
        Stream<Material> materialStream = Stream.of(Material.values());
        String cleanType = type.endsWith("S") ? type.substring(0, type.length() - 1) : type;

        switch (cleanType) {
            case "SWORD":
            case "PICKAXE":
            case "AXE":
            case "SHOVEL":
            case "HOE":
                return materialStream.filter(m -> m.name().endsWith("_" + cleanType)).collect(Collectors.toSet());
            case "ARMOR":
                return materialStream.filter(m -> {
                    String name = m.name();
                    return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
                }).collect(Collectors.toSet());
            case "HELMET":
            case "CHESTPLATE":
            case "LEGGINGS":
            case "BOOTS":
                 return materialStream.filter(m -> m.name().endsWith("_" + cleanType)).collect(Collectors.toSet());
            default:
                return Sets.newHashSet();
        }
    }
}