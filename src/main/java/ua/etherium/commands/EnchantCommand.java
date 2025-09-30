package ua.etherium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.gui.MenuManager;
import ua.etherium.utils.ColorUtils;

public class EnchantCommand implements CommandExecutor {

    private final AtheriumEnchants plugin;
    private final MenuManager menuManager;

    public EnchantCommand(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.menuManager = new MenuManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ColorUtils.color("&#FF6600Использование: /ate <menu|give|enchant|reload>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "menu":
                return handleMenu(sender);
            case "give":
                return handleGive(sender, args);
            case "enchant":
                return handleEnchant(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sender.sendMessage(ColorUtils.color("&#FF0000Неизвестная команда!"));
                return true;
        }
    }

    private boolean handleMenu(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.color("&#FF0000Команда только для игроков!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("atheriumenchants.menu")) {
            player.sendMessage(ColorUtils.color("&#FF0000У вас нет прав!"));
            return true;
        }

        menuManager.openMenu(player, "main");
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("atheriumenchants.give")) {
            sender.sendMessage(ColorUtils.color("&#FF0000У вас нет прав!"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.color("&#FF6600Использование: /ate give <игрок> <зачарование> [уровень]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.color("&#FF0000Игрок не найден!"));
            return true;
        }

        String enchantKey = args[2];
        CustomEnchant enchant = plugin.getEnchantManager().getEnchant(enchantKey);

        if (enchant == null) {
            sender.sendMessage(ColorUtils.color("&#FF0000Зачарование не найдено!"));
            return true;
        }

        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtils.color("&#FF0000Неверный уровень!"));
                return true;
            }
        }

        if (level < 1 || level > enchant.getMaxLevel()) {
            sender.sendMessage(ColorUtils.color("&#FF0000Уровень должен быть от 1 до " + enchant.getMaxLevel()));
            return true;
        }

        ItemStack book = enchant.createEnchantedBook(level);
        target.getInventory().addItem(book);

        sender.sendMessage(ColorUtils.color("&#29FF1DВыдана книга зачарования!"));
        target.sendMessage(ColorUtils.color("&#29FF1DВы получили книгу зачарования!"));
        return true;
    }

    private boolean handleEnchant(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.color("&#FF0000Команда только для игроков!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("atheriumenchants.enchant")) {
            player.sendMessage(ColorUtils.color("&#FF0000У вас нет прав!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtils.color("&#FF6600Использование: /ate enchant <зачарование> [уровень]"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(ColorUtils.color("&#FF0000Возьмите предмет в руку!"));
            return true;
        }

        String enchantKey = args[1];
        CustomEnchant enchant = plugin.getEnchantManager().getEnchant(enchantKey);

        if (enchant == null) {
            player.sendMessage(ColorUtils.color("&#FF0000Зачарование не найдено!"));
            return true;
        }

        if (!enchant.canApplyTo(item)) {
            player.sendMessage(ColorUtils.color("&#FF0000Нельзя наложить это зачарование на данный предмет!"));
            return true;
        }

        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ColorUtils.color("&#FF0000Неверный уровень!"));
                return true;
            }
        }

        if (level < 1 || level > enchant.getMaxLevel()) {
            player.sendMessage(ColorUtils.color("&#FF0000Уровень должен быть от 1 до " + enchant.getMaxLevel()));
            return true;
        }

        ItemStack enchanted = enchant.applyToItem(item, level);
        player.getInventory().setItemInMainHand(enchanted);
        player.sendMessage(ColorUtils.color("&#29FF1DПредмет зачарован!"));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("atheriumenchants.reload")) {
            sender.sendMessage(ColorUtils.color("&#FF0000У вас нет прав!"));
            return true;
        }

        plugin.reloadConfig();
        plugin.getConfigManager().reload();
        plugin.getEnchantManager().loadEnchants();

        sender.sendMessage(ColorUtils.color("&#29FF1DКонфиги перезагружены!"));
        return true;
    }
}