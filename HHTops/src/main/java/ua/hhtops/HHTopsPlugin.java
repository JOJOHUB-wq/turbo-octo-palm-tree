package ua.hhtops;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ua.hhtops.command.HHTopsCommand;
import ua.hhtops.command.HHTopsTabCompleter;
import ua.hhtops.hologram.HologramManager;
import ua.hhtops.storage.ConfigManager;
import ua.hhtops.storage.StatisticsStorageManager;
import ua.hhtops.top.TopManager;
import ua.hhtops.vault.EconomyManager;

public final class HHTopsPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private StatisticsStorageManager statisticsStorageManager;
    private EconomyManager economyManager;
    private HologramManager hologramManager;
    private TopManager topManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        statisticsStorageManager = new StatisticsStorageManager(this);
        economyManager = new EconomyManager(this);
        economyManager.setup();
        if (!economyManager.isAvailable()) {
            getLogger().warning("Экономика через Vault не найдена. Топ денег будет пустым.");
        }
        hologramManager = new HologramManager(this, configManager);
        topManager = new TopManager(this, configManager, statisticsStorageManager, economyManager, hologramManager);

        PluginCommand pluginCommand = getCommand("hhtops");
        if (pluginCommand == null) {
            getLogger().severe("Команда hhtops не объявлена в plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        HHTopsCommand command = new HHTopsCommand(configManager, topManager);
        HHTopsTabCompleter tabCompleter = new HHTopsTabCompleter();
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(tabCompleter);

        hologramManager.cleanupAll();
        topManager.refreshAll();
        topManager.startUpdater();
        getLogger().info("Плагин HHTops включен.");
    }

    @Override
    public void onDisable() {
        if (topManager != null) {
            topManager.stopUpdater();
        }
        if (hologramManager != null) {
            hologramManager.cleanupAll();
        }
        getLogger().info("Плагин HHTops выключен.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StatisticsStorageManager getStatisticsStorageManager() {
        return statisticsStorageManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public TopManager getTopManager() {
        return topManager;
    }
}
