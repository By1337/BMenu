package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.handler.BreakableConditionalHandler;
import dev.by1337.bmenu.handler.ConditionalHandler;
import dev.by1337.bmenu.handler.FirstMatchHandler;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.legacy.RequirementsFactory;
import dev.by1337.bmenu.yaml.codec.CodecSelector;
import dev.by1337.bmenu.yaml.codec.YamlTester;
import dev.by1337.bmenu.yaml.dfu.BMenuDFU;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@FunctionalInterface
public interface Requirement {
    YamlCodec<Requirement> CODEC = Codec.allOf();
    Requirement TRUE = new Requirement() {
        @Override
        public boolean test(ExecuteContext menu, PlaceholderApplier placeholders) {
            return true;
        }

        @Override
        public String toString() {
            return "true";
        }

        @Override
        public @NotNull Requirement compile() {
            return this;
        }
    };
    Requirement FALSE = new Requirement() {
        @Override
        public boolean test(ExecuteContext menu, PlaceholderApplier placeholders) {
            return false;
        }

        @Override
        public String toString() {
            return "false";
        }

        @Override
        public @NotNull Requirement compile() {
            return this;
        }
    };

    @Deprecated
   default boolean test(Menu menu, PlaceholderApplier placeholders){
       return test(ExecuteContext.of(menu), placeholders);
   }
   boolean test(ExecuteContext ctx, PlaceholderApplier placeholders);

    default @Nullable Requirement compile() {
        return null;
    }

    default YamlValue encode() {
        return YamlValue.wrap(toString());
    }

    class Codec {
        public static final YamlCodec<Requirement> ANY_OF = codec(false);

        public static final YamlCodec<Requirement> ALL_OF = codec(true);




        public static YamlCodec<Requirement> allOf() {
            return ALL_OF;
        }

        public static YamlCodec<Requirement> oneOf() {
            return ANY_OF;
        }

        private static final YamlCodec<Requirement> FROM_STRING = YamlCodec.of(
                v -> YamlCodec.STRING.decode(v).flatMap(Codec::fromString).flatMap(r -> {
                    var c = r.compile();
                    return c == null ? DataResult.success(r) : DataResult.success(c);
                }),
                Requirement::encode,
                SchemaTypes.ANY
        );

        public static YamlCodec<Requirement> codec(boolean allOf){
            return YamlCodec.recursive(codec -> new CodecSelector<Requirement>(v -> {
                        var c = v.encode();
                        if (c.isMap()){
                            c.asYamlMap().result().set("$type", v.getClass().getSimpleName());
                        }
                        return c;
                    })
                    .add(YamlTester.IF_PRIMITIVE, FROM_STRING)
                    .add(YamlTester.ifKey("requirements"), YamlCodec.lazyLoad(() -> BreakableConditionalHandler.CODEC))
                    .add(YamlTester.ifKey("if", "if-one", "if-all", "check"), YamlCodec.lazyLoad(() -> ConditionalHandler.CODEC))
                    .add(YamlTester.ifKey("oneOf"), YamlCodec.lazyLoad(() -> FirstMatchHandler.CODEC))
                    .add(YamlTester.ifKey("do"), YamlCodec.lazyLoad(() -> ConditionalHandler.CODEC))
                    .add(YamlTester.ifKey("type"), RequirementsFactory::readLegacyType)
                    .add(YamlTester.ifKey("checks"), v -> v.asYamlMap().flatMap(map -> {
                        boolean anyOf = Objects.equals(true, map.getRaw("anyOf"));
                        if (anyOf) return oneOf().decode(map.get("checks"));
                        return allOf().decode(map.get("checks"));
                    }))
                    .add(YamlTester.IF_LIST, v -> v.asList(codec).flatMap(l -> {
                        if (allOf){
                            return DataResult.success(allOf(l));
                        }
                        return DataResult.success(oneOf(l));
                    }))
            );
        }


        public static Requirement allOf(List<Requirement> list) {
            return of(list, true);
        }

        public static Requirement oneOf(List<Requirement> list) {
            return of(list, false);
        }

        static Requirement of(List<Requirement> list, final boolean allOf) {
            if (list.isEmpty()) return TRUE;
            return new Requirement() {
                @Override
                public boolean test(ExecuteContext menu, PlaceholderApplier placeholders) {
                    if (allOf) {
                        for (Requirement requirement : list) {
                            if (!requirement.test(menu, placeholders)) return false;
                        }
                        return true;
                    }
                    for (Requirement requirement : list) {
                        if (requirement.test(menu, placeholders)) return true;
                    }
                    return false;
                }

                @Override
                public YamlValue encode() {
                    List<Object> res = new ArrayList<>();
                    for (Requirement requirement : list) {
                        res.add(requirement.encode().getRaw());
                    }
                    if (allOf) {
                        return YamlValue.wrap(res);
                    }
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("anyOf", true);
                    map.put("checks", res);
                    return YamlValue.wrap(map);
                }

            };
        }

        private static DataResult<Requirement> fromString(String input) {
            if (input.equalsIgnoreCase("true")) {
                return DataResult.success(Requirement.TRUE);
            } else if (input.equalsIgnoreCase("false")) {
                return DataResult.success(Requirement.FALSE);
            }

            if (input.startsWith("has") || input.startsWith("!has")) {
                return DataResult.success(new PermissionRequirement(input.startsWith("!"), input.split(" ")[1]));
            } else if (input.startsWith("nearby") || input.startsWith("!nearby")) {
                return NearbyRequirement.CODEC.decode(YamlValue.wrap(input))
                        .flatMap(DataResult::success);
            } else if (input.startsWith("regex") || input.startsWith("!regex")) {
                //regex[0] \\d+[1] 00[2]
                String[] args = input.split(" ");
                return DataResult.success(new RegexRequirement(
                        input.startsWith("!"),
                        args[1],
                        args[2]
                ));
            }

            String[] args = input.split(" ");
            if (args.length == 3) {
                var op = StringsRequirement.Operation.byOperator(args[1]);
                if (op == null) return DataResult.success(new MathRequirement(input));
                return DataResult.success(new StringsRequirement(op, args[0], args[2]));
            } else {
                return DataResult.success(new MathRequirement(input));
            }
        }
    }
}
