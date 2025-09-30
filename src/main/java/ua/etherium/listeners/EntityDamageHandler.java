package ua.etherium.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.etherium.AtheriumEnchants;
import ua.etherium.enchants.CustomEnchant;
import ua.etherium.enchants.types.*;
import ua.etherium.managers.ConfigManager;
import ua.etherium.managers.CooldownManager;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EntityDamageHandler implements Listener {

    private final AtheriumEnchants plugin;
    private final CooldownManager cooldownManager;
    private final ConfigManager configManager;

    public EntityDamageHandler(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.cooldownManager = plugin.getCooldownManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity victim = (LivingEntity) event.getEntity();
        Player attacker = getAttacker(event);

        if (attacker == null) return;

        if (victim instanceof Player) {
            if (handleVictimEnchants((Player) victim, attacker, event)) {
                return;
            }
        }

        handleAttackerEnchants(attacker, victim, event);
    }

    private Player getAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                return (Player) arrow.getShooter();
            }
        }
        return null;
    }

    private boolean handleVictimEnchants(Player victim, Player attacker, EntityDamageByEntityEvent event) {
        for (ItemStack armor : victim.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().isAir()) continue;

            DodgeEnchant dodge = (DodgeEnchant) plugin.getEnchantManager().getEnchant("dodge");
            int dodgeLevel = CustomEnchant.getEnchantLevel(armor, dodge);
            if (dodge != null && dodgeLevel > 0) {
                if (tryDodge(victim, dodge, dodgeLevel)) {
                    event.setCancelled(true);
                    return true;
                }
            }
        }

        ItemStack chestplate = victim.getInventory().getChestplate();
        if (chestplate != null) {
            PoisonThornsEnchant poisonThorns = (PoisonThornsEnchant) plugin.getEnchantManager().getEnchant("poison_thorns");
            int thornsLevel = CustomEnchant.getEnchantLevel(chestplate, poisonThorns);
            if (poisonThorns != null && thornsLevel > 0) {
                applyPoisonThorns(victim, attacker, poisonThorns, thornsLevel);
            }
        }

        return false;
    }

    private void handleAttackerEnchants(Player attacker, LivingEntity victim, EntityDamageByEntityEvent event) {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType().isAir()) return;

        if (event.getDamager() instanceof Arrow) {
            handleRangedEnchants(attacker, victim, weapon, event);
        } else {
            if (victim instanceof Player) {
                handleMeleeEnchants(attacker, (Player) victim, weapon);
            }
        }
    }

    private void handleRangedEnchants(Player attacker, LivingEntity victim, ItemStack bow, EntityDamageByEntityEvent event) {
        SniperEnchant sniper = (SniperEnchant) plugin.getEnchantManager().getEnchant("sniper");
        int sniperLevel = CustomEnchant.getEnchantLevel(bow, sniper);
        if (sniper != null && sniperLevel > 0) {
            applySniper(attacker, victim, sniper, sniperLevel, event);
        }

        ExplosiveEnchant explosive = (ExplosiveEnchant) plugin.getEnchantManager().getEnchant("explosive");
        int explosiveLevel = CustomEnchant.getEnchantLevel(bow, explosive);
        if (explosive != null && explosiveLevel > 0) {
            applyExplosive(attacker, victim, explosive, explosiveLevel);
        }
    }

    private void handleMeleeEnchants(Player attacker, Player victim, ItemStack weapon) {
        Random random = ThreadLocalRandom.current();

        OxidationEnchant oxidation = (OxidationEnchant) plugin.getEnchantManager().getEnchant("oxidation");
        int oxidationLevel = CustomEnchant.getEnchantLevel(weapon, oxidation);
        if (oxidation != null && oxidationLevel > 0) {
            applyOxidation(attacker, victim, oxidation, oxidationLevel);
        }

        VampirismEnchant vampirism = (VampirismEnchant) plugin.getEnchantManager().getEnchant("vampirism");
        int vampirismLevel = CustomEnchant.getEnchantLevel(weapon, vampirism);
        if (vampirism != null && vampirismLevel > 0) {
            applyVampirism(attacker, vampirism, vampirismLevel);
        }

        ToxicEnchant toxic = (ToxicEnchant) plugin.getEnchantManager().getEnchant("toxic");
        int toxicLevel = CustomEnchant.getEnchantLevel(weapon, toxic);
        if (toxic != null && toxicLevel > 0) {
            applyToxic(attacker, victim, toxic, toxicLevel);
        }

        IdentifyEnchant identify = (IdentifyEnchant) plugin.getEnchantManager().getEnchant("identify");
        int identifyLevel = CustomEnchant.getEnchantLevel(weapon, identify);
        if (identify != null && identifyLevel > 0) {
            applyIdentify(attacker, victim, identify, identifyLevel);
        }
    }

    private boolean tryDodge(Player victim, DodgeEnchant enchant, int level) {
        if (cooldownManager.isOnCooldown(victim, enchant.getKey())) return false;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (victim.isSneaking()) {
            chance += configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".sneak_bonus");
        }

        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");
            cooldownManager.setCooldown(victim, enchant.getKey(), cooldown);
            return true;
        }
        return false;
    }

    private void applyPoisonThorns(Player victim, Player attacker, PoisonThornsEnchant enchant, int level) {
        if (cooldownManager.isOnCooldown(victim, enchant.getKey())) return;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            int duration = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".duration");
            int amplifier = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".amplifier", 1) - 1;
            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");

            attacker.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
            cooldownManager.setCooldown(victim, enchant.getKey(), cooldown);
        }
    }

    private void applySniper(Player attacker, LivingEntity victim, SniperEnchant enchant, int level, EntityDamageByEntityEvent event) {
        if (cooldownManager.isOnCooldown(attacker, enchant.getKey())) return;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            double distance = attacker.getLocation().distance(victim.getLocation());
            double damagePerBlock = configManager.getEnchantsConfig().getDouble(enchant.getKey() + ".levels." + level + ".damage_per_block");
            double bonusDamage = distance * damagePerBlock;
            event.setDamage(event.getDamage() + bonusDamage);

            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");
            cooldownManager.setCooldown(attacker, enchant.getKey(), cooldown);
        }
    }

    private void applyExplosive(Player attacker, LivingEntity victim, ExplosiveEnchant enchant, int level) {
        if (cooldownManager.isOnCooldown(attacker, enchant.getKey())) return;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            float power = (float) configManager.getEnchantsConfig().getDouble(enchant.getKey() + ".levels." + level + ".power");
            victim.getWorld().createExplosion(victim.getLocation(), power, false, false, attacker);

            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");
            cooldownManager.setCooldown(attacker, enchant.getKey(), cooldown);
        }
    }

    private void applyOxidation(Player attacker, Player victim, OxidationEnchant enchant, int level) {
        if (cooldownManager.isOnCooldown(attacker, enchant.getKey())) return;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            int damage = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".damage_armor");
            for (ItemStack armor : victim.getInventory().getArmorContents()) {
                if (armor != null && armor.getItemMeta() instanceof Damageable) {
                    Damageable meta = (Damageable) armor.getItemMeta();
                    meta.setDamage(meta.getDamage() + damage);
                    if (meta.getDamage() < armor.getType().getMaxDurability()) {
                        armor.setItemMeta((ItemMeta) meta);
                    }
                }
            }
            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");
            cooldownManager.setCooldown(attacker, enchant.getKey(), cooldown);
        }
    }

    private void applyVampirism(Player attacker, VampirismEnchant enchant, int level) {
        if (cooldownManager.isOnCooldown(attacker, enchant.getKey())) return;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            int duration = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".duration");
            int amplifier = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".amplifier", 1) - 1;
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier));

            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");
            cooldownManager.setCooldown(attacker, enchant.getKey(), cooldown);
        }
    }

    private void applyToxic(Player attacker, Player victim, ToxicEnchant enchant, int level) {
        if (cooldownManager.isOnCooldown(attacker, enchant.getKey())) return;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            int duration = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".duration");
            int amplifier = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".amplifier", 1) - 1;
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));

            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");
            cooldownManager.setCooldown(attacker, enchant.getKey(), cooldown);
        }
    }

    private void applyIdentify(Player attacker, Player victim, IdentifyEnchant enchant, int level) {
        if (cooldownManager.isOnCooldown(attacker, enchant.getKey())) return;

        int chance = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".chance");
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            int duration = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".duration");
            victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0));

            int cooldown = configManager.getEnchantsConfig().getInt(enchant.getKey() + ".levels." + level + ".cooldown");
            cooldownManager.setCooldown(attacker, enchant.getKey(), cooldown);
        }
    }
}