package ua.etherium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.ArrayList;
import ua.etherium.utils.ColorUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantTabCompleter implements TabCompleter {

    private final AtheriumEnchants plugin;

    public EnchantTabCompleter(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("menu", "give", "enchant", "reload"));
            return filter(completions, args[0]);
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("give")) {
            return handleGiveCompletion(sender, args);
        }

        if (subCommand.equals("enchant")) {
            return handleEnchantCompletion(sender, args);
        }

        return completions;
    }

    private List<String> handleGiveCompletion(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            return filter(completions, args[1]);
        }

        if (args.length == 3) {
            completions.addAll(plugin.getEnchantManager().getAllEnchants().stream().map(CustomEnchant::getKey).collect(Collectors.toList()));
            return filter(completions, args[2]);
        }

        if (args.length == 4) {
            CustomEnchant enchant = plugin.getEnchantManager().getEnchant(args[2]);
            if (enchant != null) {
                for (int i = 1; i <= enchant.getMaxLevel(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
            return filter(completions, args[3]);
        }
        return completions;
    }

    private List<String> handleEnchantCompletion(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return new ArrayList<>();
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                 player.sendMessage(ColorUtils.color("&#FF6600Возьмите предмет в руку для подсказки по зачарованиям."));
                return completions;
            }

            for (CustomEnchant enchant : plugin.getEnchantManager().getAllEnchants()) {
                if (enchant.canApplyTo(item)) {
                    completions.add(enchant.getKey());
                }
            }
            return filter(completions, args[1]);
        }

        if (args.length == 3) {
            CustomEnchant enchant = plugin.getEnchantManager().getEnchant(args[1]);
            if (enchant != null) {
                for (int i = 1; i <= enchant.getMaxLevel(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
            return filter(completions, args[2]);
        }

        return completions;
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}