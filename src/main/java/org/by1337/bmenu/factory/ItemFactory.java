package org.by1337.bmenu.factory;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.by1337.bmenu.click.ClickHandlerImpl;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.factory.fixer.ItemFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ItemFactory {
    public static final YamlCodec<int[]> SLOTS_YAML_CODEC = YamlCodec.of(
            ItemFactory::readSlots,
            v -> YamlValue.wrap(Joiner.on(",").join(Ints.asList(v))),
            SchemaTypes.oneOf(SchemaTypes.STRING_OR_NUMBER, SchemaTypes.array(SchemaTypes.STRING_OR_NUMBER))
    );
    private static final Logger LOGGER = LoggerFactory.getLogger("BMenu#ItemFactory");


    @Deprecated(forRemoval = true)
    public static Map<String, MenuItemBuilder> readItems(Map<String, YamlContext> items, MenuLoader loader) {
        Map<String, MenuItemBuilder> itemMap = new HashMap<>();

        for (String itemID : items.keySet()) {
            YamlContext ctx = items.get(itemID);
            itemMap.put(itemID, readItem(ctx, loader));
        }
        return itemMap;
    }

    @Deprecated(forRemoval = true)
    public static MenuItemBuilder readItem(YamlContext ctx, MenuLoader loader) {
        throw new UnsupportedOperationException(); //todo
    }

    public static Map<String, MenuItemBuilder> readItems(Map<String, YamlMap> items) {
        Map<String, MenuItemBuilder> itemMap = new HashMap<>();

        for (String itemID : items.keySet()) {
            itemMap.put(itemID, readItem(items.get(itemID)));
        }
        return itemMap;
    }

    public static MenuItemBuilder readItem(YamlMap item) {
        ItemFixer.fixItem(item);
        return item.get().decode(MenuItemBuilder.YAML_CODEC);
    }


    public static int[] readSlots(YamlValue data) {
        List<Integer> slots = new ArrayList<>();
        if (data.isPrimitive()) {
            parseSlot(data.getAsString(), slots::add);
        } else if (data.isCollection()) {
            for (String s : data.getAsStringList()) {
                parseSlot(s, slots::add);
            }
        }
        if (slots.isEmpty()) {
            return new int[]{-1};
        }
        int[] slot = new int[slots.size()];
        for (int x = 0; x < slots.size(); x++) {
            slot[x] = slots.get(x);
        }
        return slot;
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

    @Deprecated(forRemoval = true)
    public static int[] getSlots(YamlContext context) {
        List<Integer> slots = new ArrayList<>();
        try {
            if (context.getHandle().contains("slot")) {
                slots.add(context.getAsInteger("slot"));
            }
            if (context.getHandle().contains("slots")) {
                for (String str : context.getList("slots", String.class)) {
                    if (str.contains("-")) {
                        String[] s = str.replace(" ", "").split("-");
                        int x = Integer.parseInt(s[0]);
                        int x1 = Integer.parseInt(s[1]);
                        for (int i = Math.min(x, x1); i <= Math.max(x, x1); i++) {
                            slots.add(i);
                        }
                    } else {
                        int x = Integer.parseInt(str.replace(" ", ""));
                        slots.add(x);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse slots", e);
            return new int[]{-1};
        }
        if (slots.isEmpty()) {
            return new int[]{-1};
        }
        int[] slot = new int[slots.size()];
        for (int x = 0; x < slots.size(); x++) {
            slot[x] = slots.get(x);
        }
        return slot;
    }

}
