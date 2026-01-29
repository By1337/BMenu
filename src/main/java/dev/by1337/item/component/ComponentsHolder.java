package dev.by1337.item.component;

import dev.by1337.item.ItemModel;
import dev.by1337.item.component.impl.*;
import dev.by1337.bmenu.util.holder.ColorHolder;
import dev.by1337.bmenu.util.holder.IntHolder;
import dev.by1337.bmenu.util.holder.StringHolder;
import dev.by1337.bmenu.util.holder.Holder;
import dev.by1337.core.ServerVersion;
import dev.by1337.item.ItemComponents;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentsHolder {
    private static final int INIT_SIZE = ItemComponents.count();
    private final Object[] components;

    public ComponentsHolder() {
        components = new Object[INIT_SIZE];
    }

    private ComponentsHolder(Object[] components) {
        this.components = components;
    }

    public boolean getBool(@Nullable BaseComponent<Boolean> type) {
        return get(type, false);
    }

    @Nullable
    public <T> T get(@Nullable BaseComponent<T> type) {
        if (type == null) return null;
        return (T) components[type.id()];
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(@Nullable BaseComponent<T> type, T def) {
        if (type == null) return def;
        var o = components[type.id()];
        return o == null ? def : (T) o;
    }

    public <T> void set(BaseComponent<T> type, T value) {
        if (type == null) return;
        components[type.id()] = value;
    }

    public ComponentsHolder copy() {
        return new ComponentsHolder(Arrays.copyOf(components, INIT_SIZE));
    }

    public ComponentsHolder merge(ComponentsHolder other) {
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
        return new ComponentsHolder(result);
    }

    private <T extends MergeableComponent<T>> T merge(T t, T t2) {
        return t.and(t2);
    }

    public static ComponentsHolder fromItemStack(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return new ComponentsHolder();
        ComponentsHolder result = new ComponentsHolder();
        var lore = im.lore();
        if (lore != null) {
            result.set(ItemComponents.LORE, new ItemLoreComponent(new ArrayList<>(lore)));
        }
        var name = im.displayName();
        if (name != null) {
            result.set(ItemComponents.NAME, name);
        }
        result.set(ItemComponents.AMOUNT, new IntHolder(Integer.toString(itemStack.getAmount())));
        if (im instanceof Damageable damageable) {
            result.set(ItemComponents.DAMAGE, new IntHolder(Integer.toString(damageable.getDamage())));
        }
        result.set(ItemComponents.MATERIAL, new StringHolder(itemStack.getType().name()));
        if (ServerVersion.is1_21_5orNewer()) {
            var v = im.getCustomModelDataComponent();
            if (v != null) {
                result.set(ItemComponents.MODEL_DATA, new CustomModelDataComponent(
                        v.getFloats(),
                        v.getFlags(),
                        v.getStrings(),
                        v.getColors()
                ));
            }
        } else if (im.hasCustomModelData()) {
            var v = im.getCustomModelData();
            result.set(ItemComponents.MODEL_DATA, new CustomModelDataComponent(
                    List.of(((Integer) v).floatValue()),
                    List.of(),
                    List.of(),
                    List.of()
            ));
        }
        if (im instanceof PotionMeta potionMeta){

            if (potionMeta.hasCustomEffects()){
                result.set(ItemComponents.POTION_CONTENTS, new PotionContentsComponent(potionMeta.getCustomEffects()));
            }
            if (potionMeta.hasColor()){
                result.set(ItemComponents.COLOR, ColorHolder.fromBukkit(potionMeta.getColor()));
            }
        }
        if (im instanceof LeatherArmorMeta m){
            result.set(ItemComponents.COLOR, ColorHolder.fromBukkit(m.getColor()));
        }
        {
            var map = im.getEnchants();
            if (!map.isEmpty()){
                result.set(ItemComponents.ENCHANTMENTS, EnchantmentsComponent.fromMap(map));
            }
        }
        if (im.isUnbreakable()){
            result.set(ItemComponents.UNBREAKABLE, true);
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
            result.set(ItemComponents.CONTAINER, new ContainerComponent(map));
        }
        if (ItemComponents.TRIM != null){
            if (im instanceof ArmorMeta armorMeta){
                var v = armorMeta.getTrim();
                if (v != null){
                    result.set(ItemComponents.TRIM, new ArmorTrimComponent(new Holder<>(v.getMaterial()), new Holder<>(v.getPattern())));
                }
            }
        }
        if (ServerVersion.is1_20_5orNewer()){
            if (im.isHideTooltip()){
                result.set(ItemComponents.HIDE_TOOLTIP, true);
            }
            if (im.getEnchantmentGlintOverride()){
                result.set(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
            if (im.hasMaxStackSize()){
                result.set(ItemComponents.MAX_STACK_SIZE, im.getMaxStackSize());
            }
            if (ServerVersion.is1_21_3orNewer()){
                if (im.isGlider()){
                    result.set(ItemComponents.GLIDER, true);
                }
            }
        }
        var set = im.getItemFlags();
        if (!set.isEmpty()){
            result.set(ItemComponents.HIDE_FLAGS, new HideFlagsComponents(set));
        }
        return result;
    }
}
