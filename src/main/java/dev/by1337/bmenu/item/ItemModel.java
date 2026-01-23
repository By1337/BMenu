package dev.by1337.bmenu.item;

import dev.by1337.bmenu.item.component.ItemDataComponent;
import dev.by1337.bmenu.item.component.ItemDataComponents;
import dev.by1337.bmenu.util.DataString;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ItemModel {
    public static final YamlCodec<ItemModel> CODEC;
    public static final ItemModel AIR;
    private final ItemComponents components;


    public static ItemModel fromItemStack(ItemStack itemStack){
        return new ItemModel(ItemComponents.fromItemStack(itemStack));
    }


    public ItemModel(ItemComponents components) {
        this.components = components;
    }

    public static ItemModel ofMaterial(String material) {
        if (material.equalsIgnoreCase("air")) return AIR;
        var result = new ItemModel(new ItemComponents());
        result.components.set(ItemDataComponents.MATERIAL, new DataString(material));
        return result;
    }

    public ItemModel and(ItemModel i) {
        return new ItemModel(components.merge(i.components));
    }

    public boolean getBool(@Nullable ItemDataComponent<Boolean> type) {
        return components.getBool(type);
    }

    @Nullable
    public <T> T get(@Nullable ItemDataComponent<T> type) {
        return components.get(type);
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(@Nullable ItemDataComponent<T> type, T def) {
        return components.get(type, def);
    }

    static {
        AIR = new ItemModel(new ItemComponents());
        AIR.components.set(ItemDataComponents.MATERIAL, new DataString("air"));
        CODEC = ItemDataComponents.COMPONENTS_CODEC.map(
                ItemModel::new,
                i -> i.components
        );
    }
}
