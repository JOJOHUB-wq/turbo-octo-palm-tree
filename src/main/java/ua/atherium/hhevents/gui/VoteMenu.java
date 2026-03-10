package ua.atherium.hhevents.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.hhevents.HHEvents;
import ua.atherium.hhevents.events.VoteEvent;
import ua.atherium.hhevents.utils.ColorUtil;
import ua.atherium.hhevents.utils.ItemBuilder;

public class VoteMenu extends BaseGUI {

    private final HHEvents plugin;
    private final VoteEvent voteEvent;

    public VoteMenu(HHEvents plugin, VoteEvent voteEvent) {
        super(27, ColorUtil.color("&8Голосование за ивент"));
        this.plugin = plugin;
        this.voteEvent = voteEvent;

        setupMenu();
    }

    private void setupMenu() {
        inventory.setItem(11, new ItemBuilder(Material.ENDER_CHEST).name("&5Мистик").lore("&7Голосовать за Мистика", "&7Текущие голоса: &a" + voteEvent.getMysticVotes()).build());
        inventory.setItem(15, new ItemBuilder(Material.IRON_BLOCK).name("&7Голем").lore("&7Голосовать за Голема", "&7Текущие голоса: &a" + voteEvent.getGolemVotes()).build());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (slot == 11) {
            voteEvent.addVote(player, "mystic");
            player.closeInventory();
        } else if (slot == 15) {
            voteEvent.addVote(player, "golem");
            player.closeInventory();
        }
    }
}
