package org.by1337.bmenu.factory;

import blib.com.mojang.serialization.Codec;
import blib.com.mojang.serialization.DataResult;
import blib.com.mojang.serialization.DynamicOps;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlOps;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.configuration.serialization.BukkitCodecs;
import org.by1337.blib.util.Pair;
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

        if (result.isError()){
            LOGGER.error(result.error().get().message());

        }
        MenuItemBuilder builder = result.resultOrPartial().get().getFirst();

//        builder.setItemFlags(ctx.get("item_flags").getAsList(YamlValue::getAsString, Collections.emptyList()).stream().map(s -> ItemFlag.valueOf(s.toUpperCase(Locale.ENGLISH))).toList());
//        if (ctx.getAsBoolean("all_flags", false)){
//            builder.setItemFlags(Arrays.stream(ItemFlag.values()).toList());
//        }
//        builder.setPotionEffects(
//                ctx.get("potion_effects").getAsList(YamlValue::getAsString, Collections.emptyList()).stream()
//                        .map(s -> {
//                            String[] args0 = s.split(";");
//                            if (args0.length != 3) {
//                                LOGGER.error("expected <PotionEffectType>;<duration>;<amplifier>, not {}", s);
//                                return null;
//                            }
//                            PotionEffectType type = Objects.requireNonNull(PotionEffectType.getByName(args0[0].toLowerCase(Locale.ENGLISH)), "PotionEffectType is null");
//                            int duration = Integer.parseInt(args0[1]);
//                            int amplifier = Integer.parseInt(args0[2]);
//                            return new PotionEffect(type, duration, amplifier);
//                        }).filter(Objects::nonNull).toList()
//
//        );
       // builder.setColor(ctx.getAs("color", Color.class, null));
//        builder.setEnchantments(
//                ctx.get("enchantments").getAsList(YamlValue::getAsString, Collections.emptyList()).stream()
//                        .map(s -> {
//                            String[] args0 = s.split(";");
//                            if (args0.length != 2) {
//                                LOGGER.error("was expected to be enchantmentid;level, not {}", s);
//                                return null;
//                            }
//                            Enchantment type = Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(args0[0].toLowerCase(Locale.ENGLISH))), "Enchantment is null");
//                            int level = Integer.parseInt(args0[1]);
//                            return Pair.of(type, level);
//                        }).filter(Objects::nonNull).toList()
//        );
        builder.setSlots(getSlots(ctx));
        builder.setViewRequirement(
                ObjectUtil.mapIfNotNullOrDefault(
                        ctx.get("view_requirement.requirements"),
                        v -> RequirementsFactory.read(v, loader),
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
                                        v -> RequirementsFactory.read(v, loader),
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
        FIELD_CODECS.add(Codec.unboundedMap(Codec.STRING, Codec.STRING), MenuItemBuilder::args, MenuItemBuilder::setArgs, "args", Map.of());

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
