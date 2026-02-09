package dev.by1337.bmenu.command;


import dev.by1337.bmenu.handler.ConditionalHandler;
import dev.by1337.bmenu.handler.FirstMatchHandler;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class Commands implements MenuEventHandler {
    public static final Commands EMPTY = new Commands(List.of());

    public static final YamlCodec<Commands> CODEC = YamlCodec.recursive(codec -> new YamlCodec<Commands>() {
        private final YamlCodec<Map<String, YamlValue>> S2OBJET_MAP = YamlCodec.mapOf(STRING, YAML_VALUE);

        @Override
        public DataResult<Commands> decode(YamlValue yaml) {
            if (yaml.isPrimitive()) return STRING.decode(yaml).mapValue(s -> new Commands(List.of(fixCommand(s))));
            if (yaml.isList()) {
                return yaml.asList(codec).mapValue(Commands::fromCommandsList);
            }
            return S2OBJET_MAP.decode(yaml).flatMap(map -> {
                List<MenuEventHandler> handlers = new ArrayList<>();
                StringBuilder err = new StringBuilder();
                if (
                        map.containsKey("if") ||
                                map.containsKey("if-all") ||
                                map.containsKey("if-one")
                ) {
                    tryDecode(yaml, ConditionalHandler.CODEC, err, handlers::add);
                }
                if (map.containsKey("oneOf")) {
                    tryDecode(map.get("oneOf"), FirstMatchHandler.CODEC, err, handlers::add);
                }
                boolean hasBreak = false;
                StringBuilder buffer = new StringBuilder();
                for (String cmd : map.keySet()) {
                    if (cmd.startsWith("if") || cmd.equals("oneOf") || cmd.equals("do") || cmd.equals("else")) continue;
                    buffer.append("[").append(cmd).append("]");
                    var value = map.get(cmd).decode(MULTI_LINE_STRING).getOrThrow();
                    if (!value.isBlank()) {
                        buffer.append(" ").append(value.replace("\n", "<br><reset>"));
                    }
                    var s = buffer.toString();
                    if (s.equalsIgnoreCase("[break]")) {
                        hasBreak = true;
                        break;
                    }
                    handlers.add(new MenuCommand(buffer.toString()));
                    buffer.setLength(0);
                }
                var res = new Commands(handlers, hasBreak);
                if (err.isEmpty()) return DataResult.success(res);
                err.setLength(err.length() - 1);
                return DataResult.error(err.toString()).partial(res);
            });
        }

        private static <T> void tryDecode(YamlValue v, YamlCodec<T> codec, StringBuilder err, Consumer<T> c) {
            var res = codec.decode(v);
            if (res.hasError()) {
                err.append(res.error()).append("\n");
            }
            if (res.hasResult()) {
                c.accept(res.result());
            }
        }

        @Override
        public YamlValue encode(Commands commands) {
            List<Object> list = new ArrayList<>();
            for (MenuEventHandler command : commands.commands) {
                list.add(command.encode().getRaw());
            }
            return YamlValue.wrap(list);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SchemaTypes.ANY;
        }

        private String fixCommand(String input) {
            if (input.startsWith("[")) return input;
            return "[message] " + input.replace("\n", "<br><reset>");
        }
    });
    private final List<MenuEventHandler> commands;
    private boolean hasBreak;

    @ApiStatus.Experimental
    public Commands(ConditionalHandler requirement) {
        commands = List.of(requirement);
    }

    public Commands(List<MenuEventHandler> commands, boolean hasBreak) {
        this.commands = commands;
        this.hasBreak = hasBreak;
    }

    public Commands(List<String> list) {
        commands = new ArrayList<>();
        for (String s : list) {
            if (s.equalsIgnoreCase("[break]")) {
                hasBreak = true;
                break;
            }
            commands.add(new MenuCommand(s));
        }
    }

    @Override
    public boolean run(ExecuteContext ctx, PlaceholderApplier placeholders) {
        boolean state = true;
        for (MenuEventHandler like : commands) {
            if (!like.run(ctx, placeholders)) {
                state = false;
            }
        }
        return state;
    }

    public static Commands fromCommandsList(List<Commands> list) {
        List<MenuEventHandler> result = new ArrayList<>();
        boolean hasBreak = false;
        for (Commands commands : list) {
            result.addAll(commands.commands);
            //noinspection all
            if (hasBreak = commands.isHasBreak()) {
                break;
            }
        }
        return new Commands(result, hasBreak);
    }

    public boolean isHasBreak() {
        return hasBreak;
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public YamlValue encode() {
        return CODEC.encode(this);
    }


    @Override
    public String toString() {
        return "Commands{" +
                "commands=" + commands +
                ", hasBreak=" + hasBreak +
                '}';
    }

}
//    public static final YamlCodec<Commands> CODEC = new YamlCodec<>() {
//        private final YamlCodec<List<Commands>> COMMANDS = YamlCodec.lazyLoad(() -> thisCodec().listOf());
//        private final YamlCodec<Map<String, YamlValue>> S2OBJET_MAP = YamlCodec.mapOf(STRING, YAML_VALUE);
//
//        @Override
//        public DataResult<Commands> decode(YamlValue yaml) {
//            if (yaml.isPrimitive()) return STRING.decode(yaml).mapValue(s -> new Commands(List.of(fixCommand(s))));
//            if (yaml.isList()) {
//                return COMMANDS.decode(yaml).mapValue(Commands::fromCommandsList);
//            }
//            return S2OBJET_MAP.decode(yaml).flatMap(map -> {
//                if (map.containsKey("if")) {
//                    StringBuilder err = new StringBuilder();
//                    List<MenuEventHandler> handlers = new ArrayList<>();
//                    var v = allOf(
//                            ConditionalHandler::new,
//                            map.get("if").decode(Requirement.CODEC),
//                            YamlValue.wrap(map.get("do")).decode(CODEC, Commands.EMPTY),
//                            YamlValue.wrap(map.get("else")).decode(CODEC, Commands.EMPTY)
//                    ).mapValue(Commands::new);
//                    if (v.hasError()) err.append(v.error()).append("\n");
//                    if (v.hasResult()) handlers.add(v.result());
//                    for (String s : map.keySet()) {
//                        if (s.equals("if") || s.equals("else") || s.equals("do")) continue;
//                        var data = CODEC.decode(map.get(s));
//                        if (data.hasError()) err.append(data.error()).append("\n");
//                        if (data.hasResult()) handlers.add(data.result());
//                    }
//                    if (err.isEmpty()) return DataResult.success(new Commands(handlers, false));
//                    err.setLength(err.length() - 1);
//                    return DataResult.error(err.toString()).partial(new Commands(handlers, false));
//                } else {
//                    List<String> commands = new ArrayList<>();
//                    StringBuilder buffer = new StringBuilder();
//                    for (String cmd : map.keySet()) {
//                        buffer.append("[").append(cmd).append("]");
//                        var value = map.get(cmd).decode(MULTI_LINE_STRING).getOrThrow();
//                        if (!value.isBlank()) {
//                            buffer.append(" ").append(value.replace("\n", "<br><reset>"));
//                        }
//                        commands.add(buffer.toString());
//                        buffer.setLength(0);
//                    }
//                    return DataResult.success(new Commands(commands));
//                }
//            });
//        }
//
//        private static YamlCodec<Commands> thisCodec() {
//            return CODEC;
//        }
//
//        private String fixCommand(String input) {
//            if (input.startsWith("[")) return input;
//            return "[message] " + input.replace("\n", "<br><reset>");
//        }
//
//        @Override
//        public YamlValue encode(Commands commands) {
//            List<Object> out = new ArrayList<>();
//            for (MenuEventHandler command : commands.commands) {
//                if (command instanceof MenuCommand mc) {
//                    out.add(mc.source());
//                } else if (command instanceof ConditionalHandler r) {
//                    Map<String, Object> map = new LinkedHashMap<>();
//                    map.put("if", r.req().encode().getRaw());
//                    if (!r.doCmds().isEmpty()) {
//                        map.put("do", encode(r.elseCmds()).getRaw());
//                    }
//                    if (!r.elseCmds().isEmpty()) {
//                        map.put("do", encode(r.doCmds()).getRaw());
//                    }
//                    out.add(map);
//                }
//            }
//            return YamlValue.wrap(out);
//        }
//
//        @Override
//        public @NotNull SchemaType schema() {
//            return SchemaTypes.ANY;
//        }
//    };

//    @SuppressWarnings("unchecked")
//    private static <T, T1, T2, R> DataResult<R> allOf(RecordYamlCodecBuilder.Function3<T, T1, T2, R> f, DataResult<T> r, DataResult<T1> r1, DataResult<T2> r2) {
//        final T t;
//        final T1 t1;
//        final T2 t2;
//        if (r.hasResult()) {
//            t = r.result();
//        } else {
//            return (DataResult<R>) r;
//        }
//        if (r1.hasResult()) {
//            t1 = r1.result();
//        } else {
//            return (DataResult<R>) r1;
//        }
//        if (r2.hasResult()) {
//            t2 = r2.result();
//        } else {
//            return (DataResult<R>) r2;
//        }
//        try {
//            R res = f.apply(t, t1, t2);
//            return DataResult.success(res);
//        } catch (Exception e) {
//            return DataResult.error(e);
//        }
//    }