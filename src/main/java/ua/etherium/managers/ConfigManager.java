package ua.etherium.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.etherium.AtheriumEnchants;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final AtheriumEnchants plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration enchantsConfig;
    private final Map<String, FileConfiguration> menuConfigs = new HashMap<>();

    public ConfigManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        // Load main config.yml
        plugin.reloadConfig();
        mainConfig = plugin.getConfig();

        // Load enchants.yml
        File enchantsFile = new File(plugin.getDataFolder(), "enchants.yml");
        if (!enchantsFile.exists()) {
            plugin.saveResource("enchants.yml", false);
        }
        enchantsConfig = YamlConfiguration.loadConfiguration(enchantsFile);

        // Load menu configurations
        menuConfigs.clear();
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (menusFolder.exists() && menusFolder.isDirectory()) {
            File[] menuFiles = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (menuFiles != null) {
                for (File menuFile : menuFiles) {
                    String menuName = menuFile.getName().replace(".yml", "");
                    menuConfigs.put(menuName, YamlConfiguration.loadConfiguration(menuFile));
                }
            }
        }
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getEnchantsConfig() {
        if (enchantsConfig == null) {
             reload();
        }
        return enchantsConfig;
    }

    public FileConfiguration getMenuConfig(String name) {
        return menuConfigs.get(name);
    }
}