package dev.by1337.bmenu.command;


import dev.by1337.cmd.Command;
import dev.by1337.cmd.CommandReader;
import dev.by1337.cmd.CompiledCommand;
import dev.by1337.plc.Placeholderable;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.JsonSchemaTypeBuilder;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;


public class Commands {
    public static final Commands EMPTY = new Commands(List.of());
    public static final YamlCodec<Commands> CODEC = new YamlCodec<Commands>() {
        private final SchemaType schemaType = make(() -> {
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
            var strOrMap = SchemaTypes.anyOf(SchemaTypes.STRING, builder.build());
            return SchemaTypes.anyOf(strOrMap, SchemaTypes.array(strOrMap));
        });
        private final YamlCodec<List<YamlValue>> OBJ_LIST = YamlCodec.YAML_VALUE.listOf();

        @Override
        public DataResult<Commands> decode(YamlValue yamlValue) {
            if (yamlValue.isPrimitive())
                return STRING.decode(yamlValue).mapValue(s -> {
                    if (s.startsWith("[")) {
                        return new Commands(List.of(s));
                    } else {
                        return new Commands(List.of("[message] " + s));
                    }
                });
            if (yamlValue.isMap()) return MenuCodecs.MAP_TO_LIST.decode(yamlValue).mapValue(Commands::new);
            return OBJ_LIST.decode(yamlValue).flatMap(list -> {
                StringBuilder error = new StringBuilder();
                List<String> result = new ArrayList<>();
                for (YamlValue value : list) {
                    var res = this.decode(value);
                    if (res.hasError()) {
                        error.append(res.error()).append("\n");
                    }
                    if (res.hasResult()) {
                        Commands c = res.result();
                        result.addAll(c.list);
                    }
                }
                if (!error.isEmpty()) {
                    error.setLength(error.length() - 1);
                    return DataResult.error(error.toString()).partial(new Commands(result));
                }
                return DataResult.success(new Commands(result));
            });
        }

        @Override
        public YamlValue encode(Commands commands) {
            return YamlValue.wrap(commands.list);
        }

        @Override
        public @NotNull SchemaType schema() {
            return schemaType;
        }
    };
    private static final Logger log = LoggerFactory.getLogger(Commands.class);
    private final List<String> list;
    private final List<MenuCommand> compiled = new ArrayList<>();
    private boolean hasBreak;

    public Commands(List<String> list) {
        this.list = list;
        for (String s : list) {
            if (s.equalsIgnoreCase("[break]")) {
                hasBreak = true;
                break;
            }
            compiled.add(new MenuCommand(s));
        }
    }

    public void run(ExecuteContext menu, Placeholderable placeholders) {
        for (MenuCommand menuCommand : compiled) {
            menuCommand.run(menu, placeholders);
        }
    }

    public List<String> list() {
        return list;
    }

    private static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public boolean isHasBreak() {
        return hasBreak;
    }

    public boolean isEmpty() {
        return compiled.isEmpty();
    }
}
