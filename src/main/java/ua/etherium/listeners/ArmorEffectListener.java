package ua.etherium.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.enchants.types.SpringsEnchant;

public class ArmorEffectListener implements Listener {

    private final AtheriumEnchants plugin;

    public ArmorEffectListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateSpringsEffect(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateSpringsEffect(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getSlotType() == SlotType.ARMOR) {
            Player player = (Player) event.getWhoClicked();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateSpringsEffect(player), 1L);
        }
    }

    private void updateSpringsEffect(Player player) {
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) return;

        SpringsEnchant springs = (SpringsEnchant) plugin.getEnchantManager().getEnchant("springs");
        if (springs == null) return;

        int level = CustomEnchant.getEnchantLevel(boots, springs);
        if (level > 0) {
            int amplifier = plugin.getConfigManager().getEnchantsConfig().getInt(springs.getKey() + ".levels." + level + ".amplifier", 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, amplifier - 1, true, false, true));
        }
    }
}