package ua.hhtops.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.hhtops.storage.ConfigManager;
import ua.hhtops.top.TopCategory;
import ua.hhtops.top.TopManager;

public final class HHTopsCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final TopManager topManager;

    public HHTopsCommand(ConfigManager configManager, TopManager topManager) {
        this.configManager = configManager;
        this.topManager = topManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hhtops.admin")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("only-player"));
            return true;
        }

        if (args.length != 2 || !args[0].equalsIgnoreCase("movehere")) {
            sender.sendMessage(configManager.getMessage("usage"));
            return true;
        }

        TopCategory category = TopCategory.fromArgument(args[1]);
        if (category == null) {
            sender.sendMessage(configManager.getMessage("unknown-category"));
            return true;
        }

        Player player = (Player) sender;
        topManager.moveHologram(category, player.getLocation());
        player.sendMessage(configManager.getMessage("moved").replace("{category}", category.getDisplayName()));
        return true;
    }
}
