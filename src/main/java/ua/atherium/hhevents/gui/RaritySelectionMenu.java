package ua.atherium.hhevents.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.utils.ColorUtil;
import ua.atherium.hhevents.utils.ItemBuilder;

public class RaritySelectionMenu extends BaseGUI {

    private final HHEvents plugin;
    private final String eventType;

    public RaritySelectionMenu(HHEvents plugin, String eventType) {
        super(27, ColorUtil.color("&8Выберите редкость: " + eventType));
        this.plugin = plugin;
        this.eventType = eventType;

        setupMenu();
    }

    private void setupMenu() {
        inventory.setItem(11, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("&fОбычный").build());
        inventory.setItem(13, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).name("&9Редкий").build());
        inventory.setItem(15, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name("&5Мифический").build());
        inventory.setItem(22, new ItemBuilder(Material.ARROW).name("&eНазад").build());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (slot == 11) {
            new LootEditorMenu(plugin, eventType, "common", 0).open(player);
        } else if (slot == 13) {
            new LootEditorMenu(plugin, eventType, "rare", 0).open(player);
        } else if (slot == 15) {
            new LootEditorMenu(plugin, eventType, "mythic", 0).open(player);
        } else if (slot == 22) {
            new EventSetupMenu(plugin).open(player);
        }
    }
}
