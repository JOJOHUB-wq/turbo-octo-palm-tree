package ua.atherium.hhevents.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.managers.LootManager;
import ua.atherium.hhevents.utils.ColorUtil;
import ua.atherium.hhevents.utils.ItemBuilder;

import java.util.List;

public class LootEditorMenu extends BaseGUI {

    private final HHEvents plugin;
    private final String eventType;
    private final String rarity;
    private final int page;

    public LootEditorMenu(HHEvents plugin, String eventType, String rarity, int page) {
        super(54, ColorUtil.color("&8Редактор лута: " + rarity));
        this.plugin = plugin;
        this.eventType = eventType;
        this.rarity = rarity;
        this.page = page;

        setupMenu();
    }

    private void setupMenu() {
        List<LootManager.LootItem> items = plugin.getLootManager().getLootItems(eventType, rarity);

        int maxItemsPerPage = 45;
        int startIndex = page * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            LootManager.LootItem li = items.get(i);
            ItemStack displayItem = new ItemBuilder(li.getItem())
                    .addLore("&7Шанс (Вес): &a" + li.getWeight())
                    .addLore("&eКлик - Изменить вес / Удалить")
                    .build();
            inventory.setItem(i - startIndex, displayItem);
        }

        if (page > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW).name("&eПредыдущая страница").build());
        }
        if (items.size() > endIndex) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW).name("&eСледующая страница").build());
        }

        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("&cНазад").build());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR && event.getClickedInventory() == inventory && event.getSlot() < 45) {
                plugin.getLootManager().addLootItem(eventType, rarity, cursor.clone(), 10);
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "Предмет добавлен!"));
                player.setItemOnCursor(null);
                setupMenu();
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);

        if (event.getSlot() == 45 && clicked.getType() == Material.ARROW) {
            new LootEditorMenu(plugin, eventType, rarity, page - 1).open(player);
        } else if (event.getSlot() == 53 && clicked.getType() == Material.ARROW) {
            new LootEditorMenu(plugin, eventType, rarity, page + 1).open(player);
        } else if (event.getSlot() == 49 && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (event.getSlot() < 45) {
            List<LootManager.LootItem> items = plugin.getLootManager().getLootItems(eventType, rarity);
            int index = page * 45 + event.getSlot();
            if (index < items.size()) {
                new WeightEditorMenu(plugin, eventType, rarity, items.get(index)).open(player);
            }
        }
    }
}
