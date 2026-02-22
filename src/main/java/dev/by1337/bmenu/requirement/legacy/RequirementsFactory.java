package dev.by1337.bmenu.requirement.legacy;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.handler.ConditionalHandler;
import dev.by1337.bmenu.requirement.MathRequirement;
import dev.by1337.bmenu.requirement.PermissionRequirement;
import dev.by1337.bmenu.requirement.RegexRequirement;
import dev.by1337.bmenu.requirement.StringsRequirement;
import dev.by1337.bmenu.yaml.dfu.BMenuDFU;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequirementsFactory {
    private static final YamlCodec<Map<String, YamlMap>> STRING_TO_YAML_MAP_CODEC = YamlCodec.mapOf(YamlCodec.STRING, YamlCodec.YAML_MAP);

    //@Deprecated
    public static Requirements readLegacy2(YamlValue data) {
        return new Requirements(readLegacy(data));
    }
    public static List<ConditionalHandler> readLegacy(YamlValue data) {
        if (data.isMap()){
            return List.of(readLegacyType(data).getOrThrow());
        }
        List<ConditionalHandler> requirements = new ArrayList<>();
        Map<String, YamlMap> map = data.decode(STRING_TO_YAML_MAP_CODEC).getOrThrow();
        for (YamlMap value : map.values()) {
            requirements.add(readLegacyType(value.get()).getOrThrow());
        }
        return requirements;
    }
    public static DataResult<ConditionalHandler> readLegacyType(YamlValue v){
        return BMenuDFU.COMMANDS_KEY_RENAMER.apply(v).asYamlMap().flatMap(value -> {
            var res = value.get("type").decode(YamlCodec.STRING);
            if (!res.hasResult()) return DataResult.error(res.error());
            String type = res.result();
            var commands = value.get("do").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
            var denyCommands = value.get("else").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
            switch (type) {
                case "math", "!math", "m", "!m" -> {
                    return DataResult.success(new ConditionalHandler(new MathRequirement(
                            value.get("expression").asString().getOrThrow()
                    ), commands, denyCommands));
                }
                case "string equals", "!string equals", "se", "!se" -> {
                    StringsRequirement requirement = new StringsRequirement(
                            StringsRequirement.Operation.EQUALS,
                            value.get("input").asString().getOrThrow(),
                            value.get("output").asString().getOrThrow()
                    );
                    if (type.startsWith("!")) requirement = requirement.invert();
                    return DataResult.success(new ConditionalHandler(requirement, commands, denyCommands));
                }
                case "string equals ignorecase", "!string equals ignorecase", "sei", "!sei" -> {
                    StringsRequirement requirement = new StringsRequirement(
                            StringsRequirement.Operation.EQUALS_IGNORE_CASE,
                            value.get("input").asString().getOrThrow(),
                            value.get("output").asString().getOrThrow()
                    );
                    if (type.startsWith("!")) requirement = requirement.invert();
                    return DataResult.success(new ConditionalHandler(requirement, commands, denyCommands));
                }
                case "string contains", "!string contains", "sc", "!sc" -> {
                    StringsRequirement requirement = new StringsRequirement(
                            StringsRequirement.Operation.CONTAINS,
                            value.get("input").asString().getOrThrow(),
                            value.get("output").asString().getOrThrow()
                    );
                    if (type.startsWith("!")) requirement = requirement.invert();
                    return DataResult.success(new ConditionalHandler(requirement, commands, denyCommands));
                }
                case "regex matches", "!regex matches", "rm", "!rm" -> {
                    RegexRequirement requirement = new RegexRequirement(
                            type.startsWith("!"),
                            value.get("regex").asString().getOrThrow(),
                            value.get("input").asString().getOrThrow()
                    );
                    return DataResult.success(new ConditionalHandler(requirement, commands, denyCommands));
                }
                case "has permission", "!has permission", "hp", "!hp" -> {
                    PermissionRequirement requirement = new PermissionRequirement(
                            type.startsWith("!"),
                            value.get("permission").asString().getOrThrow()
                    );
                    return DataResult.success(new ConditionalHandler(requirement, commands, denyCommands));
                }
                default -> {
                    return DataResult.error("unknown type " + type);
                }
            }
        });


    }
}
