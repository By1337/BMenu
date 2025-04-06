package org.by1337.bmenu.factory;

import blib.com.mojang.datafixers.util.Pair;
import blib.com.mojang.serialization.Codec;
import blib.com.mojang.serialization.DataResult;
import blib.com.mojang.serialization.DynamicOps;
import org.bukkit.inventory.ItemFlag;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlOps;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.configuration.serialization.BukkitCodecs;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.click.ClickHandlerImpl;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.requirement.Requirements;
import org.by1337.bmenu.serialization.ValueCodecSet;
import org.by1337.bmenu.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ItemFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger("BMenu#ItemFactory");
    private static final ValueCodecSet<MenuItemBuilder> FIELD_CODECS;
    public static final Codec<MenuItemBuilder> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<blib.com.mojang.datafixers.util.Pair<MenuItemBuilder, T>> decode(DynamicOps<T> ops, T t) {
            MenuItemBuilder menuItemBuilder = new MenuItemBuilder();
            DataResult<MenuItemBuilder> result = FIELD_CODECS.decode(menuItemBuilder, ops, t);
            menuItemBuilder.postDecode();
            return result.map(m -> blib.com.mojang.datafixers.util.Pair.of(m, t));
        }

        @Override
        public <T> DataResult<T> encode(MenuItemBuilder menuItemBuilder, DynamicOps<T> ops, T t) {
            return FIELD_CODECS.encode(menuItemBuilder, ops, t);
        }
    };

    public static Map<String, MenuItemBuilder> readItems(Map<String, YamlContext> items, MenuLoader loader) {
        Map<String, MenuItemBuilder> itemMap = new HashMap<>();

        for (String itemID : items.keySet()) {
            YamlContext ctx = items.get(itemID);
            itemMap.put(itemID, readItem(ctx, loader));
        }
        return itemMap;
    }

    public static MenuItemBuilder readItem(YamlContext ctx, MenuLoader loader) {
        DataResult<blib.com.mojang.datafixers.util.Pair<MenuItemBuilder, YamlValue>> result = CODEC.decode(YamlOps.INSTANCE, ctx.get());

        if (result.isError()) {
            LOGGER.error(result.error().get().message());

        }
        MenuItemBuilder builder = result.resultOrPartial().get().getFirst();

        builder.setSlots(getSlots(ctx));
        builder.setViewRequirement(
                ObjectUtil.mapIfNotNullOrDefault(
                        ctx.get("view_requirement.requirements"),
                        RequirementsFactory::read,
                        Requirements.EMPTY
                ),
                ctx.getList("view_requirement.deny_commands", String.class, Collections.emptyList())
        );

        for (MenuClickType value : MenuClickType.values()) {
            String key = value.getConfigKeyClick();
            if (ctx.getHandle().contains(key)) {
                builder.addClickListener(
                        value,
                        new ClickHandlerImpl(
                                ctx.get(key + ".deny_commands")
                                        .getAsList(YamlValue::getAsString, Collections.emptyList())
                                ,
                                ctx.get(key + ".commands").getAsList(YamlValue::getAsString, Collections.emptyList())
                                ,
                                ObjectUtil.mapIfNotNullOrDefault(
                                        ctx.get(key + ".requirements"),
                                        RequirementsFactory::read,
                                        Requirements.EMPTY
                                )
                        )
                );
            }
        }
        return builder;
    }


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

    static {
        FIELD_CODECS = new ValueCodecSet<>();
        FIELD_CODECS.add(Codec.STRING, MenuItemBuilder::material, MenuItemBuilder::setMaterial, "material", "STONE");
        FIELD_CODECS.add(Codec.STRING, MenuItemBuilder::name, MenuItemBuilder::setName, "display_name");
        FIELD_CODECS.add(Codec.STRING, MenuItemBuilder::amount, MenuItemBuilder::setAmount, "amount", "1");
        FIELD_CODECS.add(Codec.STRING.listOf(), MenuItemBuilder::lore, MenuItemBuilder::setLore, "lore", List.of());
        FIELD_CODECS.add(new Codec<>() {
            @Override
            public <T> DataResult<Pair<Map<String, String>, T>> decode(DynamicOps<T> ops, T t) {

                ;
                return ops.getMap(t).map(map -> {
                    Map<String, String> result = new HashMap<>();
                    map.entries().forEach(pair -> {
                        String key = ops.getStringValue(pair.getFirst()).getOrThrow();
                        T value = pair.getSecond();

                        var v = ops.getStringValue(value);
                        if (!v.isError()) {
                            result.put(key, v.getOrThrow());
                        } else {
                            var list = ops.getStream(value).getOrThrow();
                            StringBuilder sb = new StringBuilder();
                            list.forEach(t1 -> {
                                sb.append(ops.getStringValue(t1).getOrThrow()).append("\\n");
                            });
                            if (!sb.isEmpty()) {
                                sb.setLength(sb.length() - 2);
                            }
                            result.put(key, sb.toString());
                        }
                    });
                    return DataResult.success(Pair.of(result, t));
                }).getOrThrow();
            }

            @Override
            public <T> DataResult<T> encode(Map<String, String> map, DynamicOps<T> ops, T t) {
                Map<T, T> map0 = new IdentityHashMap<>();
                map.forEach((k, v) -> map0.put(ops.createString(k), ops.createString(v)));
                return DataResult.success(ops.createMap(map0));
            }
        }, MenuItemBuilder::args, MenuItemBuilder::setArgs, "args", Map.of());

        FIELD_CODECS.add(Codec.BOOL, MenuItemBuilder::unbreakable, MenuItemBuilder::setUnbreakable, "unbreakable", false);
        FIELD_CODECS.add(Codec.BOOL, MenuItemBuilder::ticking, MenuItemBuilder::setTicking, "ticking", false);
        FIELD_CODECS.add(Codec.BOOL, MenuItemBuilder::isStaticItem, MenuItemBuilder::setStaticItem, "static", false);

        FIELD_CODECS.add(Codec.BOOL,
                i -> i.getItemFlags().size() == ItemFlag.values().length,
                (i, f) -> {
                    if (f) i.setItemFlags(Arrays.stream(ItemFlag.values()).toList());
                },
                "all_flags",
                false
        );

        FIELD_CODECS.add(Codec.INT, MenuItemBuilder::modelData, MenuItemBuilder::setModelData, "model_data", 0);
        FIELD_CODECS.add(Codec.INT, MenuItemBuilder::priority, MenuItemBuilder::setPriority, "priority", 0);
        FIELD_CODECS.add(Codec.INT, MenuItemBuilder::damage, MenuItemBuilder::setDamage, "damage", 0);
        FIELD_CODECS.add(Codec.INT, MenuItemBuilder::tickSpeed, MenuItemBuilder::setTickSpeed, "tick-speed", 1);
        FIELD_CODECS.add(MenuItemCodecs.COLOR_CODEC, MenuItemBuilder::color, MenuItemBuilder::setColor, "color");

        FIELD_CODECS.add(
                BukkitCodecs.ITEM_FLAG.listOf(),
                MenuItemBuilder::itemFlags,
                MenuItemBuilder::setItemFlags,
                "item_flags",
                List.of()
        );

        FIELD_CODECS.add(
                MenuItemCodecs.ENCHANTMENT_AND_LEVEL_CODEC.listOf(),
                MenuItemBuilder::enchantments,
                MenuItemBuilder::setEnchantments,
                "enchantments",
                List.of()
        );

        FIELD_CODECS.add(
                MenuItemCodecs.POTION_EFFECT_CODEC.listOf(),
                MenuItemBuilder::potionEffects,
                MenuItemBuilder::setPotionEffects,
                "potion_effects",
                List.of()
        );

    }
}
