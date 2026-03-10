package ua.atherium.hhevents.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ua.atherium.hhevents.HHEvents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class LootManager {

    private final HHEvents plugin;
    private FileConfiguration lootConfig;
    private File lootFile;
    private final Map<String, Map<String, List<LootItem>>> lootMap = new HashMap<>();

    public LootManager(HHEvents plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        lootFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!lootFile.exists()) {
            plugin.saveResource("loot.yml", false);
        }
        lootConfig = YamlConfiguration.loadConfiguration(lootFile);
        reloadLoot();
    }

    public void reloadLoot() {
        lootMap.clear();
        if (lootConfig.getConfigurationSection("loot") == null) return;

        for (String eventType : lootConfig.getConfigurationSection("loot").getKeys(false)) {
            Map<String, List<LootItem>> rarityMap = new HashMap<>();
            for (String rarity : lootConfig.getConfigurationSection("loot." + eventType).getKeys(false)) {
                List<LootItem> items = new ArrayList<>();
                for (String itemId : lootConfig.getConfigurationSection("loot." + eventType + "." + rarity).getKeys(false)) {
                    ItemStack itemStack = lootConfig.getItemStack("loot." + eventType + "." + rarity + "." + itemId + ".item");
                    int weight = lootConfig.getInt("loot." + eventType + "." + rarity + "." + itemId + ".weight", 10);
                    if (itemStack != null) {
                        items.add(new LootItem(itemId, itemStack, weight));
                    }
                }
                rarityMap.put(rarity, items);
            }
            lootMap.put(eventType, rarityMap);
        }
    }

    public void saveConfig() {
        try {
            lootConfig.save(lootFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить loot.yml");
        }
    }

    public ItemStack getRandomLoot(String eventType, String rarity) {
        if (!lootMap.containsKey(eventType) || !lootMap.get(eventType).containsKey(rarity)) {
            return null;
        }

        List<LootItem> items = lootMap.get(eventType).get(rarity);
        if (items == null || items.isEmpty()) return null;

        int totalWeight = items.stream().mapToInt(LootItem::getWeight).sum();
        if (totalWeight <= 0) return null;

        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;

        for (LootItem item : items) {
            currentWeight += item.getWeight();
            if (random < currentWeight) {
                return item.getItem().clone();
            }
        }

        return items.get(0).getItem().clone();
    }

    public void addLootItem(String eventType, String rarity, ItemStack item, int weight) {
        String id = String.valueOf(System.currentTimeMillis());
        lootConfig.set("loot." + eventType + "." + rarity + "." + id + ".item", item);
        lootConfig.set("loot." + eventType + "." + rarity + "." + id + ".weight", weight);
        saveConfig();
        reloadLoot();
    }

    public void removeLootItem(String eventType, String rarity, String id) {
        lootConfig.set("loot." + eventType + "." + rarity + "." + id, null);
        saveConfig();
        reloadLoot();
    }

    public void updateLootItemWeight(String eventType, String rarity, String id, int weight) {
        lootConfig.set("loot." + eventType + "." + rarity + "." + id + ".weight", weight);
        saveConfig();
        reloadLoot();
    }

    public List<LootItem> getLootItems(String eventType, String rarity) {
        if (lootMap.containsKey(eventType) && lootMap.get(eventType).containsKey(rarity)) {
            return lootMap.get(eventType).get(rarity);
        }
        return new ArrayList<>();
    }

    public static class LootItem {
        private final String id;
        private final ItemStack item;
        private final int weight;

        public LootItem(String id, ItemStack item, int weight) {
            this.id = id;
            this.item = item;
            this.weight = weight;
        }

        public String getId() {
            return id;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getWeight() {
            return weight;
        }
    }
}
