package ua.atherium.hhevents.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.gui.EventSetupMenu;
import ua.atherium.hhevents.utils.ColorUtil;

public class EventCommand implements CommandExecutor {

    private final HHEvents plugin;

    public EventCommand(HHEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hhevents.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cУ вас нет прав!"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.color("&8&m----------------------------------"));
            sender.sendMessage(ColorUtil.color("&e/event delay &7- Время до следующего ивента"));
            sender.sendMessage(ColorUtil.color("&e/event start <mystic|golem|vote> &7- Запустить ивент"));
            sender.sendMessage(ColorUtil.color("&e/event tp &7- Телепорт к активному ивенту"));
            sender.sendMessage(ColorUtil.color("&e/event give &7- Получить инструменты админа"));
            sender.sendMessage(ColorUtil.color("&e/event display &7- Вкл/Выкл BossBar"));
            sender.sendMessage(ColorUtil.color("&e/event gui &7- Открыть редактор ивентов"));
            sender.sendMessage(ColorUtil.color("&8&m----------------------------------"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "delay":
                int timeLeft = plugin.getEventTimerManager().getTimeLeft();
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&eДо следующего ивента осталось: &a" + timeLeft + " сек."));
                break;

            case "start":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cУкажите тип ивента (mystic, golem, vote)"));
                    return true;
                }
                String type = args[1].toLowerCase();
                if (type.equals("mystic")) {
                    plugin.getEventTimerManager().startMysticEvent();
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&aИвент Мистик запущен форсированно."));
                } else if (type.equals("golem")) {
                    plugin.getEventTimerManager().startGolemEvent();
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&aИвент Голем запущен форсированно."));
                } else if (type.equals("vote")) {
                    plugin.getEventTimerManager().startVoteEvent();
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&aГолосование запущено форсированно."));
                } else {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cНеизвестный тип ивента."));
                }
                break;

            case "vote_gui":
                if (sender instanceof Player && plugin.getActiveEvent() instanceof ua.atherium.hhevents.events.VoteEvent) {
                    new ua.atherium.hhevents.gui.VoteMenu(plugin, (ua.atherium.hhevents.events.VoteEvent) plugin.getActiveEvent()).open((Player) sender);
                }
                break;

            case "tp":
                if (sender instanceof Player) {
                    if (plugin.getActiveEvent() != null && plugin.getActiveEvent().getLocation() != null) {
                        ((Player) sender).teleport(plugin.getActiveEvent().getLocation());
                        sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&aВы телепортированы к ивенту!"));
                    } else {
                        sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cНет активного ивента с локацией."));
                    }
                }
                break;

            case "give":
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    org.bukkit.inventory.ItemStack stick = new ua.atherium.hhevents.utils.ItemBuilder(org.bukkit.Material.STICK)
                        .name("&dМагическая палка")
                        .lore("&7ЛКМ = Поз1, ПКМ = Поз2")
                        .build();
                    org.bukkit.inventory.ItemStack coin = new ua.atherium.hhevents.utils.ItemBuilder(org.bukkit.Material.SUNFLOWER)
                        .name("&eМонетка")
                        .modelData(1)
                        .build();
                    p.getInventory().addItem(stick, coin);
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&aИнструменты выданы."));
                } else {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cТолько для игроков."));
                }
                break;

            case "display":
                if (sender instanceof Player) {
                    plugin.getEventTimerManager().toggleBossBar((Player) sender);
                } else {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cТолько для игроков."));
                }
                break;

            case "gui":
                if (sender instanceof Player) {
                    new EventSetupMenu(plugin).open((Player) sender);
                } else {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cТолько для игроков."));
                }
                break;

            default:
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cНеизвестная команда. Введите /event"));
                break;
        }

        return true;
    }
}
