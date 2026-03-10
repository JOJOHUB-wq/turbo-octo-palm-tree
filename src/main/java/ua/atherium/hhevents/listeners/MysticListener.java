package ua.atherium.hhevents.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.events.MysticEvent;

public class MysticListener implements Listener {

    private final HHEvents plugin;
    private MysticEvent currentMysticEvent;

    public MysticListener(HHEvents plugin) {
        this.plugin = plugin;
    }

    public void setCurrentEvent(MysticEvent event) {
        this.currentMysticEvent = event;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (currentMysticEvent == null || currentMysticEvent.getStatus() != MysticEvent.EventStatus.ACTIVE) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                if (event.getClickedBlock().getLocation().distanceSquared(currentMysticEvent.getLocation()) <= 2) {
                    if (currentMysticEvent.getCurrentPhase() == MysticEvent.MysticPhase.WAITING_ACTIVATION) {
                        currentMysticEvent.forceActivate();
                    } else if (currentMysticEvent.getCurrentPhase() == MysticEvent.MysticPhase.OPENED) {
                        event.getPlayer().openInventory(currentMysticEvent.getChestInventory());
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (currentMysticEvent == null || currentMysticEvent.getStatus() != MysticEvent.EventStatus.ACTIVE) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.equals(currentMysticEvent.getChestInventory())) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            int slot = event.getSlot();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                currentMysticEvent.handleItemClick(player, slot, clickedItem);
            }
        }
    }
}
