package ua.etherium.enchants.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;

import java.util.Random;

public class OxidationEnchant extends CustomEnchant {

    private final Random random = new Random();

    public OxidationEnchant(String key, FileConfiguration config) {
        super(key, config);
    }

    public void applyEffect(Player attacker, Player victim, int level) {
        if (AtheriumEnchants.getInstance().getCooldownManager().isOnCooldown(attacker, key)) {
            return;
        }

        int chance = config.getInt(key + ".levels." + level + ".chance", 0);
        if (random.nextInt(100) < chance) {
            ItemStack[] armor = victim.getInventory().getArmorContents();
            boolean damaged = false;
            for (ItemStack armorPiece : armor) {
                if (armorPiece != null && !armorPiece.getType().isAir()) {
                    ItemMeta meta = armorPiece.getItemMeta();
                    if (meta instanceof Damageable) {
                        Damageable damageable = (Damageable) meta;
                        int damage = config.getInt(key + ".levels." + level + ".damage_armor", 1);

                        // Prevent breaking the item
                        if (damageable.getDamage() + damage < armorPiece.getType().getMaxDurability()) {
                            damageable.setDamage(damageable.getDamage() + damage);
                            armorPiece.setItemMeta(meta);
                            damaged = true;
                        }
                    }
                }
            }
            if (damaged) {
                int cooldown = config.getInt(key + ".levels." + level + ".cooldown", 0);
                AtheriumEnchants.getInstance().getCooldownManager().setCooldown(attacker, key, cooldown);
            }
        }
    }
}