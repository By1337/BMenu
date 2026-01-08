package dev.by1337.bmenu.item.item;

import dev.by1337.bmenu.item.component.ArmorTrimComponent;
import dev.by1337.bmenu.item.component.CustomModelDataComponent;
import dev.by1337.bmenu.item.component.EnchantmentData;
import dev.by1337.bmenu.util.DataInt;
import dev.by1337.bmenu.util.DataString;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Color;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface ItemModel {

    static ItemModel ofMaterial(String material) {
        if (material.equalsIgnoreCase("air")) return ItemModelImpl.AIR;
        return new ItemModelImpl(new DataString(material));
    }

    @Nullable ComponentLike name();

    boolean hasLore();

    void forEachLore(Consumer<ComponentLike> consumer);

    @NotNull DataInt amount();

    @NotNull DataString material();

    @Nullable CustomModelDataComponent customModelData();

    boolean hasItemFlags();

    void forEachItemFlags(Consumer<ItemFlag> consumer);

    boolean hasPotionEffects();

    void forEachPotionEffects(Consumer<PotionEffect> consumer);

    @Nullable Color color();

    boolean hasEnchantments();

    void forEachEnchantments(Consumer<EnchantmentData> consumer);

    @NotNull DataInt damage();

    boolean hideTooltip();

    boolean unbreakable();
    @Nullable Integer getMaxStackSize();

    @Nullable Object getCache();
    void setCache(Object cache);

    @Nullable ArmorTrimComponent getArmorTrim();

    default ItemModel and(ItemModel i) {
        ItemModel i1 = this;
        return new ItemModel() {
            private Object cache;

            @Override
            public @Nullable Object getCache() {
                return cache;
            }

            @Override
            public void setCache(Object cache) {
                this.cache = cache;
            }

            @Override
            public @Nullable ArmorTrimComponent getArmorTrim() {
                return i1.getArmorTrim();
            }

            private <T> T any(T t, T t1) {
                return t == null ? t1 : t;
            }

            @Override
            public @Nullable ComponentLike name() {
                return any(i.name(), i1.name());
            }

            @Override
            public boolean hasLore() {
                return i.hasLore() || i1.hasLore();
            }

            @Override
            public void forEachLore(Consumer<ComponentLike> consumer) {
                i.forEachLore(consumer);
                i1.forEachLore(consumer);
            }

            @Override
            public @NotNull DataInt amount() {
                return i.amount();
            }

            @Override
            public @NotNull DataString material() {
                return i.material();
            }

            @Override
            public @Nullable CustomModelDataComponent customModelData() {
                return i.customModelData();
            }

            @Override
            public boolean hasItemFlags() {
                return i.hasItemFlags() || i1.hasItemFlags();
            }

            @Override
            public void forEachItemFlags(Consumer<ItemFlag> consumer) {
                i.forEachItemFlags(consumer);
                i1.forEachItemFlags(consumer);
            }

            @Override
            public boolean hasPotionEffects() {
                return i.hasPotionEffects() || i1.hasPotionEffects();
            }

            @Override
            public void forEachPotionEffects(Consumer<PotionEffect> consumer) {
                i.forEachPotionEffects(consumer);
                i1.forEachPotionEffects(consumer);
            }

            @Override
            public @Nullable Color color() {
                return any(i.color(), i1.color());
            }

            @Override
            public boolean hasEnchantments() {
                return i.hasEnchantments() || i1.hasEnchantments();
            }

            @Override
            public void forEachEnchantments(Consumer<EnchantmentData> consumer) {
                i.forEachEnchantments(consumer);
                i1.forEachEnchantments(consumer);
            }

            @Override
            public @NotNull DataInt damage() {
                return i1.damage();
            }

            @Override
            public boolean hideTooltip() {
                return i.hideTooltip() || i1.hideTooltip();
            }

            @Override
            public boolean unbreakable() {
                return i1.unbreakable();
            }

            @Override
            public @Nullable Integer getMaxStackSize() {
                return i1.getMaxStackSize();
            }
        };
    }

}
