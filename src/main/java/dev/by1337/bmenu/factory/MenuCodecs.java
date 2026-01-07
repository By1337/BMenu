package dev.by1337.bmenu.factory;

import dev.by1337.cmd.Command;
import dev.by1337.core.util.registry.LegacyRegistryBridge;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.InlineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.JsonSchemaTypeBuilder;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.item.component.EnchantmentData;
import dev.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MenuCodecs {

    public static final YamlCodec<Map<String, String>> ARGS_CODEC =
            YamlCodec.mapOf(
                    YamlCodec.STRING,
                    YamlCodec.MULTI_LINE_STRING.schema(SchemaTypes.anyOf(SchemaTypes.STRING_OR_NUMBER, SchemaTypes.STRING_OR_NUMBER.listOf()))
            );
    public static final YamlCodec<String> MATERIAL = YamlCodec.STRING.schema(s -> s.or(BukkitCodecs.material().schema()));

    @Deprecated(since = "1.20.3")
    private static final YamlCodec<PotionEffectType> LEGACY_POTION_EFFECT_TYPE_CODEC =
            YamlCodec.lookup(Arrays.stream(PotionEffectType.values())
                    .filter(v -> v.getName() != null)
                    .collect(Collectors.toMap(
                            PotionEffectType::getName,
                            v -> v
                    )));

    public static final YamlCodec<PotionEffectType> POTION_EFFECT_TYPE_CODEC = anyCodec(
            LegacyRegistryBridge.MOB_EFFECT.yamlCodec(),
            LEGACY_POTION_EFFECT_TYPE_CODEC
    );


    private static final YamlCodec<int[]> TWO_INTS = YamlCodec.STRING.flatMap(
            s -> {
                String[] split = s.split("\\s+", 2);
                if (split.length != 2) return DataResult.error("expected '<number> <number>', but got '{}'", s);
                return YamlCodec.INT.decode(split[0]).flatMap(i1 ->
                        YamlCodec.INT.decode(split[1]).mapValue(i2 -> new int[]{i1, i2})
                );

            },
            arr -> getByIndex(arr, 0) + " " + getByIndex(arr, 1)
    );

    private static int getByIndex(int[] arr, int index) {
        return arr.length > index ? arr[index] : 0;
    }

    public static final YamlCodec<List<PotionEffect>> POTION_EFFECT_LIST_CODEC =
            YamlCodec.mapOf(LegacyRegistryBridge.MOB_EFFECT.yamlCodec(), TWO_INTS).map(
                    map -> map.entrySet().stream().map(e -> new PotionEffect(e.getKey(), e.getValue()[0], e.getValue()[1])).toList(),
                    list -> list.stream().collect(Collectors.toMap(
                            PotionEffect::getType,
                            v -> new int[]{v.getDuration(), v.getAmplifier()}
                    ))
            ).whenPrimitive(InlineYamlCodecBuilder.inline(
                    ";",
                    "<PotionEffectType>;<duration>;<amplifier>",
                    PotionEffect::new,
                    POTION_EFFECT_TYPE_CODEC.withGetter(PotionEffect::getType),
                    YamlCodec.INT.withGetter(PotionEffect::getDuration),
                    YamlCodec.INT.withGetter(PotionEffect::getAmplifier)
            ).listOf());

    public static final YamlCodec<List<EnchantmentData>> ENCHANTMENT_LIST_CODEC =
            YamlCodec.mapOf(BukkitCodecs.enchantment(), YamlCodec.INT).map(
                    map -> map.entrySet().stream().map(e -> new EnchantmentData(e.getKey(), e.getValue())).toList(),
                    list -> list.stream().collect(Collectors.toMap(
                            EnchantmentData::enchantment,
                            EnchantmentData::lvl
                    ))
            ).whenPrimitive(InlineYamlCodecBuilder.inline(
                    ";",
                    "<enchantment>;<lvl>",
                    EnchantmentData::new,
                    BukkitCodecs.enchantment().withGetter(EnchantmentData::enchantment),
                    YamlCodec.INT.withGetter(EnchantmentData::lvl)
            ).listOf());


    public static final YamlCodec<List<String>> MAP_TO_LIST = new YamlCodec<List<String>>() {
        @Override
        public DataResult<List<String>> decode(YamlValue yamlValue) {
            return YamlCodec.STRING_TO_STRING.decode(yamlValue).mapValue(map -> {
                List<String> res = new ArrayList<>();
                for (String key : map.keySet()) {
                    String val = map.get(key);
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(key).append("]");
                    if (!val.isBlank()) sb.append(" ").append(val);
                    res.add(sb.toString());
                }
                return res;
            });
        }

        @Override
        public YamlValue encode(List<String> list) {
            return YamlValue.wrap(list);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SchemaTypes.STRING.asMap();
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

    private static <T> YamlCodec<T> anyCodec(YamlCodec<T> first, YamlCodec<T> second) {
        return new YamlCodec<T>() {
            @Override
            public DataResult<T> decode(YamlValue yamlValue) {
                var v = first.decode(yamlValue);
                if (v.hasResult()) return v;
                return second.decode(yamlValue);
            }

            @Override
            public YamlValue encode(T t) {
                return first.encode(t);
            }

            @Override
            public @NotNull SchemaType schema() {
                return first.schema();
            }
        };
    }

    static {
        var builder = JsonSchemaTypeBuilder.create();
        builder.type(SchemaTypes.Type.OBJECT);
        builder.additionalProperties(true);

        for (Command<ExecuteContext> value : Menu.getCommands().getSubcommands().values()) {
            String cmd = value.name().toLowerCase(Locale.ENGLISH);
            if (cmd.startsWith("[") && cmd.endsWith("]")) {
                var subBuilder = JsonSchemaTypeBuilder.create();
                subBuilder.type(SchemaTypes.Type.STRING);
                StringBuilder sb = new StringBuilder();
                for (var arg : value.arguments()) {
                    sb.append("<").append(arg.name()).append("> ");
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

