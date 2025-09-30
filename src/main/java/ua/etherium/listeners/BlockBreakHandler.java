package ua.etherium.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.enchants.types.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class BlockBreakHandler implements Listener {

    private final AtheriumEnchants plugin;

    public BlockBreakHandler(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.isSneaking()) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) {
            return;
        }

        // --- Wisdom Enchant ---
        WisdomEnchant wisdom = (WisdomEnchant) plugin.getEnchantManager().getEnchant("wisdom");
        int wisdomLevel = CustomEnchant.getEnchantLevel(tool, wisdom);
        if (wisdomLevel > 0) {
            int bonusXp = wisdom.getBonusExperience(wisdomLevel);
            if (bonusXp > 0) {
                event.setExpToDrop(event.getExpToDrop() + bonusXp);
            }
        }

        // --- Telekinesis & Auto-Smelt ---
        handleDrops(player, tool, event);

        // --- Blast Mining & Timber ---
        handleAreaOfEffect(player, tool, event.getBlock());
    }

    private void handleAreaOfEffect(Player player, ItemStack tool, Block initialBlock) {
        // --- Timber Enchant ---
        TimberEnchant timber = (TimberEnchant) plugin.getEnchantManager().getEnchant("timber");
        int timberLevel = CustomEnchant.getEnchantLevel(tool, timber);
        if (timberLevel > 0) {
            Set<Block> treeBlocks = timber.getTreeBlocks(initialBlock, player, timberLevel);
            if (treeBlocks.size() > 1) { // More than just the initial block
                breakBlocks(player, tool, treeBlocks, initialBlock);
                return; // Don't run Blast Mining if Timber activated
            }
        }

        // --- Blast Mining Enchant ---
        BlastMiningEnchant blastMining = (BlastMiningEnchant) plugin.getEnchantManager().getEnchant("blast_mining");
        int blastMiningLevel = CustomEnchant.getEnchantLevel(tool, blastMining);
        if (blastMiningLevel > 0) {
            Set<Block> blocksToBreak = blastMining.getBlocks(initialBlock, player, blastMiningLevel);
            breakBlocks(player, tool, blocksToBreak, initialBlock);
        }
    }

    private void breakBlocks(Player player, ItemStack tool, Set<Block> blocks, Block initialBlock) {
        for (Block block : blocks) {
            if (block.equals(initialBlock)) continue; // Don't re-break the initial block
            if (player.breakBlock(block)) {
                // If we want to handle drops for these blocks too
                 handleDrops(player, tool, block);
            }
        }
    }

    private void handleDrops(Player player, ItemStack tool, BlockBreakEvent event) {
        // This method handles the drops for the *original* event block
        event.setDropItems(false); // We will handle drops manually
        Collection<ItemStack> drops = event.getBlock().getDrops(tool);
        processAndGiveDrops(player, tool, drops);
    }

    private void handleDrops(Player player, ItemStack tool, Block block) {
        // This overload handles drops for *additional* blocks broken by AoE effects
        Collection<ItemStack> drops = block.getDrops(tool);
        processAndGiveDrops(player, tool, drops);
    }

    private void processAndGiveDrops(Player player, ItemStack tool, Collection<ItemStack> drops) {
        if (drops.isEmpty()) return;

        // --- Infernal Touch (Auto-Smelt) ---
        InfernalTouchEnchant infernalTouch = (InfernalTouchEnchant) plugin.getEnchantManager().getEnchant("infernal_touch");
        int infernalLevel = CustomEnchant.getEnchantLevel(tool, infernalTouch);
        if (infernalLevel > 0) {
            drops = infernalTouch.smeltDrops(drops);
        }

        // --- Telekinesis (Auto-Pickup) ---
        TelekinesisEnchant telekinesis = (TelekinesisEnchant) plugin.getEnchantManager().getEnchant("telekinesis");
        int telekinesisLevel = CustomEnchant.getEnchantLevel(tool, telekinesis);
        if (telekinesisLevel > 0) {
            Map<Integer, ItemStack> leftover = telekinesis.addItemsToInventory(player, drops);
            // Drop any items that didn't fit in the inventory
            for (ItemStack leftoverItem : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }
        } else {
            // If no telekinesis, drop items at the block's location
            for (ItemStack drop : drops) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
    }
}