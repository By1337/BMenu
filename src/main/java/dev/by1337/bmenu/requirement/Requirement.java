package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.menu.Menu;
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
        public boolean test(Menu menu, PlaceholderApplier placeholders) {
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
        public boolean test(Menu menu, PlaceholderApplier placeholders) {
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

    boolean test(Menu menu, PlaceholderApplier placeholders);

    default @Nullable Requirement compile() {
        return null;
    }

    default YamlValue encode() {
        return YamlValue.wrap(toString());
    }

    class Codec {
        public static final YamlCodec<Requirement> ANY_OF = new YamlCodec<>() {
            @Override
            public DataResult<Requirement> decode(YamlValue yaml) {
                if (yaml.isPrimitive()) return STRING.decode(yaml)
                        .flatMap(Codec::fromString)
                        .flatMap(r -> {
                            var v = r.compile();
                            return v == null ? DataResult.success(r) : DataResult.success(v);
                        });
                if (yaml.isMap()) {
                    YamlMap map = yaml.asYamlMap().getOrThrow();
                    if (map.has("checks")) {
                        boolean anyOf = Objects.equals(true, map.getRaw("anyOf"));
                        var codec = anyOf ? oneOf() : allOf();
                        return codec.decode(map.get("checks"));
                    }
                    return DataResult.error("Expected a String, but found Map.");
                }
                return yaml.asList(this).flatMap(l -> DataResult.success(oneOf(l)));
            }

            @Override
            public YamlValue encode(Requirement requirement) {
                return requirement.encode();
            }

            @Override
            public @NotNull SchemaType schema() {
                return SchemaTypes.STRING;
            }
        };

        public static final YamlCodec<Requirement> ALL_OF = new YamlCodec<>() {
            @Override
            public DataResult<Requirement> decode(YamlValue yaml) {
                if (yaml.isPrimitive()) return STRING.decode(yaml)
                        .flatMap(Codec::fromString)
                        .flatMap(r -> {
                            var v = r.compile();
                            return v == null ? DataResult.success(r) : DataResult.success(v);
                        });
                if (yaml.isMap()) {
                    YamlMap map = yaml.asYamlMap().getOrThrow();
                    if (map.has("checks")) {
                        boolean anyOf = Objects.equals(true, map.getRaw("anyOf"));
                        var codec = anyOf ? oneOf() : allOf();
                        return codec.decode(map.get("checks"));
                    }
                    return DataResult.error("Expected a String, but found Map.");
                }
                return yaml.asList(this).flatMap(l -> DataResult.success(allOf(l)));
            }

            @Override
            public YamlValue encode(Requirement requirement) {
                return requirement.encode();
            }

            @Override
            public @NotNull SchemaType schema() {
                return SchemaTypes.STRING;
            }
        };

        public static YamlCodec<Requirement> allOf() {
            return ALL_OF;
        }

        public static YamlCodec<Requirement> oneOf() {
            return ANY_OF;
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
                public boolean test(Menu menu, PlaceholderApplier placeholders) {
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
                PermissionRequirement.Operation op;
                if (input.startsWith("!")) op = PermissionRequirement.Operation.NO;
                else op = PermissionRequirement.Operation.YES;
                return DataResult.success(new PermissionRequirement(op, input.split(" ")[1]));
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
