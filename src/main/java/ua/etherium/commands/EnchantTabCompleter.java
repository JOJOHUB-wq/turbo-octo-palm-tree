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
            completions.add("menu");
            completions.add("give");
            completions.add("enchant");
            completions.add("reload");
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }

            if (args[0].equalsIgnoreCase("enchant")) {
                if (!(sender instanceof Player)) {
                    return completions;
                }

                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();

                if (item.getType().isAir()) {
                    return completions;
                }

                for (CustomEnchant enchant : plugin.getEnchantManager().getAllEnchants()) {
                    if (enchant.canApplyTo(item)) {
                        completions.add(enchant.getKey());
                    }
                }

                return filterCompletions(completions, args[1]);
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                for (CustomEnchant enchant : plugin.getEnchantManager().getAllEnchants()) {
                    completions.add(enchant.getKey());
                }
                return filterCompletions(completions, args[2]);
            }

            if (args[0].equalsIgnoreCase("enchant")) {
                CustomEnchant enchant = plugin.getEnchantManager().getEnchant(args[1]);
                if (enchant != null) {
                    for (int i = 1; i <= enchant.getMaxLevel(); i++) {
                        completions.add(String.valueOf(i));
                    }
                }
                return completions;
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            CustomEnchant enchant = plugin.getEnchantManager().getEnchant(args[2]);
            if (enchant != null) {
                for (int i = 1; i <= enchant.getMaxLevel(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
            return completions;
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}