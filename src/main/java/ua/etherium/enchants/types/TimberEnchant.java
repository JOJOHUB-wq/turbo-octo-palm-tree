package ua.etherium.enchants.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ua.etherium.enchants.CustomEnchant;

import java.util.HashSet;
import java.util.Set;

public class TimberEnchant extends CustomEnchant {

    public TimberEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    public Set<Block> getTreeBlocks(Block startBlock, Player player, int level) {
        Set<Block> treeBlocks = new HashSet<>();
        Set<Block> toCheck = new HashSet<>();

        int maxBlocks = config.getInt(key + ".levels." + level + ".max_blocks", 40);

        if (!isLog(startBlock)) {
            return treeBlocks;
        }

        toCheck.add(startBlock);

        while (!toCheck.isEmpty() && treeBlocks.size() < maxBlocks) {
            Block current = toCheck.iterator().next();
            toCheck.remove(current);
            treeBlocks.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block relative = current.getRelative(x, y, z);
                        if (isLog(relative) && !treeBlocks.contains(relative) && !toCheck.contains(relative)) {
                            toCheck.add(relative);
                        }
                    }
                }
            }
        }

        // Also gather leaves connected to the logs
        Set<Block> leaves = new HashSet<>();
        for(Block log : treeBlocks) {
            for (int x = -4; x <= 4; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -4; z <= 4; z++) {
                         Block relative = log.getRelative(x, y, z);
                         if (isLeaves(relative) && leaves.size() < 200) { // Limit leaves to prevent lag
                             leaves.add(relative);
                         }
                    }
                }
            }
        }
        treeBlocks.addAll(leaves);

        return treeBlocks;
    }

    private boolean isLog(Block block) {
        String typeName = block.getType().name();
        return typeName.endsWith("_LOG") || typeName.endsWith("_STEM");
    }

    private boolean isLeaves(Block block) {
        return block.getType().name().endsWith("_LEAVES");
    }
}