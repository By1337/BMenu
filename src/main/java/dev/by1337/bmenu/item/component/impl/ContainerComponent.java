package dev.by1337.bmenu.item.component.impl;

import dev.by1337.bmenu.item.ItemModel;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Map;

public record ContainerComponent(Int2ObjectOpenHashMap<ItemModel> items) {
    public static final YamlCodec<ContainerComponent> CODEC = YamlCodec.lazyLoad(() ->
            YamlCodec.mapOf(YamlCodec.INT,
                    ItemModel.CODEC
            ).map(
                    map -> new ContainerComponent(new Int2ObjectOpenHashMap<>(map)),
                    map -> (Map<Integer, ItemModel>) map.items
            )
    );
}
