package ua.atherium.hhevents.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.hhevents.HHEvents;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final HHEvents plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(HHEvents plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить config.yml");
        }
    }

    public String getString(String path) {
        return config.getString(path, path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path) {
        return config.getInt(path, 0);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public double getDouble(String path) {
        return config.getDouble(path, 0.0);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }
}
