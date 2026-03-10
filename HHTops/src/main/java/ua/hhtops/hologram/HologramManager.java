package ua.hhtops.hologram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import ua.hhtops.HHTopsPlugin;
import ua.hhtops.storage.ConfigManager;
import ua.hhtops.top.TopCategory;

public final class HologramManager {

    private final HHTopsPlugin plugin;
    private final ConfigManager configManager;
    private final NamespacedKey hologramKey;
    private final NamespacedKey categoryKey;
    private final Map<TopCategory, List<UUID>> spawnedEntities;

    public HologramManager(HHTopsPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.hologramKey = new NamespacedKey(plugin, "hologram");
        this.categoryKey = new NamespacedKey(plugin, "category");
        this.spawnedEntities = new EnumMap<TopCategory, List<UUID>>(TopCategory.class);
    }

    public void updateHologram(TopCategory category, List<String> lines) {
        Location baseLocation = configManager.getHologramLocation(category);
        if (baseLocation == null || baseLocation.getWorld() == null) {
            removeHologram(category);
            plugin.getLogger().warning("Не удалось создать голограмму категории " + category.getDisplayName() + ": мир из конфига не найден.");
            return;
        }

        List<ArmorStand> activeHologram = getActiveHologram(category);
        if (activeHologram.size() != lines.size()) {
            removeHologram(category);
            spawnedEntities.put(category, spawnHologram(category, baseLocation, lines));
            return;
        }

        updateHologramLines(activeHologram, baseLocation, lines);
    }

    public void removeHologram(TopCategory category) {
        List<UUID> entityIds = spawnedEntities.remove(category);
        if (entityIds == null || entityIds.isEmpty()) {
            removeTaggedHolograms(category);
            return;
        }
        for (UUID entityId : entityIds) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        removeTaggedHolograms(category);
    }

    public void cleanupAll() {
        for (TopCategory category : TopCategory.values()) {
            removeHologram(category);
        }
        spawnedEntities.clear();
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (isManagedArmorStand(armorStand)) {
                    armorStand.remove();
                }
            }
        }
    }

    private List<UUID> spawnHologram(TopCategory category, Location baseLocation, List<String> lines) {
        List<UUID> entityIds = new ArrayList<UUID>(lines.size());
        double spacing = configManager.getLineSpacing();
        for (int index = 0; index < lines.size(); index++) {
            Location lineLocation = baseLocation.clone().add(0.0D, -spacing * index, 0.0D);
            ArmorStand armorStand = spawnLine(lineLocation, normalizeLine(lines.get(index)), category);
            entityIds.add(armorStand.getUniqueId());
        }
        return entityIds;
    }

    private void updateHologramLines(List<ArmorStand> armorStands, Location baseLocation, List<String> lines) {
        double spacing = configManager.getLineSpacing();
        for (int index = 0; index < armorStands.size(); index++) {
            ArmorStand armorStand = armorStands.get(index);
            armorStand.teleport(baseLocation.clone().add(0.0D, -spacing * index, 0.0D));
            armorStand.setCustomName(normalizeLine(lines.get(index)));
            armorStand.setCustomNameVisible(true);
        }
    }

    private List<ArmorStand> getActiveHologram(TopCategory category) {
        List<UUID> entityIds = spawnedEntities.get(category);
        if (entityIds == null || entityIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ArmorStand> armorStands = new ArrayList<ArmorStand>(entityIds.size());
        for (UUID entityId : entityIds) {
            Entity entity = Bukkit.getEntity(entityId);
            if (!(entity instanceof ArmorStand) || !entity.isValid()) {
                return Collections.emptyList();
            }
            armorStands.add((ArmorStand) entity);
        }
        return armorStands;
    }

    private ArmorStand spawnLine(Location location, String text, TopCategory category) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(text);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setSmall(false);
        armorStand.setInvulnerable(true);
        armorStand.setSilent(true);
        armorStand.setCollidable(false);
        armorStand.setRemoveWhenFarAway(false);
        armorStand.addScoreboardTag("hhtops");
        armorStand.addScoreboardTag("hhtops:" + category.getArgument());
        armorStand.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, Byte.valueOf((byte) 1));
        armorStand.getPersistentDataContainer().set(categoryKey, PersistentDataType.STRING, category.getArgument());
        return armorStand;
    }

    private void removeTaggedHolograms(TopCategory category) {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (isCategoryArmorStand(armorStand, category)) {
                    armorStand.remove();
                }
            }
        }
    }

    private boolean isManagedArmorStand(ArmorStand armorStand) {
        return armorStand.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE) || armorStand.getScoreboardTags().contains("hhtops");
    }

    private boolean isCategoryArmorStand(ArmorStand armorStand, TopCategory category) {
        String storedCategory = armorStand.getPersistentDataContainer().get(categoryKey, PersistentDataType.STRING);
        return category.getArgument().equalsIgnoreCase(storedCategory) || armorStand.getScoreboardTags().contains("hhtops:" + category.getArgument());
    }

    private String normalizeLine(String line) {
        return line == null || line.isEmpty() ? " " : line;
    }
}
