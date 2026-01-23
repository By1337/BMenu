package dev.by1337.bmenu.item;

import dev.by1337.bmenu.item.component.ItemDataComponent;
import dev.by1337.bmenu.item.component.ItemDataComponents;
import dev.by1337.bmenu.item.component.MergeableComponent;
import dev.by1337.bmenu.item.component.impl.*;
import dev.by1337.bmenu.util.ColorHolder;
import dev.by1337.bmenu.util.DataInt;
import dev.by1337.bmenu.util.DataString;
import dev.by1337.bmenu.util.Holder;
import dev.by1337.core.ServerVersion;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemComponents {
    private static final int INIT_SIZE = ItemDataComponents.count();
    private final Object[] components;

    public ItemComponents() {
        components = new Object[INIT_SIZE];
    }

    private ItemComponents(Object[] components) {
        this.components = components;
    }

    public boolean getBool(@Nullable ItemDataComponent<Boolean> type) {
        return get(type, false);
    }

    @Nullable
    public <T> T get(@Nullable ItemDataComponent<T> type) {
        if (type == null) return null;
        return (T) components[type.id()];
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(@Nullable ItemDataComponent<T> type, T def) {
        if (type == null) return def;
        var o = components[type.id()];
        return o == null ? def : (T) o;
    }

    public <T> void set(ItemDataComponent<T> type, T value) {
        if (type == null) return;
        components[type.id()] = value;
    }

    public ItemComponents copy() {
        return new ItemComponents(Arrays.copyOf(components, INIT_SIZE));
    }

    public ItemComponents merge(ItemComponents other) {
        Object[] result = Arrays.copyOf(components, INIT_SIZE);
        for (int i = 0; i < other.components.length; i++) {
            Object o = other.components[i];
            if (o != null) {
                Object current = components[i];
                if (current == null) {
                    result[i] = o;
                } else if (o instanceof MergeableComponent<?>) {
                    result[i] = merge((MergeableComponent) current, (MergeableComponent) o);
                }
            }
        }
        return new ItemComponents(result);
    }

    private <T extends MergeableComponent<T>> T merge(T t, T t2) {
        return t.and(t2);
    }

    public static ItemComponents fromItemStack(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return new ItemComponents();
        ItemComponents result = new ItemComponents();
        var lore = im.lore();
        if (lore != null) {
            result.set(ItemDataComponents.LORE, new ItemLoreComponent(new ArrayList<>(lore)));
        }
        var name = im.displayName();
        if (name != null) {
            result.set(ItemDataComponents.NAME, name);
        }
        result.set(ItemDataComponents.AMOUNT, new DataInt(Integer.toString(itemStack.getAmount())));
        if (im instanceof Damageable damageable) {
            result.set(ItemDataComponents.DAMAGE, new DataInt(Integer.toString(damageable.getDamage())));
        }
        result.set(ItemDataComponents.MATERIAL, new DataString(itemStack.getType().name()));
        if (ServerVersion.is1_21_5orNewer()) {
            var v = im.getCustomModelDataComponent();
            if (v != null) {
                result.set(ItemDataComponents.MODEL_DATA, new CustomModelDataComponent(
                        v.getFloats(),
                        v.getFlags(),
                        v.getStrings(),
                        v.getColors()
                ));
            }
        } else if (im.hasCustomModelData()) {
            var v = im.getCustomModelData();
            result.set(ItemDataComponents.MODEL_DATA, new CustomModelDataComponent(
                    List.of(((Integer) v).floatValue()),
                    List.of(),
                    List.of(),
                    List.of()
            ));
        }
        if (im instanceof PotionMeta potionMeta){
            if (potionMeta.hasCustomEffects()){
                result.set(ItemDataComponents.POTION_CONTENTS, new PotionContentsComponent(potionMeta.getCustomEffects()));
            }
            if (potionMeta.hasColor()){
                result.set(ItemDataComponents.COLOR, ColorHolder.fromBukkit(potionMeta.getColor()));
            }
        }
        if (im instanceof LeatherArmorMeta m){
            result.set(ItemDataComponents.COLOR, ColorHolder.fromBukkit(m.getColor()));
        }
        {
            var map = im.getEnchants();
            if (!map.isEmpty()){
                result.set(ItemDataComponents.ENCHANTMENTS, EnchantmentsComponent.fromMap(map));
            }
        }
        if (im.isUnbreakable()){
            result.set(ItemDataComponents.UNBREAKABLE, true);
        }
        if (im instanceof BlockStateMeta state && state instanceof Container container){
            Int2ObjectOpenHashMap<ItemModel> map = new Int2ObjectOpenHashMap<>();
            var inv = container.getInventory();
            var arr = inv.getStorageContents();
            for (int i = 0; i < arr.length; i++) {
                var item = arr[i];
                if (item == null || item.getType().isAir()) continue;
                map.put(i, ItemModel.fromItemStack(item));
            }
            result.set(ItemDataComponents.CONTAINER, new ContainerComponent(map));
        }
        if (ItemDataComponents.TRIM != null){
            if (im instanceof ArmorMeta armorMeta){
                var v = armorMeta.getTrim();
                if (v != null){
                    result.set(ItemDataComponents.TRIM, new ArmorTrimComponent(new Holder<>(v.getMaterial()), new Holder<>(v.getPattern())));
                }
            }
        }
        if (ServerVersion.is1_20_5orNewer()){
            if (im.isHideTooltip()){
                result.set(ItemDataComponents.HIDE_TOOLTIP, true);
            }
            if (im.getEnchantmentGlintOverride()){
                result.set(ItemDataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
            if (im.hasMaxStackSize()){
                result.set(ItemDataComponents.MAX_STACK_SIZE, im.getMaxStackSize());
            }
            if (ServerVersion.is1_21_3orNewer()){
                if (im.isGlider()){
                    result.set(ItemDataComponents.GLIDER, true);
                }
            }
        }
        var set = im.getItemFlags();
        if (!set.isEmpty()){
            result.set(ItemDataComponents.HIDE_FLAGS, new HideFlagsComponents(set));
        }
        return result;
    }
}
