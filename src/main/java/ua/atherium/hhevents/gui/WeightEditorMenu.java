package ua.atherium.hhevents.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.managers.LootManager;
import ua.atherium.hhevents.utils.ColorUtil;
import ua.atherium.hhevents.utils.ItemBuilder;

public class WeightEditorMenu extends BaseGUI {

    private final HHEvents plugin;
    private final String eventType;
    private final String rarity;
    private final LootManager.LootItem lootItem;
    private int currentWeight;

    public WeightEditorMenu(HHEvents plugin, String eventType, String rarity, LootManager.LootItem lootItem) {
        super(27, ColorUtil.color("&8Редактор веса предмета"));
        this.plugin = plugin;
        this.eventType = eventType;
        this.rarity = rarity;
        this.lootItem = lootItem;
        this.currentWeight = lootItem.getWeight();

        setupMenu();
    }

    private void setupMenu() {
        ItemStack centerItem = new ItemBuilder(lootItem.getItem())
                .addLore("&7Текущий вес (шанс): &a" + currentWeight)
                .build();
        inventory.setItem(13, centerItem);

        inventory.setItem(10, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("&a+1 к весу").build());
        inventory.setItem(11, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("&a+5 к весу").build());
        inventory.setItem(12, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("&a+10 к весу").build());

        inventory.setItem(14, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("&c-1 к весу").build());
        inventory.setItem(15, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("&c-5 к весу").build());
        inventory.setItem(16, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("&c-10 к весу").build());

        inventory.setItem(21, new ItemBuilder(Material.EMERALD).name("&aСохранить").build());
        inventory.setItem(22, new ItemBuilder(Material.BARRIER).name("&cУдалить предмет").build());
        inventory.setItem(23, new ItemBuilder(Material.ARROW).name("&eНазад").build());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (slot == 10) currentWeight += 1;
        else if (slot == 11) currentWeight += 5;
        else if (slot == 12) currentWeight += 10;
        else if (slot == 14) currentWeight -= 1;
        else if (slot == 15) currentWeight -= 5;
        else if (slot == 16) currentWeight -= 10;

        if (currentWeight < 1) currentWeight = 1;

        if (slot >= 10 && slot <= 16) {
            setupMenu();
        } else if (slot == 21) {
            plugin.getLootManager().updateLootItemWeight(eventType, rarity, lootItem.getId(), currentWeight);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "Вес предмета сохранен!"));
            new LootEditorMenu(plugin, eventType, rarity, 0).open(player);
        } else if (slot == 22) {
            plugin.getLootManager().removeLootItem(eventType, rarity, lootItem.getId());
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "Предмет удален!"));
            new LootEditorMenu(plugin, eventType, rarity, 0).open(player);
        } else if (slot == 23) {
            new LootEditorMenu(plugin, eventType, rarity, 0).open(player);
        }
    }
}
