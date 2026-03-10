package ua.hhtops.command;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ua.hhtops.top.TopCategory;

public final class HHTopsTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Collections.singletonList("movehere");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("hhtops.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("movehere")) {
            return filter(TopCategory.arguments(), args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> source, String input) {
        String lowerInput = input.toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}
