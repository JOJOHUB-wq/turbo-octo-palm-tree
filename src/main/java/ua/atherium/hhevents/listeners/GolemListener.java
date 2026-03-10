package ua.atherium.hhevents.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.events.GolemEvent;
import ua.atherium.hhevents.utils.ItemBuilder;

public class GolemListener implements Listener {

    private final HHEvents plugin;
    private GolemEvent currentGolemEvent;

    public GolemListener(HHEvents plugin) {
        this.plugin = plugin;
    }

    public void setCurrentEvent(GolemEvent event) {
        this.currentGolemEvent = event;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (currentGolemEvent == null || currentGolemEvent.getStatus() != GolemEvent.EventStatus.ACTIVE) {
            return;
        }

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (damager instanceof Player) {
            if (currentGolemEvent.getGolem() != null && entity.getUniqueId().equals(currentGolemEvent.getGolem().getUniqueID())) {
                event.setDamage(0);
                event.setCancelled(true);
                currentGolemEvent.handleHit((Player) damager);
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Item item = event.getItem();
            ItemStack itemStack = item.getItemStack();
            if (itemStack.getType() == Material.GRAY_DYE) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null && meta.hasLore() && meta.getLore().contains("§7Подбери, чтобы расшифровать!")) {
                    item.setItemStack(new ItemBuilder(Material.DIAMOND).build());
                }
            }
        }
    }
}
