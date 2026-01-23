package dev.by1337.bmenu.command;


import dev.by1337.bmenu.event.MenuEventHandler;
import dev.by1337.bmenu.requirement.LegacyRequirement;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Commands implements MenuEventHandler {
    public static final Commands EMPTY = new Commands(List.of());

    public static final YamlCodec<Commands> CODEC = new YamlCodec<>() {
        private final YamlCodec<List<Commands>> COMMANDS = YamlCodec.lazyLoad(() -> thisCodec().listOf());
        private final YamlCodec<Map<String, String>> S2S_MAP = YamlCodec.mapOf(STRING, MULTI_LINE_STRING);
        private final YamlCodec<Map<String, YamlValue>> S2OBJET_MAP = YamlCodec.mapOf(STRING, YAML_VALUE);

        @Override
        public DataResult<Commands> decode(YamlValue yaml) {
            if (yaml.isPrimitive()) return STRING.decode(yaml).mapValue(s -> new Commands(List.of(fixCommand(s))));
            if (yaml.isList()) {
                return COMMANDS.decode(yaml).mapValue(Commands::fromCommandsList);
            }
            return S2OBJET_MAP.decode(yaml).flatMap(map -> {
                if (map.containsKey("if")) {
                    return allOf(
                            LegacyRequirement::new,
                            map.get("if").decode(Requirement.CODEC),
                            YamlValue.wrap(map.get("do")).decode(CODEC, Commands.EMPTY),
                            YamlValue.wrap(map.get("else")).decode(CODEC, Commands.EMPTY)
                    ).mapValue(Commands::new);
                } else {
                    List<String> commands = new ArrayList<>();
                    StringBuilder buffer = new StringBuilder();
                    for (String cmd : map.keySet()) {
                        buffer.append("[").append(cmd).append("]");
                        var value = map.get(cmd).decode(MULTI_LINE_STRING).getOrThrow();
                        if (!value.isBlank()) {
                            buffer.append(" ").append(value.replace("\n", "<br><reset>"));
                        }
                        commands.add(buffer.toString());
                        buffer.setLength(0);
                    }
                    return DataResult.success(new Commands(commands));
                }
            });
        }

        private static YamlCodec<Commands> thisCodec() {
            return CODEC;
        }

        private String fixCommand(String input) {
            if (input.startsWith("[")) return input;
            return "[message] " + input.replace("\n", "<br><reset>");
        }

        @Override
        public YamlValue encode(Commands commands) {
            List<Object> out = new ArrayList<>();
            for (CommandLike command : commands.commands) {
                if (command instanceof MenuCommand mc) {
                    out.add(mc.source());
                } else if (command instanceof LegacyRequirement r) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("if", r.requirement().encode().getRaw());
                    if (!r.commands().isEmpty()) {
                        map.put("do", encode(r.commands()).getRaw());
                    }
                    if (!r.denyCommands().isEmpty()) {
                        map.put("do", encode(r.denyCommands()).getRaw());
                    }
                    out.add(map);
                }
            }
            return YamlValue.wrap(out);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SchemaTypes.ANY;
        }
    };
    private final List<CommandLike> commands;
    private boolean hasBreak;

    @ApiStatus.Experimental
    public Commands(LegacyRequirement requirement) {
        commands = List.of(requirement);
    }

    public Commands(List<CommandLike> commands, boolean hasBreak) {
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
    public void run(ExecuteContext ctx, PlaceholderApplier placeholders) {
        for (CommandLike like : commands) {
            like.run(ctx, placeholders);
        }
    }

    public static Commands fromCommandsList(List<Commands> list) {
        List<CommandLike> result = new ArrayList<>();
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

    private static <T, T1, T2, R> DataResult<R> allOf(RecordYamlCodecBuilder.Function3<T, T1, T2, R> f, DataResult<T> r, DataResult<T1> r1, DataResult<T2> r2) {
        final T t;
        final T1 t1;
        final T2 t2;
        if (r.hasResult()) {
            t = r.result();
          //  if (r.hasError()) err.append(r.error()).append("\n");
        } else {
            return (DataResult<R>) r;
        }
        if (r1.hasResult()) {
            t1 = r1.result();
           // if (r1.hasError()) err.append(r1.error()).append("\n");
        } else {
            return (DataResult<R>) r1;
        }
        if (r2.hasResult()) {
            t2 = r2.result();
          //  if (r2.hasError()) err.append(r2.error()).append("\n");
        } else {
            return (DataResult<R>) r2;
        }
        try {
            R res = f.apply(t, t1, t2);
            return DataResult.success(res);
           // if (err.isEmpty()) return DataResult.success(res);
           // err.setLength(err.length() - 1);
           // return DataResult.error(err.toString()).partial(res);
        } catch (Exception e) {
            return DataResult.error(e);
        }
    }
}
