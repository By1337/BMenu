package dev.by1337.item.component;

import org.bukkit.enchantments.Enchantment;

import java.util.Objects;

public record EnchantmentData(Enchantment enchantment, int lvl) {
    public EnchantmentData {
        Objects.requireNonNull(enchantment, "enchantment");
    }
}
