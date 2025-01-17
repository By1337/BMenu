package org.by1337.bmenu.factory;

import blib.com.mojang.serialization.Codec;
import blib.com.mojang.serialization.DataResult;
import blib.com.mojang.serialization.DynamicOps;
import blib.com.mojang.serialization.MapLike;
import blib.com.mojang.serialization.codecs.PrimitiveCodec;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.by1337.blib.chat.ChatColor;
import org.by1337.blib.util.Pair;

import java.util.Locale;
import java.util.Objects;

public class MenuItemCodecs {
    public static final Codec<PotionEffect> POTION_EFFECT_CODEC = new PrimitiveCodec<PotionEffect>() {
        @Override
        public <T> DataResult<PotionEffect> read(DynamicOps<T> ops, T t) {
            return ops.getStringValue(t).flatMap(s -> {
                try {
                    String[] args0 = s.split(";");
                    if (args0.length != 3) {
                        return DataResult.error(() -> "expected <PotionEffectType>;<duration>;<amplifier>, got '" + s + "'");
                    }
                    PotionEffectType type = Objects.requireNonNull(PotionEffectType.getByName(args0[0].toLowerCase(Locale.ENGLISH)), "PotionEffectType is null");
                    int duration = Integer.parseInt(args0[1]);
                    int amplifier = Integer.parseInt(args0[2]);

                    return DataResult.success(new PotionEffect(type, duration, amplifier));
                } catch (Throwable e) {
                    return DataResult.error(() -> "expected <PotionEffectType>;<duration>;<amplifier>, got '" + s + "' " + e.getMessage());
                }
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, PotionEffect potionEffect) {
            String sb = potionEffect.getType().getName() +
                    ";" +
                    potionEffect.getDuration() +
                    ";" +
                    potionEffect.getAmplifier();
            return ops.createString(sb);
        }
    };

    public static final Codec<Color> COLOR_CODEC = Codec.STRING.flatXmap(
            s -> {
                try {
                    return DataResult.success(ChatColor.fromHex(s).toBukkitColor());
                } catch (Throwable t) {
                    return DataResult.error(t::getMessage);
                }
            },
            c -> DataResult.success(ChatColor.toHex(c))
    );

    public static final Codec<Pair<Enchantment, Integer>> ENCHANTMENT_AND_LEVEL_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Pair<Enchantment, Integer>> read(DynamicOps<T> ops, T t) {
            return ops.getStringValue(t).flatMap(s -> {
                try {
                    String[] args0 = s.split(";");
                    if (args0.length != 2) {
                        return DataResult.error(() -> "was expected to be enchantmentid;level, got '" + s + "'");
                    }
                    Enchantment type = Enchantment.getByKey(NamespacedKey.minecraft(args0[0].toLowerCase(Locale.ENGLISH)));
                    if (type == null) {
                        return DataResult.error(() -> "Unknown enchant: " + args0[0]);
                    }
                    int level = Integer.parseInt(args0[1]);
                    return DataResult.success(Pair.of(type, level));
                } catch (Throwable e) {
                    return DataResult.error(() -> "was expected to be enchantmentid;level, got '" + s + "' " + e.getMessage());
                }
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Pair<Enchantment, Integer> pair) {
            return ops.createString(pair.getLeft().getKey().value() + ":" + pair.getRight());
        }
    };

    public static <T, E> DataResult<E> read(Codec<E> codec, DynamicOps<T> ops, String param, MapLike<T> map, E def){
        T raw = map.get(param);
        if (raw == null || raw == ops.empty()) return DataResult.success(def);
        return codec.decode(ops, raw).map(blib.com.mojang.datafixers.util.Pair::getFirst);
    }
}
