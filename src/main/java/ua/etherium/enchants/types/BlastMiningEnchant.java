package ua.etherium.enchants.types;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ua.etherium.enchants.CustomEnchant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlastMiningEnchant extends CustomEnchant {

    public BlastMiningEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    public Set<Block> getBlocks(Block center, Player player, int level) {
        Set<Block> blocks = new HashSet<>();

        double pitch = player.getLocation().getPitch();
        double yaw = player.getLocation().getYaw();

        int radiusX = config.getInt(key + ".levels." + level + ".radius_x", 1);
        int radiusY = config.getInt(key + ".levels." + level + ".radius_y", 1);
        int radiusZ = config.getInt(key + ".levels." + level + ".radius_z", 1);

        String toolType = getToolType(player.getInventory().getItemInMainHand());
        List<String> allowedBlocks = config.getStringList(key + ".levels." + level + ".blocks_" + toolType);

        if (pitch > 45) { // Looking down
            for (int x = -radiusX / 2; x <= radiusX / 2; x++) {
                for (int z = -radiusZ / 2; z <= radiusZ / 2; z++) {
                    for (int y = 0; y < radiusY; y++) {
                        Block block = center.getRelative(x, -y, z);
                        if (allowedBlocks.contains(block.getType().name())) {
                            blocks.add(block);
                        }
                    }
                }
            }
        } else if (pitch < -45) { // Looking up
            for (int x = -radiusX / 2; x <= radiusX / 2; x++) {
                for (int z = -radiusZ / 2; z <= radiusZ / 2; z++) {
                    for (int y = 0; y < radiusY; y++) {
                        Block block = center.getRelative(x, y, z);
                        if (allowedBlocks.contains(block.getType().name())) {
                            blocks.add(block);
                        }
                    }
                }
            }
        } else { // Looking forward
            BlockFace facing = getCardinalDirection(yaw);

            switch (facing) {
                case NORTH:
                case SOUTH:
                    for (int x = -radiusX / 2; x <= radiusX / 2; x++) {
                        for (int y = -radiusY / 2; y <= radiusY / 2; y++) {
                            for (int z = 0; z < radiusZ; z++) {
                                int offsetZ = facing == BlockFace.NORTH ? -z : z;
                                Block block = center.getRelative(x, y, offsetZ);
                                if (allowedBlocks.contains(block.getType().name())) {
                                    blocks.add(block);
                                }
                            }
                        }
                    }
                    break;

                case EAST:
                case WEST:
                    for (int z = -radiusZ / 2; z <= radiusZ / 2; z++) {
                        for (int y = -radiusY / 2; y <= radiusY / 2; y++) {
                            for (int x = 0; x < radiusX; x++) {
                                int offsetX = facing == BlockFace.WEST ? -x : x;
                                Block block = center.getRelative(offsetX, y, z);
                                if (allowedBlocks.contains(block.getType().name())) {
                                    blocks.add(block);
                                }
                            }
                        }
                    }
                    break;
                default:
                     break;
            }
        }

        return blocks;
    }

    private BlockFace getCardinalDirection(double yaw) {
        yaw = (yaw % 360 + 360) % 360; // Normalize yaw

        if (yaw >= 315 || yaw < 45) {
            return BlockFace.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return BlockFace.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return BlockFace.NORTH;
        } else { // 225 <= yaw < 315
            return BlockFace.EAST;
        }
    }

    private String getToolType(ItemStack tool) {
        if (tool == null) return "pickaxe";
        String name = tool.getType().name();
        if (name.contains("_PICKAXE")) return "pickaxe";
        if (name.contains("_SHOVEL") || name.contains("_SPADE")) return "shovel";
        if (name.contains("_AXE")) return "axe";
        return "pickaxe"; // Default
    }
}