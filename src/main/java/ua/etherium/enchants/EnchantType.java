package ua.etherium.enchants;

/**
 * Represents the type of item an enchantment can be applied to.
 * This helps in categorizing enchantments for GUI menus and logic checks.
 */
public enum EnchantType {
    /** Enchantments for swords, axes (as weapons). */
    WEAPON,
    /** Enchantments for pickaxes, shovels, axes (as tools), etc. */
    TOOL,
    /** Enchantments for helmets, chestplates, leggings, and boots. */
    ARMOR,
    /** Enchantments specifically for bows and crossbows. */
    BOW,
    /** Enchantments that can apply to a wide variety of items. */
    UNIVERSAL
}