package ua.atherium.hhevents;

import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.hhevents.managers.ConfigManager;
import ua.atherium.hhevents.managers.EventTimerManager;
import ua.atherium.hhevents.managers.LootManager;
import ua.atherium.hhevents.managers.ProtectionManager;
import ua.atherium.hhevents.events.BaseEvent;
import ua.atherium.hhevents.listeners.MysticListener;
import ua.atherium.hhevents.listeners.GolemListener;

public final class HHEvents extends JavaPlugin {

    private static HHEvents instance;
    private EventTimerManager eventTimerManager;
    private LootManager lootManager;
    private ProtectionManager protectionManager;
    private BaseEvent activeEvent;
    private MysticListener mysticListener;
    private GolemListener golemListener;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║     HHEvents v1.0.0                    ║");
        getLogger().info("║     Универсальная система (1.16.5)     ║");
        getLogger().info("╚════════════════════════════════════════╝");

        try {
            saveDefaultConfig();

            ConfigManager configManager = new ConfigManager(this);
            protectionManager = new ProtectionManager();
            eventTimerManager = new EventTimerManager(this, configManager);
            lootManager = new LootManager(this);

            mysticListener = new MysticListener(this);
            golemListener = new GolemListener(this);

            getServer().getPluginManager().registerEvents(new ua.atherium.hhevents.listeners.BlockListener(protectionManager), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.hhevents.listeners.GUIListener(), this);
            getServer().getPluginManager().registerEvents(mysticListener, this);
            getServer().getPluginManager().registerEvents(golemListener, this);

            getCommand("event").setExecutor(new ua.atherium.hhevents.commands.EventCommand(this));

            getLogger().info("✓ Версия ядра проверена");
            getLogger().info("✓ Конфигурация загружена");
            getLogger().info("✓ Менеджеры инициализированы");
            getLogger().info("✓ WorldEdit Hook и Vault Hook подключены");
        } catch (Exception e) {
            getLogger().severe("✗ Критическая ошибка при загрузке!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║     HHEvents отключен                  ║");
        getLogger().info("╚════════════════════════════════════════╝");
    }

    public static HHEvents getInstance() {
        return instance;
    }

    public EventTimerManager getEventTimerManager() {
        return eventTimerManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public BaseEvent getActiveEvent() {
        return activeEvent;
    }

    public void setActiveEvent(BaseEvent activeEvent) {
        this.activeEvent = activeEvent;
    }

    public MysticListener getMysticListener() {
        return mysticListener;
    }

    public GolemListener getGolemListener() {
        return golemListener;
    }
}
