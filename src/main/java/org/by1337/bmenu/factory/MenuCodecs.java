package org.by1337.bmenu.factory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.JsonSchemaTypeBuilder;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.by1337.blib.RegistryHelper;
import org.by1337.blib.command.Command;
import org.by1337.blib.util.Pair;
import org.by1337.bmenu.Menu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MenuCodecs {
    private static final Logger log = LoggerFactory.getLogger("BMenu");

    public static final YamlCodec<Map<String, String>> ARGS_CODEC =
            YamlCodec.mapOf(
                    YamlCodec.STRING,
                    YamlCodec.MULTI_LINE_STRING.schema(SchemaTypes.anyOf(SchemaTypes.STRING_OR_NUMBER, SchemaTypes.STRING_OR_NUMBER.listOf()))
            );
    public static final YamlCodec<String> MATERIAL = YamlCodec.STRING.schema(s -> s.or(BukkitYamlCodecs.MATERIAL.schema()));

    public static final YamlCodec<PotionEffect> POTION_EFFECT_YAML_CODEC = new YamlCodec<>() {
        @Override
        public DataResult<PotionEffect> decode(YamlValue yamlValue) {
            return yamlValue.decode(STRING).flatMap(s -> {
                String[] args0 = s.split(";");
                if (args0.length != 3)
                    return DataResult.error("expected <PotionEffectType>;<duration>;<amplifier>, got '" + s + "'");
                PotionEffectType effect = PotionEffectType.getByName(args0[0].toLowerCase(Locale.ENGLISH));
                if (effect == null) return DataResult.error("unknown potion effect '" + args0[0] + "'");
                return INT.decode(YamlValue.wrap(args0[1]))
                        .flatMap(duration -> INT.decode(YamlValue.wrap(args0[2]))
                                .mapValue(amplifier -> new PotionEffect(effect, duration, amplifier))
                        );
            });
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

    public static final YamlCodec<List<PotionEffect>> MODERN_POTION_EFFECT_YAML_CODEC = new YamlCodec<List<PotionEffect>>() {
        private static final BiMap<String, PotionEffectType> POTION_EFFECTS;
        private static final SchemaType SCHEMA_TYPE;

        private static final YamlCodec<List<PotionEffect>> LEGACY_CODEC = POTION_EFFECT_YAML_CODEC.listOrSingle();

        @Override
        public DataResult<List<PotionEffect>> decode(YamlValue yamlValue) {
            if (yamlValue.isPrimitive() || yamlValue.isCollection()) return LEGACY_CODEC.decode(yamlValue);
            return YamlCodec.STRING_TO_STRING.decode(yamlValue).flatMap(map -> {
                List<PotionEffect> result = new ArrayList<>();
                StringBuilder error = new StringBuilder();
                for (String s : map.keySet()) {
                    PotionEffectType effect = POTION_EFFECTS.get(s);
                    if (effect == null) {
                        error.append("Unknown potion effect '").append(s).append("'").append("\n");
                    } else {
                        String value = map.get(s);
                        String[] args = value.split(" ", 2);
                        if (args.length != 2) {
                            error.append("expected 'effect: <duration> <amplifier>', got '").append(s).append(": ").append(value).append("'").append("\n");
                        } else {
                            var res = INT.decode(YamlValue.wrap(args[0])).flatMap(duration ->
                                    INT.decode(YamlValue.wrap(args[1])).mapValue(amplifier ->
                                            new PotionEffect(effect, duration, amplifier))
                            );
                            if (res.hasError()) {
                                error.append(res.error()).append("\n");
                            }
                            if (res.hasResult()) {
                                result.add(res.result());
                            }
                        }
                    }
                }
                if (!error.isEmpty()) {
                    error.setLength(error.length() - 1);
                    return DataResult.error(error.toString()).partial(result);
                }
                return DataResult.success(result);
            });
        }

        @Override
        public YamlValue encode(List<PotionEffect> potionEffects) {
            Map<String, String> map = new HashMap<>();
            for (PotionEffect potionEffect : potionEffects) {
                var key = POTION_EFFECTS.inverse().get(potionEffect.getType());
                if (key == null) {
                    log.error("Failed to serialize potion effect: {}", potionEffect);
                    continue;
                }
                map.put(key, potionEffect.getDuration() + " " + potionEffect.getAmplifier());
            }
            return YamlValue.wrap(map);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SCHEMA_TYPE;
        }

        static {
            POTION_EFFECTS = HashBiMap.create();
            for (RegistryHelper.Holder<PotionEffectType> holder : RegistryHelper.MOB_EFFECT) {
                var type = holder.value();
                var key = holder.getKey();
                POTION_EFFECTS.put(key.getKey(), type);
            }
            var builder = JsonSchemaTypeBuilder.create();
            builder.type(SchemaTypes.Type.OBJECT);
            builder.additionalProperties(false);
            for (String enchantment : POTION_EFFECTS.keySet()) {
                builder.properties(enchantment, SchemaTypes.STRING);
            }
            SCHEMA_TYPE = builder.build().or(LEGACY_CODEC.schema());
        }
    };

    public static final YamlCodec<Pair<Enchantment, Integer>> ENCHANTMENT_YAML_CODEC = new YamlCodec<>() {
        @Override
        public DataResult<Pair<Enchantment, Integer>> decode(YamlValue yamlValue) {
            return yamlValue.decode(STRING).flatMap(s -> {
                String[] args0 = s.split(";");
                if (args0.length != 2) {
                    return DataResult.error("expected <Enchantment>;<duration> <amplifier>, got '" + s + "'");
                }
                return BukkitYamlCodecs.NAMESPACED_KEY.decode(YamlValue.wrap(args0[0].toLowerCase(Locale.ENGLISH))).flatMap(key -> {
                    Enchantment type = Enchantment.getByKey(key);
                    if (type == null) return DataResult.error("Unknown enchantment '" + key + "'");
                    return INT.decode(YamlValue.wrap(args0[1])).mapValue(level -> Pair.of(type, level));
                });

            });
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


    public static final YamlCodec<List<Pair<Enchantment, Integer>>> MODERN_ENCHANTMENT_YAML_CODEC = new YamlCodec<>() {
        private static final Map<String, Enchantment> ENCHANTMENTS;
        private static final SchemaType SCHEMA_TYPE;

        private static final YamlCodec<List<Pair<Enchantment, Integer>>> LEGACY_CODEC = ENCHANTMENT_YAML_CODEC.listOrSingle();

        @Override
        public DataResult<List<Pair<Enchantment, Integer>>> decode(YamlValue yamlValue) {
            if (yamlValue.isPrimitive() || yamlValue.isCollection()) {
                return LEGACY_CODEC.decode(yamlValue);
            }
            return YamlCodec.STRING_TO_INT.decode(yamlValue).flatMap(map -> {
                List<Pair<Enchantment, Integer>> result = new ArrayList<>();
                StringBuilder error = new StringBuilder();
                for (String key : map.keySet()) {
                    DataResult<Enchantment> enchant = BukkitYamlCodecs.ENCHANTMENT.decode(YamlValue.wrap(key));
                    if (enchant.hasError()) {
                        error.append(enchant.error()).append("\n");
                    }
                    if (enchant.hasResult()) {
                        result.add(Pair.of(enchant.result(), map.get(key)));
                    }
                }
                if (!error.isEmpty()) {
                    error.setLength(error.length() - 1);
                    return DataResult.error(error.toString()).partial(result);
                }
                return DataResult.success(result);
            });
        }

        @Override
        public YamlValue encode(List<Pair<Enchantment, Integer>> list) {
            Map<String, Integer> map = new LinkedHashMap<>();
            for (Pair<Enchantment, Integer> enchantment : list) {
                map.put(enchantment.getLeft().getKey().getKey(), enchantment.getRight());
            }
            return YamlValue.wrap(map);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SCHEMA_TYPE;
        }

        static {
            ENCHANTMENTS = new HashMap<>();
            for (Enchantment enchantment : Registry.ENCHANTMENT) {
                ENCHANTMENTS.put(enchantment.getKey().getKey(), enchantment);
            }

            var builder = JsonSchemaTypeBuilder.create();
            builder.type(SchemaTypes.Type.OBJECT);
            builder.additionalProperties(false);
            for (String enchantment : ENCHANTMENTS.keySet()) {
                builder.properties(enchantment, SchemaTypes.INT);
            }
            SCHEMA_TYPE = builder.build().or(LEGACY_CODEC.schema());
        }
    };

    public static final String COMMANDS_SCHEMA_TYPE_REF_NAME = "commands_ref";
    public static final SchemaType COMMANDS_SCHEMA_TYPE;

    public static final YamlCodec<List<String>> COMMANDS = new YamlCodec<>() {

        private static final SchemaType SCHEMA_TYPE = new JsonSchemaTypeBuilder().ref("#/definitions/" + COMMANDS_SCHEMA_TYPE_REF_NAME).build();
        private static final YamlCodec<List<YamlValue>> YAML_VALUES = YamlCodec.YAML_VALUE.listOf();

        @Override
        public DataResult<List<String>> decode(YamlValue yamlValue) {
            if (yamlValue.isPrimitive()) return YamlCodec.STRINGS.decode(yamlValue);
            return YAML_VALUES.decode(yamlValue).flatMap(list -> {
                List<String> result = new ArrayList<>(list.size());
                StringBuilder error = new StringBuilder();
                for (YamlValue value : list) {
                    if (value.isMap()) {
                        var dataResult = YamlCodec.STRING_TO_STRING.decode(value).flatMap(map -> {
                            List<String> res = new ArrayList<>();
                            for (String key : map.keySet()) {
                                String val = map.get(key);
                                StringBuilder sb = new StringBuilder();
                                sb.append("[").append(key).append("]");
                                if (!val.isBlank()) sb.append(" ").append(val);
                                res.add(sb.toString());
                            }
                            return DataResult.success(res);
                        });
                        if (dataResult.hasError()) {
                            error.append(dataResult.error()).append("\n");
                        }
                        if (dataResult.hasResult()) {
                            result.addAll(dataResult.result());
                        }
                    } else {
                        var res = YamlCodec.STRING.decode(value);
                        if (res.hasError()) {
                            error.append(res.error()).append("\n");
                        }
                        if (res.hasResult()) {
                            result.add(res.result());
                        }
                    }
                }
                if (!error.isEmpty()) {
                    error.setLength(error.length() - 1);
                    return DataResult.error(error.toString()).partial(result);
                }
                return DataResult.success(result);
            });
        }

        @Override
        public YamlValue encode(List<String> list) {
            return YamlCodec.STRINGS.encode(list);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SCHEMA_TYPE;
        }
    };

    static {
        var builder = JsonSchemaTypeBuilder.create();
        builder.type(SchemaTypes.Type.OBJECT);
        builder.additionalProperties(true);

        for (Command<Menu> value : Menu.getCommands().getSubcommands().values()) {
            String cmd = value.getCommand().toLowerCase(Locale.ENGLISH);
            if (cmd.startsWith("[") && cmd.endsWith("]")) {
                var subBuilder = JsonSchemaTypeBuilder.create();
                subBuilder.type(SchemaTypes.Type.STRING);
                StringBuilder sb = new StringBuilder();
                for (var arg : value.getArguments()) {
                    sb.append("<").append(arg.getName()).append("> ");
                }
                if (!sb.isEmpty()) {
                    sb.setLength(sb.length() - 1);
                }
                subBuilder.examples(sb.toString());
                builder.properties(cmd.substring(1, cmd.length() - 1), subBuilder.build());
            }
        }
        builder.properties("rebuild", JsonSchemaTypeBuilder.create().type(SchemaTypes.Type.STRING).examples("").build());
        builder.properties("die", JsonSchemaTypeBuilder.create().type(SchemaTypes.Type.STRING).examples("").build());
        builder.properties("update", JsonSchemaTypeBuilder.create().type(SchemaTypes.Type.STRING).examples("").build());
        builder.properties("set_local", JsonSchemaTypeBuilder.create().type(SchemaTypes.Type.STRING).examples("<param> <value>").build());

        COMMANDS_SCHEMA_TYPE = builder.build().listOf().or(SchemaTypes.STRING, SchemaTypes.STRING.listOf());
    }

}

