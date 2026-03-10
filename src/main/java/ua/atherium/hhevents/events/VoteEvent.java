package ua.atherium.hhevents.events;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.utils.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteEvent extends BaseEvent {

    private final HHEvents plugin;
    private int timeLeft = 180;
    private BukkitRunnable task;
    private final Map<UUID, String> votes = new HashMap<>();

    private int mysticVotes = 0;
    private int golemVotes = 0;

    public VoteEvent(HHEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        setStatus(EventStatus.ACTIVE);

        TextComponent message = new TextComponent(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&eНачалось голосование за следующий ивент! "));
        TextComponent click = new TextComponent(ColorUtil.color("&a&l[НАЖМИ ЧТОБЫ ПРОГОЛОСОВАТЬ]"));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event vote_gui"));
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtil.color("&7Нажми, чтобы открыть меню!")).create()));

        message.addExtra(click);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(message);
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                timeLeft--;
                if (timeLeft <= 0) {
                    finishVoting();
                }
            }
        };
        task.runTaskTimer(plugin, 20L, 20L);
    }

    public void addVote(Player player, String eventType) {
        if (status != EventStatus.ACTIVE) return;

        if (votes.containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cВы уже проголосовали!"));
            return;
        }

        votes.put(player.getUniqueId(), eventType);
        if (eventType.equalsIgnoreCase("mystic")) {
            mysticVotes++;
        } else if (eventType.equalsIgnoreCase("golem")) {
            golemVotes++;
        }

        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&aВаш голос учтен!"));
    }

    private void finishVoting() {
        stop();

        String winner;
        if (mysticVotes > golemVotes) {
            winner = "Мистик";
            plugin.getEventTimerManager().startMysticEvent();
        } else if (golemVotes > mysticVotes) {
            winner = "Голем";
            plugin.getEventTimerManager().startGolemEvent();
        } else {
            if (Math.random() > 0.5) {
                winner = "Мистик (Случайно)";
                plugin.getEventTimerManager().startMysticEvent();
            } else {
                winner = "Голем (Случайно)";
                plugin.getEventTimerManager().startGolemEvent();
            }
        }

        Bukkit.broadcastMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&eГолосование завершено! Победил: &a" + winner));
    }

    @Override
    public void stop() {
        setStatus(EventStatus.FINISHED);
        if (task != null) {
            task.cancel();
        }
        plugin.setActiveEvent(null);
        plugin.getEventTimerManager().setPaused(false);
    }

    public int getMysticVotes() {
        return mysticVotes;
    }

    public int getGolemVotes() {
        return golemVotes;
    }
}
