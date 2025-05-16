package org.by1337.bmenu.factory;

import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.by1337.blib.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MenuCodecs {
    public static final YamlCodec<Map<String, String>> ARGS_CODEC =
            YamlCodec.mapOf(
                    YamlCodec.STRING,
                    YamlCodec.MULTI_LINE_STRING.schema(SchemaTypes.anyOf(SchemaTypes.STRING_OR_NUMBER, SchemaTypes.STRING_OR_NUMBER.listOf()))
            );
    public static final YamlCodec<String> MATERIAL = YamlCodec.STRING.schema(s -> s.or(BukkitYamlCodecs.MATERIAL.schema()));

    public static final YamlCodec<PotionEffect> POTION_EFFECT_YAML_CODEC = new YamlCodec<>() {
        @Override
        public PotionEffect decode(YamlValue yamlValue) {
            String s = yamlValue.decode(STRING);
            String[] args0 = s.split(";");
            if (args0.length != 3) {
                throw new IllegalArgumentException("expected <PotionEffectType>;<duration>;<amplifier>, got '" + s + "'");
            }
            PotionEffectType type = Objects.requireNonNull(PotionEffectType.getByName(args0[0].toLowerCase(Locale.ENGLISH)), "PotionEffectType is null");
            int duration = Integer.parseInt(args0[1]);
            int amplifier = Integer.parseInt(args0[2]);

            return new PotionEffect(type, duration, amplifier);
        }

        @Override
        public YamlValue encode(PotionEffect potionEffect) {
            String sb = potionEffect.getType().getName() +
                    ";" +
                    potionEffect.getDuration() +
                    ";" +
                    potionEffect.getAmplifier();
            return YamlValue.wrap(sb);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SchemaTypes.STRING;
        }
    };

    public static final YamlCodec<Pair<Enchantment, Integer>> ENCHANTMENT_YAML_CODEC = new YamlCodec<>() {
        @Override
        public Pair<Enchantment, Integer> decode(YamlValue yamlValue) {
            String s = yamlValue.decode(STRING);
            try {
                String[] args0 = s.split(";");
                if (args0.length != 2) {
                    throw new IllegalArgumentException("was expected to be enchantmentid;level, got '" + s + "'");
                }
                Enchantment type = Enchantment.getByKey(NamespacedKey.minecraft(args0[0].toLowerCase(Locale.ENGLISH)));
                if (type == null) {
                    throw new IllegalArgumentException("Unknown enchant: " + args0[0]);
                }
                int level = Integer.parseInt(args0[1]);
                return Pair.of(type, level);
            } catch (Throwable e) {
                throw new IllegalArgumentException("was expected to be enchantmentid;level, got '" + s + "' " + e.getMessage());
            }
        }

        @Override
        public YamlValue encode(Pair<Enchantment, Integer> pair) {
            return YamlValue.wrap(pair.getLeft().getKey().value() + ":" + pair.getRight());
        }

        @Override
        public @NotNull SchemaType schema() {
            return SchemaTypes.STRING;
        }
    };
}
