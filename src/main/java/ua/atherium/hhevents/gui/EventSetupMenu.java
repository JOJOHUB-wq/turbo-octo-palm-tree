package ua.atherium.hhevents.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.utils.ColorUtil;
import ua.atherium.hhevents.utils.ItemBuilder;

public class EventSetupMenu extends BaseGUI {

    private final HHEvents plugin;

    public EventSetupMenu(HHEvents plugin) {
        super(27, ColorUtil.color("&8Редактор ивентов"));
        this.plugin = plugin;

        setupMenu();
    }

    private void setupMenu() {
        inventory.setItem(11, new ItemBuilder(Material.ENDER_CHEST).name("&5Мистик").lore("&7Настройка ивента Мистик").build());
        inventory.setItem(15, new ItemBuilder(Material.IRON_BLOCK).name("&7Голем").lore("&7Настройка ивента Голем").build());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (slot == 11) {
            new RaritySelectionMenu(plugin, "mystic").open(player);
        } else if (slot == 15) {
            new RaritySelectionMenu(plugin, "golem").open(player);
        }
    }
}
