package dev.by1337.bmenu.item;

import dev.by1337.bmenu.item.component.ItemDataComponent;
import dev.by1337.bmenu.item.component.ItemDataComponents;
import dev.by1337.bmenu.util.DataString;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ItemModel {
    public static final YamlCodec<ItemModel> CODEC;
    public static final ItemModel AIR;
    private final ItemComponents components;

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
        return null;
    }

    public boolean getBool(ItemDataComponent<Boolean> type) {
        return components.getBool(type);
    }

    @Nullable
    public <T> T get(ItemDataComponent<T> type) {
        return components.get(type);
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(ItemDataComponent<T> type, T def) {
        return components.get(type, def);
    }

    static {
        AIR = new ItemModel(new ItemComponents());
        AIR.components.set(ItemDataComponents.MATERIAL, new DataString("air"));

        var builder = PipelineYamlCodecBuilder.of(ItemComponents::new);

        for (ItemDataComponent component : ItemDataComponents.list()) {
            builder.field(component.codec(), component.name(),
                    v -> v.get(component),
                    (v, c) -> v.set(component, c)
            );
        }
        CODEC = builder.build().map(
                ItemModel::new,
                i -> i.components
        );
    }
}
