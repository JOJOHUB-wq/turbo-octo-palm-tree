package ua.etherium.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.enchants.types.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BlockBreakHandler implements Listener {

    private final AtheriumEnchants plugin;
    private final Set<Block> brokenByEnchant = new HashSet<>();

    public BlockBreakHandler(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (player.getGameMode() == GameMode.CREATIVE || tool.getType().isAir()) {
            return;
        }

        if (brokenByEnchant.contains(event.getBlock())) {
            brokenByEnchant.remove(event.getBlock());
            return;
        }

        Set<Block> blocksToBreak = new HashSet<>();
        Block startBlock = event.getBlock();
        boolean isTimber = false;

        TimberEnchant timber = (TimberEnchant) plugin.getEnchantManager().getEnchant("timber");
        if (timber != null && CustomEnchant.getEnchantLevel(tool, timber) > 0 && isLog(startBlock.getType())) {
            blocksToBreak.addAll(getTree(startBlock, timber.getMaxLevel()));
            if (blocksToBreak.size() > 1) {
                isTimber = true;
            }
        }

        if (blocksToBreak.isEmpty()) {
            BlastMiningEnchant blastMining = (BlastMiningEnchant) plugin.getEnchantManager().getEnchant("blast_mining");
            int blastMiningLevel = CustomEnchant.getEnchantLevel(tool, blastMining);
            if (blastMining != null && blastMiningLevel > 0) {
                blocksToBreak.addAll(getBlastMineBlocks(startBlock, player, blastMiningLevel));
            }
        }

        event.setDropItems(false);
        processBlockDrops(startBlock, player, tool);

        if (!blocksToBreak.isEmpty()) {
            final boolean finalIsTimber = isTimber;
            long delay = plugin.getConfigManager().getEnchantsConfig().getLong("timber.levels.1.animation_delay_ticks", 1);

            List<Block> sortedBlocks = new ArrayList<>(blocksToBreak);
            if (finalIsTimber) {
                sortedBlocks.sort(Comparator.comparingInt(Block::getY).reversed());
            }

            for (int i = 0; i < sortedBlocks.size(); i++) {
                Block block = sortedBlocks.get(i);
                if (block.equals(startBlock)) continue;

                if (finalIsTimber) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> breakSingleBlock(block, player, tool), i * delay);
                } else {
                    breakSingleBlock(block, player, tool);
                }
            }
        }
    }

    private void breakSingleBlock(Block block, Player player, ItemStack tool) {
        if (block.isLiquid() || block.getType().isAir()) return;

        brokenByEnchant.add(block);
        processBlockDrops(block, player, tool);
        block.breakNaturally(tool, true);
        applyDurability(tool, player);
    }

    private void processBlockDrops(Block block, Player player, ItemStack tool) {
        Collection<ItemStack> drops = block.getDrops(tool);

        InfernalTouchEnchant infernalTouch = (InfernalTouchEnchant) plugin.getEnchantManager().getEnchant("infernal_touch");
        int infernalLevel = CustomEnchant.getEnchantLevel(tool, infernalTouch);
        if (infernalTouch != null && infernalLevel > 0) {
            drops = smeltDrops(drops);
        }

        TelekinesisEnchant telekinesis = (TelekinesisEnchant) plugin.getEnchantManager().getEnchant("telekinesis");
        int telekinesisLevel = CustomEnchant.getEnchantLevel(tool, telekinesis);
        if (telekinesis != null && telekinesisLevel > 0) {
            for (ItemStack leftover : player.getInventory().addItem(drops.toArray(new ItemStack[0])).values()) {
                player.getWorld().dropItemNaturally(block.getLocation(), leftover);
            }
        } else {
            for (ItemStack drop : drops) {
                player.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }

        WisdomEnchant wisdom = (WisdomEnchant) plugin.getEnchantManager().getEnchant("wisdom");
        int wisdomLevel = CustomEnchant.getEnchantLevel(tool, wisdom);
        if (wisdom != null && wisdomLevel > 0) {
            int chance = plugin.getConfigManager().getEnchantsConfig().getInt("wisdom.levels." + wisdomLevel + ".chance", 0);
            if (ThreadLocalRandom.current().nextInt(100) < chance) {
                int min = plugin.getConfigManager().getEnchantsConfig().getInt("wisdom.levels." + wisdomLevel + ".min_exp", 0);
                int max = plugin.getConfigManager().getEnchantsConfig().getInt("wisdom.levels." + wisdomLevel + ".max_exp", 0);
                if (max > min) {
                    player.giveExp(ThreadLocalRandom.current().nextInt(min, max + 1));
                }
            }
        }
    }

    private void applyDurability(ItemStack tool, Player player) {
        if (tool.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) tool.getItemMeta();
            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
            if (ThreadLocalRandom.current().nextInt(100) < (100 / (unbreakingLevel + 1))) {
                damageable.setDamage(damageable.getDamage() + 1);
                if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), "entity.item.break", 1.0F, 1.0F);
                } else {
                    tool.setItemMeta((ItemMeta) damageable);
                }
            }
        }
    }

    private Collection<ItemStack> smeltDrops(Collection<ItemStack> drops) {
        FileConfiguration mainConfig = AtheriumEnchants.getInstance().getConfig();
        List<ItemStack> smeltedDrops = new ArrayList<>();
        for (ItemStack drop : drops) {
            String smeltedMaterialName = mainConfig.getString("infernal_touch_smelt_map." + drop.getType().name());
            if (smeltedMaterialName != null) {
                try {
                    Material smeltedMaterial = Material.valueOf(smeltedMaterialName);
                    smeltedDrops.add(new ItemStack(smeltedMaterial, drop.getAmount()));
                } catch (IllegalArgumentException ignored) {
                    smeltedDrops.add(drop);
                }
            } else {
                smeltedDrops.add(drop);
            }
        }
        return smeltedDrops;
    }

    private Set<Block> getBlastMineBlocks(Block center, Player player, int level) {
        Set<Block> blocks = new HashSet<>();
        FileConfiguration enchantsConfig = plugin.getConfigManager().getEnchantsConfig();
        int radiusX = enchantsConfig.getInt("blast_mining.levels." + level + ".radius_x", 1);
        int radiusY = enchantsConfig.getInt("blast_mining.levels." + level + ".radius_y", 1);
        int radiusZ = enchantsConfig.getInt("blast_mining.levels." + level + ".radius_z", 1);
        Location loc = player.getLocation();

        if (loc.getPitch() > 45) {
            for (int x = -radiusX / 2; x <= radiusX / 2; x++) {
                for (int z = -radiusZ / 2; z <= radiusZ / 2; z++) {
                    for (int y = 0; y < radiusY; y++) blocks.add(center.getRelative(x, -y, z));
                }
            }
        } else if (loc.getPitch() < -45) {
            for (int x = -radiusX / 2; x <= radiusX / 2; x++) {
                for (int z = -radiusZ / 2; z <= radiusZ / 2; z++) {
                    for (int y = 0; y < radiusY; y++) blocks.add(center.getRelative(x, y, z));
                }
            }
        } else {
            BlockFace facing = player.getFacing();
            if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
                for (int x = -radiusX / 2; x <= radiusX / 2; x++) {
                    for (int y = -radiusY / 2; y <= radiusY / 2; y++) {
                        for (int z = 0; z < radiusZ; z++) blocks.add(center.getRelative(x, y, facing == BlockFace.NORTH ? -z : z));
                    }
                }
            } else if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
                for (int z = -radiusZ / 2; z <= radiusZ / 2; z++) {
                    for (int y = -radiusY / 2; y <= radiusY / 2; y++) {
                        for (int x = 0; x < radiusX; x++) blocks.add(center.getRelative(facing == BlockFace.WEST ? -x : x, y, z));
                    }
                }
            }
        }
        return blocks;
    }

    private Set<Block> getTree(Block start, int maxBlocks) {
        Set<Block> tree = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        toCheck.add(start);

        while (!toCheck.isEmpty() && tree.size() < maxBlocks) {
            Block current = toCheck.poll();
            if (!isLog(current.getType()) || tree.contains(current)) continue;
            tree.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        toCheck.add(current.getRelative(x, y, z));
                    }
                }
            }
        }
        return tree;
    }

    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_STEM") || material.name().endsWith("WOOD");
    }
}