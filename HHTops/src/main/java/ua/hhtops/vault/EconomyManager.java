package ua.hhtops.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import ua.hhtops.HHTopsPlugin;

public final class EconomyManager {

    private final HHTopsPlugin plugin;
    private Economy economy;

    public EconomyManager(HHTopsPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = provider.getProvider();
        }
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public double getBalance(OfflinePlayer player) {
        if (economy == null || player == null) {
            return 0.0D;
        }
        try {
            return economy.getBalance(player);
        } catch (NoSuchMethodError exception) {
            String name = player.getName();
            return name == null ? 0.0D : economy.getBalance(name);
        } catch (UnsupportedOperationException exception) {
            String name = player.getName();
            return name == null ? 0.0D : economy.getBalance(name);
        }
    }
}
