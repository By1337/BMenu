package dev.by1337.item;

import dev.by1337.item.component.BaseComponent;
import dev.by1337.item.component.ComponentsHolder;
import dev.by1337.bmenu.util.holder.StringHolder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ItemModel {
    public static final YamlCodec<ItemModel> CODEC;
    public static final ItemModel AIR;
    private final ComponentsHolder components;

    public static ItemModel fromItemStack(ItemStack itemStack){
        return new ItemModel(ComponentsHolder.fromItemStack(itemStack));
    }

    public ItemModel(ComponentsHolder components) {
        this.components = components;
    }

    public static ItemModel ofMaterial(String material) {
        if (material.equalsIgnoreCase("air")) return AIR;
        var result = new ItemModel(new ComponentsHolder());
        result.components.set(ItemComponents.MATERIAL, new StringHolder(material));
        return result;
    }

    public ItemModel and(ItemModel i) {
        return new ItemModel(components.merge(i.components));
    }

    public boolean getBool(@Nullable BaseComponent<Boolean> type) {
        return components.getBool(type);
    }

    @Nullable
    public <T> T get(@Nullable BaseComponent<T> type) {
        return components.get(type);
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(@Nullable BaseComponent<T> type, T def) {
        return components.get(type, def);
    }

    static {
        AIR = new ItemModel(new ComponentsHolder());
        AIR.components.set(ItemComponents.MATERIAL, new StringHolder("air"));
        CODEC = ItemComponents.COMPONENTS_CODEC.map(
                ItemModel::new,
                i -> i.components
        );
    }
}
