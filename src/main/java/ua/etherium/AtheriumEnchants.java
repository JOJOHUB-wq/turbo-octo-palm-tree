package ua.etherium;

import org.bukkit.plugin.java.JavaPlugin;
import ua.etherium.commands.EnchantCommand;
import ua.etherium.commands.EnchantTabCompleter;
import ua.etherium.listeners.*;
import ua.etherium.managers.ConfigManager;
import ua.etherium.managers.CooldownManager;
import ua.etherium.managers.EnchantManager;

public class AtheriumEnchants extends JavaPlugin {

    private static AtheriumEnchants instance;
    private EnchantManager enchantManager;
    private ConfigManager configManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("enchants.yml", false);
        saveResource("menus/main.yml", false);
        saveResource("menus/weapon.yml", false);
        saveResource("menus/tools.yml", false);
        saveResource("menus/armor.yml", false);

        configManager = new ConfigManager(this);
        cooldownManager = new CooldownManager();
        enchantManager = new EnchantManager(this);

        enchantManager.loadEnchants();

        getCommand("atheriumenchants").setExecutor(new EnchantCommand(this));
        getCommand("atheriumenchants").setTabCompleter(new EnchantTabCompleter(this));

        getServer().getPluginManager().registerEvents(new BlockBreakHandler(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageHandler(this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantTableListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);

        getLogger().info("AtheriumEnchants загружен успешно!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AtheriumEnchants выгружен!");
    }

    public static AtheriumEnchants getInstance() {
        return instance;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}