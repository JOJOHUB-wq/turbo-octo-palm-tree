package ua.atherium.hhevents.managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.utils.ColorUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EventTimerManager {

    private final HHEvents plugin;
    private final ConfigManager configManager;
    private int timeLeft;
    private boolean paused;
    private BossBar bossBar;
    private final Set<UUID> hiddenBossBars = new HashSet<>();

    public EventTimerManager(HHEvents plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.timeLeft = configManager.getInt("settings.default-timer");
        this.paused = false;

        String title = configManager.getString("messages.bossbar");
        this.bossBar = Bukkit.createBossBar(ColorUtil.color(title), BarColor.YELLOW, BarStyle.SOLID);

        startTimer();
    }

    private void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!paused) {
                    timeLeft--;
                    if (timeLeft <= 0) {
                        triggerRandomEvent();
                        timeLeft = configManager.getInt("settings.default-timer");
                    }
                }
                updateBossBar();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void updateBossBar() {
        if (!paused) {
            String formatTime = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
            String title = configManager.getString("messages.bossbar")
                    .replace("%name%", "Ожидание")
                    .replace("%rarity%", "Обычный")
                    .replace("%time%", formatTime);
            bossBar.setTitle(ColorUtil.color(title));
            bossBar.setProgress((double) timeLeft / configManager.getInt("settings.default-timer"));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!hiddenBossBars.contains(player.getUniqueId())) {
                bossBar.addPlayer(player);
            } else {
                bossBar.removePlayer(player);
            }
        }
    }

    private void triggerRandomEvent() {
        int mysticChance = configManager.getInt("chances.mystic");
        int golemChance = configManager.getInt("chances.golem");
        int voteChance = configManager.getInt("chances.vote");
        int total = mysticChance + golemChance + voteChance;

        if (total == 0) return;

        int random = ThreadLocalRandom.current().nextInt(total);
        if (random < mysticChance) {
            startMysticEvent();
        } else if (random < mysticChance + golemChance) {
            startGolemEvent();
        } else {
            startVoteEvent();
        }
    }

    public void startMysticEvent() {
        paused = true;
        org.bukkit.World world = Bukkit.getWorld(configManager.getString("settings.world", "world"));
        int radius = configManager.getInt("settings.search-radius", 5000);

        ua.atherium.hhevents.utils.LocationUtil.findSafeLocation(world, radius).thenAccept(loc -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (loc == null) {
                    plugin.getLogger().warning("Не удалось найти безопасную локацию для Мистика!");
                    paused = false;
                    return;
                }
                ua.atherium.hhevents.events.MysticEvent mystic = new ua.atherium.hhevents.events.MysticEvent(plugin);
                mystic.setLocation(loc);
                plugin.setActiveEvent(mystic);
                plugin.getProtectionManager().addProtection(loc);
                plugin.getMysticListener().setCurrentEvent(mystic);
                mystic.start();
            });
        });
    }

    public void startGolemEvent() {
        paused = true;
        org.bukkit.World world = Bukkit.getWorld(configManager.getString("settings.world", "world"));
        int radius = configManager.getInt("settings.search-radius", 5000);

        ua.atherium.hhevents.utils.LocationUtil.findSafeLocation(world, radius).thenAccept(loc -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (loc == null) {
                    plugin.getLogger().warning("Не удалось найти безопасную локацию для Голема!");
                    paused = false;
                    return;
                }
                ua.atherium.hhevents.events.GolemEvent golem = new ua.atherium.hhevents.events.GolemEvent(plugin);
                golem.setLocation(loc);
                plugin.setActiveEvent(golem);
                plugin.getProtectionManager().addProtection(loc);
                plugin.getGolemListener().setCurrentEvent(golem);
                golem.start();
            });
        });
    }

    public void startVoteEvent() {
        paused = true;
        ua.atherium.hhevents.events.VoteEvent vote = new ua.atherium.hhevents.events.VoteEvent(plugin);
        plugin.setActiveEvent(vote);
        vote.start();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void toggleBossBar(Player player) {
        if (hiddenBossBars.contains(player.getUniqueId())) {
            hiddenBossBars.remove(player.getUniqueId());
            bossBar.addPlayer(player);
            player.sendMessage(ColorUtil.color(configManager.getString("messages.prefix") + "BossBar включен."));
        } else {
            hiddenBossBars.add(player.getUniqueId());
            bossBar.removePlayer(player);
            player.sendMessage(ColorUtil.color(configManager.getString("messages.prefix") + "BossBar скрыт."));
        }
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public BossBar getBossBar() {
        return bossBar;
    }
}
