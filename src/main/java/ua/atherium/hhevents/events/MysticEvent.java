package ua.atherium.hhevents.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.utils.ColorUtil;
import ua.atherium.hhevents.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MysticEvent extends BaseEvent {

    private final HHEvents plugin;
    private int phaseTimeLeft;
    private MysticPhase currentPhase;
    private Inventory chestInventory;
    private BukkitRunnable task;
    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public MysticEvent(HHEvents plugin) {
        this.plugin = plugin;
        this.currentPhase = MysticPhase.WAITING_ACTIVATION;
        this.phaseTimeLeft = 300;
    }

    @Override
    public void start() {
        if (location == null) return;

        setStatus(EventStatus.ACTIVE);
        spawnEnderChest();

        String msg = plugin.getConfig().getString("messages.mystic-spawn")
                .replace("%x%", String.valueOf(location.getBlockX()))
                .replace("%y%", String.valueOf(location.getBlockY()))
                .replace("%z%", String.valueOf(location.getBlockZ()));
        Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + msg));

        startTask();
    }

    private void spawnEnderChest() {
        Block block = location.getBlock();
        block.setType(Material.ENDER_CHEST);
        Block block2 = location.clone().add(1, 0, 0).getBlock();
        block2.setType(Material.ENDER_CHEST);

        chestInventory = Bukkit.createInventory(null, 54, ColorUtil.color("&5Мистический Сундук"));
    }

    private void startTask() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                phaseTimeLeft--;

                if (currentPhase == MysticPhase.WAITING_ACTIVATION) {
                    if (phaseTimeLeft <= 0) {
                        currentPhase = MysticPhase.WAITING_OPEN;
                        phaseTimeLeft = 180;
                        Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "Мистик активирован! Открытие через 3 минуты."));
                    }
                } else if (currentPhase == MysticPhase.WAITING_OPEN) {
                    applyEffects();
                    if (phaseTimeLeft <= 0) {
                        currentPhase = MysticPhase.OPENED;
                        phaseTimeLeft = 180;
                        Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "Мистик открыт! Лут спавнится."));
                    }
                } else if (currentPhase == MysticPhase.OPENED) {
                    spawnLoot();
                    updateEncryptedItems();
                    if (phaseTimeLeft <= 0 || isInventoryEmpty()) {
                        stop();
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 20L, 20L);
    }

    public void forceActivate() {
        if (currentPhase == MysticPhase.WAITING_ACTIVATION) {
            currentPhase = MysticPhase.WAITING_OPEN;
            phaseTimeLeft = 180;
            Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "Мистик был досрочно активирован! Открытие через 3 минуты."));
        }
    }

    private void applyEffects() {
        for (Player p : location.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(location) <= 100) {
                if (Math.random() < 0.2) {
                    p.setFireTicks(60);
                }
            }
        }
    }

    private void spawnLoot() {
        if (Math.random() < 0.3) {
            ItemStack loot = getRandomLoot();
            if (loot != null) {
                int slot = getEmptySlot();
                if (slot != -1) {
                    chestInventory.setItem(slot, encryptItem(loot));
                }
            }
        }
    }

    private int getEmptySlot() {
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < chestInventory.getSize(); i++) {
            if (chestInventory.getItem(i) == null || chestInventory.getItem(i).getType() == Material.AIR) {
                emptySlots.add(i);
            }
        }
        if (emptySlots.isEmpty()) return -1;
        return emptySlots.get((int) (Math.random() * emptySlots.size()));
    }

    private ItemStack getRandomLoot() {
        return new ItemStack(Material.DIAMOND, 1);
    }

    private ItemStack encryptItem(ItemStack original) {
        return new ItemBuilder(Material.GRAY_DYE)
                .name("&k" + Math.random())
                .lore("&7Нажми, чтобы расшифровать!")
                .build();
    }

    private void updateEncryptedItems() {
        for (int i = 0; i < chestInventory.getSize(); i++) {
            ItemStack item = chestInventory.getItem(i);
            if (item != null && item.getType() == Material.GRAY_DYE) {
                item.getItemMeta().setDisplayName(ColorUtil.color("&k" + Math.random()));
                chestInventory.setItem(i, item);
            }
        }
    }

    public void handleItemClick(Player player, int slot, ItemStack clickedItem) {
        long current = System.currentTimeMillis();
        if (cooldowns.containsKey(player.getUniqueId()) && current - cooldowns.get(player.getUniqueId()) < 500) {
            spawnFakeItemBounce(player);
            return;
        }

        cooldowns.put(player.getUniqueId(), current);

        ItemStack decrypted = getRandomLoot();
        player.getInventory().addItem(decrypted);
        chestInventory.setItem(slot, null);
    }

    private void spawnFakeItemBounce(Player player) {
        Location playerLoc = player.getLocation();
        Location chestLoc = location.clone().add(0.5, 1, 0.5);

        net.minecraft.server.v1_16_R3.World nmsWorld = ((org.bukkit.craftbukkit.v1_16_R3.CraftWorld) playerLoc.getWorld()).getHandle();
        net.minecraft.server.v1_16_R3.EntityItem fakeItem = new net.minecraft.server.v1_16_R3.EntityItem(
                nmsWorld,
                playerLoc.getX(),
                playerLoc.getY() + 1,
                playerLoc.getZ(),
                org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack.asNMSCopy(new ItemStack(Material.ENDER_PEARL))
        );

        net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity packetSpawn = new net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity(fakeItem, 2);
        net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata packetMeta = new net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata(fakeItem.getId(), fakeItem.getDataWatcher(), true);

        ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packetSpawn);
        ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packetMeta);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                if (ticks > 20) {
                    net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy packetDestroy = new net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy(fakeItem.getId());
                    ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packetDestroy);
                    this.cancel();
                    return;
                }

                double fraction = (double) ticks / 20;
                double currentX = playerLoc.getX() + (chestLoc.getX() - playerLoc.getX()) * fraction;
                double currentY = playerLoc.getY() + 1 + (chestLoc.getY() - (playerLoc.getY() + 1)) * fraction;
                double currentZ = playerLoc.getZ() + (chestLoc.getZ() - playerLoc.getZ()) * fraction;

                fakeItem.setLocation(currentX, currentY, currentZ, 0, 0);

                net.minecraft.server.v1_16_R3.PacketPlayOutEntityTeleport packetTeleport = new net.minecraft.server.v1_16_R3.PacketPlayOutEntityTeleport(fakeItem);
                ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packetTeleport);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private boolean isInventoryEmpty() {
        for (ItemStack item : chestInventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void stop() {
        setStatus(EventStatus.FINISHED);
        if (task != null) {
            task.cancel();
        }

        location.getBlock().setType(Material.AIR);
        location.clone().add(1, 0, 0).getBlock().setType(Material.AIR);

        plugin.getProtectionManager().removeProtection(location);
        plugin.setActiveEvent(null);
        plugin.getEventTimerManager().setPaused(false);

        Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.event-end")));
    }

    public Inventory getChestInventory() {
        return chestInventory;
    }

    public MysticPhase getCurrentPhase() {
        return currentPhase;
    }

    public enum MysticPhase {
        WAITING_ACTIVATION,
        WAITING_OPEN,
        OPENED
    }
}
