package ua.hhtops.top;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import ua.hhtops.HHTopsPlugin;
import ua.hhtops.hologram.HologramManager;
import ua.hhtops.storage.ConfigManager;
import ua.hhtops.storage.StatisticsStorageManager;
import ua.hhtops.vault.EconomyManager;

public final class TopManager {

    private static final Comparator<TopEntry> TOP_ENTRY_COMPARATOR = Comparator.comparingDouble(TopEntry::getSortableValue).reversed();

    private final HHTopsPlugin plugin;
    private final ConfigManager configManager;
    private final StatisticsStorageManager statisticsStorageManager;
    private final EconomyManager economyManager;
    private final HologramManager hologramManager;
    private final DecimalFormat wholeNumberFormat;
    private final DecimalFormat moneyFormat;
    private BukkitTask updateTask;

    public TopManager(HHTopsPlugin plugin, ConfigManager configManager, StatisticsStorageManager statisticsStorageManager, EconomyManager economyManager, HologramManager hologramManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.statisticsStorageManager = statisticsStorageManager;
        this.economyManager = economyManager;
        this.hologramManager = hologramManager;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        this.wholeNumberFormat = new DecimalFormat("#,##0", symbols);
        this.moneyFormat = new DecimalFormat("#,##0.##", symbols);
    }

    public void startUpdater() {
        stopUpdater();
        long intervalTicks = configManager.getUpdateIntervalSeconds() * 20L;
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                refreshAll();
            }
        }, intervalTicks, intervalTicks);
    }

    public void stopUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public void refreshAll() {
        for (TopCategory category : TopCategory.values()) {
            refresh(category);
        }
    }

    public void refresh(TopCategory category) {
        hologramManager.updateHologram(category, buildLines(category));
    }

    public void moveHologram(TopCategory category, Location location) {
        configManager.setHologramLocation(category, location);
        refresh(category);
    }

    private List<String> buildLines(TopCategory category) {
        List<String> titleLines = configManager.getTitleLines(category);
        int topSize = configManager.getTopSize();
        List<String> lines = new ArrayList<String>(titleLines.size() + topSize);
        lines.addAll(titleLines);
        List<TopEntry> entries = getTopEntries(category);
        for (int place = 1; place <= topSize; place++) {
            String rawLine;
            if (place <= entries.size()) {
                TopEntry entry = entries.get(place - 1);
                rawLine = applyPlaceholders(configManager.getLineFormat(category), place, entry.getPlayerName(), entry.getDisplayValue());
            } else {
                rawLine = applyPlaceholders(configManager.getEmptyFormat(category), place, "Нет данных", "Нет данных");
            }
            lines.add(configManager.colorize(rawLine));
        }
        return lines;
    }

    private List<TopEntry> getTopEntries(TopCategory category) {
        List<TopEntry> entries;
        if (category == TopCategory.MONEY) {
            entries = getMoneyEntries();
        } else {
            entries = getStatisticEntries(category);
        }

        entries.sort(TOP_ENTRY_COMPARATOR);
        int limit = Math.min(entries.size(), configManager.getTopSize());
        return new ArrayList<TopEntry>(entries.subList(0, limit));
    }

    private List<TopEntry> getStatisticEntries(TopCategory category) {
        Map<UUID, Long> rawValues = statisticsStorageManager.loadStatistics(category);
        List<TopEntry> entries = new ArrayList<TopEntry>();
        for (Map.Entry<UUID, Long> entry : rawValues.entrySet()) {
            long value = entry.getValue().longValue();
            if (value <= 0L) {
                continue;
            }
            String playerName = resolvePlayerName(entry.getKey());
            String displayValue = formatStatisticValue(category, value);
            entries.add(new TopEntry(playerName, value, displayValue));
        }
        return entries;
    }

    private List<TopEntry> getMoneyEntries() {
        if (!economyManager.isAvailable()) {
            return new ArrayList<TopEntry>();
        }

        List<TopEntry> entries = new ArrayList<TopEntry>();
        Set<UUID> handledPlayers = new HashSet<UUID>();
        addMoneyEntries(entries, handledPlayers, Arrays.asList(Bukkit.getOfflinePlayers()));
        addMoneyEntries(entries, handledPlayers, Bukkit.getOnlinePlayers());
        return entries;
    }

    private void addMoneyEntries(List<TopEntry> entries, Set<UUID> handledPlayers, Iterable<? extends OfflinePlayer> players) {
        for (OfflinePlayer player : players) {
            if (player == null || player.getUniqueId() == null) {
                continue;
            }
            if (!handledPlayers.add(player.getUniqueId())) {
                continue;
            }
            if (!player.isOnline() && !player.hasPlayedBefore()) {
                continue;
            }
            double balance = economyManager.getBalance(player);
            if (balance <= 0.0D) {
                continue;
            }
            entries.add(new TopEntry(resolvePlayerName(player), balance, moneyFormat.format(balance)));
        }
    }

    private String resolvePlayerName(UUID uuid) {
        return resolvePlayerName(Bukkit.getOfflinePlayer(uuid));
    }

    private String resolvePlayerName(OfflinePlayer player) {
        String name = player.getName();
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        String uuid = player.getUniqueId() == null ? "unknown" : player.getUniqueId().toString();
        return uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
    }

    private String formatStatisticValue(TopCategory category, long value) {
        if (category == TopCategory.TIME) {
            return formatPlayTime(value);
        }
        return wholeNumberFormat.format(value);
    }

    private String formatPlayTime(long ticks) {
        long totalMinutes = Math.max(0L, ticks / 20L / 60L);
        long days = totalMinutes / 1440L;
        long hours = totalMinutes % 1440L / 60L;
        long minutes = totalMinutes % 60L;
        List<String> parts = new ArrayList<String>();
        if (days > 0L) {
            parts.add(days + " " + plural(days, "день", "дня", "дней"));
        }
        if (hours > 0L || days > 0L) {
            parts.add(hours + " " + plural(hours, "час", "часа", "часов"));
        }
        parts.add(minutes + " " + plural(minutes, "минута", "минуты", "минут"));
        return String.join(" ", parts);
    }

    private String plural(long number, String one, String few, String many) {
        long mod100 = number % 100L;
        long mod10 = number % 10L;
        if (mod100 >= 11L && mod100 <= 14L) {
            return many;
        }
        if (mod10 == 1L) {
            return one;
        }
        if (mod10 >= 2L && mod10 <= 4L) {
            return few;
        }
        return many;
    }

    private String applyPlaceholders(String template, int place, String player, String value) {
        return template
                .replace("{place}", String.valueOf(place))
                .replace("{player}", player)
                .replace("{value}", value);
    }
}
