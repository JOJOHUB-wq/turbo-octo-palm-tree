package ua.etherium.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.enchants.types.*;

public class EntityDamageHandler implements Listener {

    private final AtheriumEnchants plugin;

    public EntityDamageHandler(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();
        Player attacker = null;

        // Determine the attacker
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            }
        }

        if (attacker == null) {
            return;
        }

        // --- Handle Victim's Enchantments (Dodge) ---
        if (victim instanceof Player) {
            Player victimPlayer = (Player) victim;
            for (ItemStack armor : victimPlayer.getInventory().getArmorContents()) {
                if (armor != null) {
                    DodgeEnchant dodge = (DodgeEnchant) plugin.getEnchantManager().getEnchant("dodge");
                    int dodgeLevel = CustomEnchant.getEnchantLevel(armor, dodge);
                    if (dodgeLevel > 0) {
                        if (dodge.tryToDodge(victimPlayer, dodgeLevel)) {
                            event.setCancelled(true);
                            // Maybe send a message or play a sound
                            // victimPlayer.sendMessage(ColorUtils.color("&aYou dodged the attack!"));
                            // attacker.sendMessage(ColorUtils.color("&cYour opponent dodged!"));
                            return; // Stop processing if dodged
                        }
                    }
                }
            }
        }

        // --- Handle Attacker's Enchantments ---
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType().isAir()) return;

        // --- Sniper Enchant (for bows/crossbows) ---
        if (event.getDamager() instanceof Arrow) {
            SniperEnchant sniper = (SniperEnchant) plugin.getEnchantManager().getEnchant("sniper");
            int sniperLevel = CustomEnchant.getEnchantLevel(weapon, sniper);
            if (sniperLevel > 0) {
                double distance = attacker.getLocation().distance(victim.getLocation());
                double bonusDamage = sniper.getBonusDamage(attacker, distance, sniperLevel);
                event.setDamage(event.getDamage() + bonusDamage);
            }
        }

        // --- Melee Weapon Enchants ---
        if (victim instanceof Player) {
            Player victimPlayer = (Player) victim;

            // Oxidation
            OxidationEnchant oxidation = (OxidationEnchant) plugin.getEnchantManager().getEnchant("oxidation");
            int oxidationLevel = CustomEnchant.getEnchantLevel(weapon, oxidation);
            if(oxidationLevel > 0) {
                oxidation.applyEffect(attacker, victimPlayer, oxidationLevel);
            }

            // Vampirism
            VampirismEnchant vampirism = (VampirismEnchant) plugin.getEnchantManager().getEnchant("vampirism");
            int vampirismLevel = CustomEnchant.getEnchantLevel(weapon, vampirism);
            if(vampirismLevel > 0) {
                vampirism.applyEffect(attacker, vampirismLevel);
            }

            // Toxic
            ToxicEnchant toxic = (ToxicEnchant) plugin.getEnchantManager().getEnchant("toxic");
            int toxicLevel = CustomEnchant.getEnchantLevel(weapon, toxic);
            if(toxicLevel > 0) {
                toxic.applyEffect(attacker, victimPlayer, toxicLevel);
            }

            // Identify
            IdentifyEnchant identify = (IdentifyEnchant) plugin.getEnchantManager().getEnchant("identify");
            int identifyLevel = CustomEnchant.getEnchantLevel(weapon, identify);
            if(identifyLevel > 0) {
                identify.applyEffect(attacker, victimPlayer, identifyLevel);
            }
        }
    }
}