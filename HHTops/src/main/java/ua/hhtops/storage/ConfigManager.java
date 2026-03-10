package ua.hhtops.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import ua.hhtops.HHTopsPlugin;
import ua.hhtops.top.TopCategory;

public final class ConfigManager {

    private final HHTopsPlugin plugin;

    public ConfigManager(HHTopsPlugin plugin) {
        this.plugin = plugin;
    }

    public long getUpdateIntervalSeconds() {
        return Math.max(1L, plugin.getConfig().getLong("update-interval-seconds", 300L));
    }

    public int getTopSize() {
        return Math.max(1, plugin.getConfig().getInt("top-size", 10));
    }

    public double getLineSpacing() {
        return plugin.getConfig().getDouble("line-spacing", 0.28D);
    }

    public List<String> getTitleLines(TopCategory category) {
        List<String> lines = plugin.getConfig().getStringList(getCategoryPath(category) + ".title");
        if (lines == null || lines.isEmpty()) {
            return Collections.singletonList(colorize(category.getDisplayName()));
        }
        List<String> colored = new ArrayList<String>();
        for (String line : lines) {
            colored.add(colorize(line));
        }
        return colored;
    }

    public String getLineFormat(TopCategory category) {
        return plugin.getConfig().getString(getCategoryPath(category) + ".line-format", "&e{place}. &f{player} &7- &6{value}");
    }

    public String getEmptyFormat(TopCategory category) {
        return plugin.getConfig().getString(getCategoryPath(category) + ".empty-format", "&e{place}. &7Нет данных");
    }

    public Location getHologramLocation(TopCategory category) {
        FileConfiguration config = plugin.getConfig();
        String basePath = getCategoryPath(category) + ".location";
        String worldName = config.getString(basePath + ".world");
        if (worldName == null || worldName.trim().isEmpty()) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        double x = config.getDouble(basePath + ".x");
        double y = config.getDouble(basePath + ".y");
        double z = config.getDouble(basePath + ".z");
        return new Location(world, x, y, z);
    }

    public void setHologramLocation(TopCategory category, Location location) {
        String basePath = getCategoryPath(category) + ".location";
        plugin.getConfig().set(basePath + ".world", location.getWorld() == null ? null : location.getWorld().getName());
        plugin.getConfig().set(basePath + ".x", location.getX());
        plugin.getConfig().set(basePath + ".y", location.getY());
        plugin.getConfig().set(basePath + ".z", location.getZ());
        plugin.saveConfig();
    }

    public String getMessage(String key) {
        return colorize(plugin.getConfig().getString("messages." + key, "&cСообщение не найдено: " + key));
    }

    public String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    private String getCategoryPath(TopCategory category) {
        return "holograms." + category.getArgument();
    }
}
