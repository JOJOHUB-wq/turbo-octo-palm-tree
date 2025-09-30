package ua.etherium.enchants.types;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import ua.etherium.enchants.CustomEnchant;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class InfernalTouchEnchant extends CustomEnchant {

    private final Map<Material, Material> smeltMap = new HashMap<>();

    public InfernalTouchEnchant(String key, FileConfiguration config) {
        super(key, config);
        loadSmeltMap();
    }

    private void loadSmeltMap() {
        String path = key + ".smelt_map";
        if (config.isConfigurationSection(path)) {
            for (String from : config.getConfigurationSection(path).getKeys(false)) {
                String to = config.getString(path + "." + from);
                try {
                    Material fromMat = Material.valueOf(from.toUpperCase());
                    Material toMat = Material.valueOf(to.toUpperCase());
                    smeltMap.put(fromMat, toMat);
                } catch (IllegalArgumentException e) {
                    // Log error or ignore
                }
            }
        }
    }

    public Collection<ItemStack> smeltDrops(Collection<ItemStack> drops) {
        return drops.stream()
                .map(this::smeltDrop)
                .collect(Collectors.toList());
    }

    private ItemStack smeltDrop(ItemStack drop) {
        Material smeltedType = smeltMap.get(drop.getType());
        if (smeltedType != null) {
            drop.setType(smeltedType);
        }
        return drop;
    }
}