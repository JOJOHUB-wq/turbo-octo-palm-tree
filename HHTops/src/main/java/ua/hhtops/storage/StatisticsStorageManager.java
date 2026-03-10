package ua.hhtops.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ua.hhtops.HHTopsPlugin;
import ua.hhtops.top.TopCategory;

public final class StatisticsStorageManager {

    private final HHTopsPlugin plugin;

    public StatisticsStorageManager(HHTopsPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, Long> loadStatistics(TopCategory category) {
        Map<UUID, Long> values = new HashMap<UUID, Long>();
        if (!category.hasStatistic()) {
            return values;
        }

        Path statsDirectory = resolveStatsDirectory();
        if (statsDirectory != null && Files.isDirectory(statsDirectory)) {
            try (Stream<Path> stream = Files.list(statsDirectory)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                        .forEach(path -> readPlayerStatistic(path, category, values));
            } catch (IOException exception) {
                plugin.getLogger().warning("Не удалось прочитать папку статистики игроков: " + exception.getMessage());
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            values.put(player.getUniqueId(), Long.valueOf(player.getStatistic(category.getStatistic())));
        }
        return values;
    }

    private void readPlayerStatistic(Path path, TopCategory category, Map<UUID, Long> values) {
        UUID uuid = parseUuid(path.getFileName().toString());
        if (uuid == null) {
            return;
        }

        long value = readStatisticValue(path, category.getStatisticKey());
        values.put(uuid, Long.valueOf(value));
    }

    private long readStatisticValue(Path path, String statisticKey) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            JsonObject statsObject = getObject(root, "stats");
            JsonObject customObject = getObject(statsObject, "minecraft:custom");
            if (customObject == null) {
                return 0L;
            }
            JsonElement element = customObject.get(statisticKey);
            if (element == null || !element.isJsonPrimitive()) {
                return 0L;
            }
            return element.getAsLong();
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось прочитать файл статистики " + path.getFileName() + ": " + exception.getMessage());
            return 0L;
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Не удалось разобрать файл статистики " + path.getFileName() + ": " + exception.getMessage());
            return 0L;
        }
    }

    private JsonObject getObject(JsonObject source, String key) {
        if (source == null) {
            return null;
        }
        JsonElement element = source.get(key);
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    private Path resolveStatsDirectory() {
        if (Bukkit.getWorlds().isEmpty()) {
            return null;
        }
        World primaryWorld = Bukkit.getWorlds().get(0);
        return primaryWorld.getWorldFolder().toPath().resolve("stats");
    }

    private UUID parseUuid(String fileName) {
        String raw = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
