package ua.etherium.managers;

import org.bukkit.configuration.file.FileConfiguration;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.enchants.types.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnchantManager {

    private final AtheriumEnchants plugin;
    private final Map<String, CustomEnchant> enchants = new HashMap<>();

    public EnchantManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    public void loadEnchants() {
        enchants.clear();
        FileConfiguration config = plugin.getConfigManager().getEnchantsConfig();
        Set<String> enchantKeys = config.getKeys(false);

        for (String key : enchantKeys) {
            if (!config.getBoolean(key + ".enabled", false)) {
                continue;
            }

            CustomEnchant enchant = createEnchantment(key, config);
            if (enchant != null) {
                enchants.put(key.toLowerCase(), enchant);
            } else {
                plugin.getLogger().warning("Unknown enchantment type in enchants.yml: " + key);
            }
        }
        plugin.getLogger().info("Loaded " + enchants.size() + " custom enchantments.");
    }

    private CustomEnchant createEnchantment(String key, FileConfiguration config) {
        switch (key.toLowerCase()) {
            case "oxidation":
                return new OxidationEnchant(key, config);
            case "vampirism":
                return new VampirismEnchant(key, config);
            case "toxic":
                return new ToxicEnchant(key, config);
            case "identify":
                return new IdentifyEnchant(key, config);
            case "blast_mining":
                return new BlastMiningEnchant(key, config);
            case "timber":
                return new TimberEnchant(key, config);
            case "infernal_touch":
                return new InfernalTouchEnchant(key, config);
            case "telekinesis":
                return new TelekinesisEnchant(key, config);
            case "wisdom":
                return new WisdomEnchant(key, config);
            case "dodge":
                return new DodgeEnchant(key, config);
            case "springs":
                return new SpringsEnchant(key, config);
            case "poison_thorns":
                return new PoisonThornsEnchant(key, config);
            case "sniper":
                return new SniperEnchant(key, config);
            case "explosive":
                return new ExplosiveEnchant(key, config);
            default:
                return null;
        }
    }

    public CustomEnchant getEnchant(String key) {
        return enchants.get(key.toLowerCase());
    }

    public Collection<CustomEnchant> getAllEnchants() {
        return enchants.values();
    }
}