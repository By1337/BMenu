package org.by1337.bmenu.factory;

import com.google.common.primitives.Ints;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ItemFactory {
    public static final YamlCodec<int[]> SLOTS_YAML_CODEC = new YamlCodec<int[]>() {
        private final SchemaType schemaType = SchemaTypes.oneOf(SchemaTypes.STRING_OR_NUMBER, SchemaTypes.array(SchemaTypes.STRING_OR_NUMBER));

        @Override
        public DataResult<int[]> decode(YamlValue yamlValue) {
            return DataResult.success(readSlots(yamlValue)); // todo
        }

        @Override
        public YamlValue encode(int[] ints) {
            return YamlValue.wrap(Ints.asList(ints));
        }

        @Override
        public @NotNull SchemaType schema() {
            return schemaType;
        }
    };
    private static final Logger LOGGER = LoggerFactory.getLogger("BMenu#ItemFactory");


    @Deprecated
    public static Map<String, MenuItemBuilder> readItems(Map<String, YamlMap> items) {
        Map<String, MenuItemBuilder> itemMap = new HashMap<>();

        for (String itemID : items.keySet()) {
            itemMap.put(itemID, readItem(items.get(itemID)));
        }
        return itemMap;
    }

    @Deprecated
    public static MenuItemBuilder readItem(YamlMap item) {
        return item.get().decode(MenuItemBuilder.YAML_CODEC).getOrThrow();
    }


    public static int[] readSlots(YamlValue data) {
        List<Integer> slots = new ArrayList<>();
        if (data.isPrimitive()) {
            parseSlot(data.decode(YamlCodec.STRING).getOrThrow(), slots::add);
        } else if (data.isCollection()) {
            for (String s : data.getAsStringList().getOrThrow()) {
                parseSlot(s, slots::add);
            }
        }
        if (slots.isEmpty()) {
            return new int[]{-1};
        }
        return Ints.toArray(slots);
    }

    private static void parseSlot(String input, Consumer<Integer> consumer) {
        try {
            for (int i : AnimationUtil.readSlots(input)) {
                consumer.accept(i);
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse slot number. Input: '{}'", input, e);
        }
    }
}
