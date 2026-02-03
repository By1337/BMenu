package dev.by1337.item.component.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.by1337.core.ServerVersion;
import dev.by1337.yaml.KeyedYamlCodec;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasePotionComponent {
    public static final YamlCodec<BasePotionComponent> CODEC = Pair.CODEC.map(
            BasePotionComponent::new,
            p -> p.data
    );
    private final Pair data;

    private BasePotionComponent(Pair data) {
        this.data = data;
    }

    public BasePotionComponent(PotionType type) {
        if (ServerVersion.is1_20_2orNewer()) {
            data = new Pair(type, null);
        } else {
            data = new Pair(null, CraftPotionUtil.toBukkit(type));
        }
    }

    public BasePotionComponent(PotionData data) {
        if (ServerVersion.is1_20_2orNewer()) {
            this.data = new Pair(data.getType(), null);
        } else {
            this.data = new Pair(null, data);
        }
    }


    public void apply(PotionMeta pm) {
        if (ServerVersion.is1_20_2orNewer()) {
            pm.setBasePotionType(data.type);
        } else {
            pm.setBasePotionData(data.data);
        }
    }

    public static BasePotionComponent fromMeta(PotionMeta im) {
        if (ServerVersion.is1_20_2orNewer()) {
            return new BasePotionComponent(im.getBasePotionType());
        } else {
            return new BasePotionComponent(im.getBasePotionData());
        }
    }

    private record Pair(PotionType type, PotionData data) {
        public static final YamlCodec<Pair> CODEC;

        public static Pair of(String dat) {
            if (ServerVersion.is1_20_2orNewer()) {
                return new Pair(Registry.POTION.get(Key.key(dat)), null);
            } else {
                return new Pair(null, CraftPotionUtil.toBukkit(dat));
            }
        }

        static {
            if (ServerVersion.is1_20_2orNewer()) {
                KeyedYamlCodec<PotionType> codec = new KeyedYamlCodec<>(Registry.POTION, "PotionType");
                CODEC = codec.map(
                        t -> new Pair(t, null),
                        Pair::type
                );
            } else {
                CODEC = new YamlCodec<>() {
                    final static Map<String, PotionData> key2Data;
                    final static SchemaType schema;

                    @Override
                    public DataResult<Pair> decode(YamlValue yaml) {
                        return yaml.decode(STRING).flatMap(s -> {
                            var v = key2Data.get(s);
                            if (v == null) return DataResult.error("Unknown potion type " + s);
                            return DataResult.success(new Pair(null, v));
                        });
                    }

                    @Override
                    public YamlValue encode(Pair pair) {
                        return YamlValue.wrap(CraftPotionUtil.fromBukkit(pair.data));
                    }

                    @Override
                    public @NotNull SchemaType schema() {
                        return schema;
                    }

                    static {
                        var keys = CraftPotionUtil.keys();
                        schema = SchemaTypes.enumOf(keys);
                        key2Data = new HashMap<>();
                        for (String key : keys) {
                            key2Data.put(key, CraftPotionUtil.toBukkit(key));
                        }
                    }
                };
            }
        }
    }

    private static class CraftPotionUtil {
        private static final BiMap<PotionType, String> regular;
        private static final BiMap<PotionType, String> upgradeable;
        private static final BiMap<PotionType, String> extendable;

        public static String fromBukkit(PotionData data) {
            String type;
            if (data.isUpgraded()) {
                type = upgradeable.get(data.getType());
            } else if (data.isExtended()) {
                type = extendable.get(data.getType());
            } else {
                type = regular.get(data.getType());
            }

            Preconditions.checkNotNull(type, "Unknown potion type from data " + data);
            return "minecraft:" + type;
        }

        public static PotionData toBukkit(PotionType type) {
            String s;
            if ((s = regular.get(type)) != null) return toBukkit(s);
            if ((s = upgradeable.get(type)) != null) return toBukkit(s);
            if ((s = extendable.get(type)) != null) return toBukkit(s);
            return toBukkit("empty");
        }

        public static PotionData toBukkit(String type) {
            if (type == null) {
                return new PotionData(PotionType.UNCRAFTABLE, false, false);
            } else {
                if (type.startsWith("minecraft:")) {
                    type = type.substring(10);
                }

                PotionType potionType = extendable.inverse().get(type);
                if (potionType != null) {
                    return new PotionData(potionType, true, false);
                } else {
                    potionType = upgradeable.inverse().get(type);
                    if (potionType != null) {
                        return new PotionData(potionType, false, true);
                    } else {
                        potionType = regular.inverse().get(type);
                        return potionType != null ? new PotionData(potionType, false, false) : new PotionData(PotionType.UNCRAFTABLE, false, false);
                    }
                }
            }
        }

        private static Set<String> keys() {
            Set<String> res = new HashSet<>();
            res.addAll(regular.values());
            res.addAll(upgradeable.values());
            res.addAll(extendable.values());
            return res;
        }

        static {
            regular = ImmutableBiMap.<PotionType, String>builder()
                    .put(PotionType.UNCRAFTABLE, "empty")
                    .put(PotionType.WATER, "water")
                    .put(PotionType.MUNDANE, "mundane")
                    .put(PotionType.THICK, "thick")
                    .put(PotionType.AWKWARD, "awkward")
                    .put(PotionType.NIGHT_VISION, "night_vision")
                    .put(PotionType.INVISIBILITY, "invisibility")
                    .put(PotionType.JUMP, "leaping")
                    .put(PotionType.FIRE_RESISTANCE, "fire_resistance")
                    .put(PotionType.SPEED, "swiftness")
                    .put(PotionType.SLOWNESS, "slowness")
                    .put(PotionType.WATER_BREATHING, "water_breathing")
                    .put(PotionType.INSTANT_HEAL, "healing")
                    .put(PotionType.INSTANT_DAMAGE, "harming")
                    .put(PotionType.POISON, "poison")
                    .put(PotionType.REGEN, "regeneration")
                    .put(PotionType.STRENGTH, "strength")
                    .put(PotionType.WEAKNESS, "weakness")
                    .put(PotionType.LUCK, "luck")
                    .put(PotionType.TURTLE_MASTER, "turtle_master")
                    .put(PotionType.SLOW_FALLING, "slow_falling")
                    .build();
            upgradeable = ImmutableBiMap.<PotionType, String>builder()
                    .put(PotionType.JUMP, "strong_leaping")
                    .put(PotionType.SPEED, "strong_swiftness")
                    .put(PotionType.INSTANT_HEAL, "strong_healing")
                    .put(PotionType.INSTANT_DAMAGE, "strong_harming")
                    .put(PotionType.POISON, "strong_poison")
                    .put(PotionType.REGEN, "strong_regeneration")
                    .put(PotionType.STRENGTH, "strong_strength")
                    .put(PotionType.SLOWNESS, "strong_slowness")
                    .put(PotionType.TURTLE_MASTER, "strong_turtle_master")
                    .build();
            extendable = ImmutableBiMap.<PotionType, String>builder()
                    .put(PotionType.NIGHT_VISION, "long_night_vision")
                    .put(PotionType.INVISIBILITY, "long_invisibility")
                    .put(PotionType.JUMP, "long_leaping")
                    .put(PotionType.FIRE_RESISTANCE, "long_fire_resistance")
                    .put(PotionType.SPEED, "long_swiftness")
                    .put(PotionType.SLOWNESS, "long_slowness")
                    .put(PotionType.WATER_BREATHING, "long_water_breathing")
                    .put(PotionType.POISON, "long_poison")
                    .put(PotionType.REGEN, "long_regeneration")
                    .put(PotionType.STRENGTH, "long_strength")
                    .put(PotionType.WEAKNESS, "long_weakness")
                    .put(PotionType.TURTLE_MASTER, "long_turtle_master")
                    .put(PotionType.SLOW_FALLING, "long_slow_falling")
                    .build();
        }
    }
}