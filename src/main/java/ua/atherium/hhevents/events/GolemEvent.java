package ua.atherium.hhevents.events;

import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.entities.CustomGolem;
import ua.atherium.hhevents.utils.ColorUtil;
import ua.atherium.hhevents.utils.ItemBuilder;
import ua.atherium.hhevents.utils.SchematicUtil;

import java.io.File;

public class GolemEvent extends BaseEvent {

    private final HHEvents plugin;
    private int hitsLeft;
    private CustomGolem golem;

    public GolemEvent(HHEvents plugin) {
        this.plugin = plugin;
        this.hitsLeft = plugin.getConfig().getInt("golem.health", 1000);
    }

    @Override
    public void start() {
        if (location == null) return;
        setStatus(EventStatus.ACTIVE);

        File schemDir = new File(plugin.getDataFolder(), "schematics");
        if (!schemDir.exists()) schemDir.mkdirs();

        File schemFile = new File(schemDir, plugin.getConfig().getString("golem.schematic", "golem_arena.schem"));
        if (schemFile.exists()) {
            SchematicUtil.pasteSchematic(schemFile, location);
        }

        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        golem = new CustomGolem(location.clone().add(0, 1, 0), 10);
        nmsWorld.addEntity(golem, CreatureSpawnEvent.SpawnReason.CUSTOM);

        String msg = plugin.getConfig().getString("messages.golem-spawn")
                .replace("%x%", String.valueOf(location.getBlockX()))
                .replace("%y%", String.valueOf(location.getBlockY()))
                .replace("%z%", String.valueOf(location.getBlockZ()));
        Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + msg));
    }

    public void handleHit(Player player) {
        if (status != EventStatus.ACTIVE) return;

        hitsLeft--;
        giveReward(player);

        if (hitsLeft <= 0) {
            die();
        }
    }

    private void giveReward(Player player) {
        double rand = Math.random();
        if (rand < 0.3) {
            int minCoin = plugin.getConfig().getInt("golem.rewards.coins-min");
            int maxCoin = plugin.getConfig().getInt("golem.rewards.coins-max");
            int amount = minCoin + (int) (Math.random() * ((maxCoin - minCoin) + 1));
            player.sendMessage(ColorUtil.color("&aВы получили " + amount + " монет!"));
        } else if (rand < 0.5) {
            int minDon = plugin.getConfig().getInt("golem.rewards.donate-min");
            int maxDon = plugin.getConfig().getInt("golem.rewards.donate-max");
            int amount = minDon + (int) (Math.random() * ((maxDon - minDon) + 1));
            if (amount > 0) {
                String cmd = plugin.getConfig().getString("golem.donate-command")
                        .replace("%player%", player.getName())
                        .replace("%amount%", String.valueOf(amount));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                player.sendMessage(ColorUtil.color("&bВы получили " + amount + " донат-валюты!"));
            }
        }
    }

    private void die() {
        setStatus(EventStatus.FINISHED);
        if (golem != null) {
            golem.die();
        }

        scatterLoot();

        if (location != null) {
            plugin.getProtectionManager().removeProtection(location);
        }
        plugin.setActiveEvent(null);
        plugin.getEventTimerManager().setPaused(false);

        Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.event-end")));
    }

    private void scatterLoot() {
        Location base = location.clone().add(0, 1, 0);
        for (int i = 0; i < 20; i++) {
            ItemStack loot = new ItemBuilder(Material.DIAMOND).build();
            ItemStack encrypted = encryptItem(loot);

            org.bukkit.entity.Item item = base.getWorld().dropItem(base, encrypted);
            item.setPickupDelay(40);

            double x = (Math.random() - 0.5) * 1.5;
            double y = 3 + Math.random() * 4;
            double z = (Math.random() - 0.5) * 1.5;
            item.setVelocity(new Vector(x, y, z));

            base.getWorld().spawnParticle(org.bukkit.Particle.WATER_SPLASH, base, 10);
        }
    }

    private ItemStack encryptItem(ItemStack original) {
        return new ItemBuilder(Material.GRAY_DYE)
                .name("&k" + Math.random())
                .lore("&7Подбери, чтобы расшифровать!")
                .build();
    }

    @Override
    public void stop() {
        if (status == EventStatus.ACTIVE) {
            setStatus(EventStatus.FINISHED);
            if (golem != null && golem.isAlive()) {
                golem.die();
            }

            if (location != null) {
                plugin.getProtectionManager().removeProtection(location);
            }
            plugin.setActiveEvent(null);
            plugin.getEventTimerManager().setPaused(false);

            Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.event-end")));
        }
    }

    public CustomGolem getGolem() {
        return golem;
    }
}
