package dev.by1337.bmenu.requirement.legacy;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.requirement.*;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequirementsFactory {
    private static final YamlCodec<Map<String, YamlMap>> STRING_TO_YAML_MAP_CODEC = YamlCodec.mapOf(YamlCodec.STRING, YamlCodec.YAML_MAP);

    //@Deprecated
    public static Requirements readLegacy(YamlValue data) {
        List<LegacyRequirement> requirements = new ArrayList<>();
        Map<String, YamlMap> map = data.decode(STRING_TO_YAML_MAP_CODEC).getOrThrow();
        for (YamlMap value : map.values()) {
            String type = value.get("type").decode(YamlCodec.STRING).getOrThrow();
            var commands = value.get("commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
            var denyCommands = value.get("deny_commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
            switch (type) {
                case "math", "!math", "m", "!m" -> {
                    requirements.add(new LegacyRequirement(new MathRequirement(
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
                    requirements.add(new LegacyRequirement(requirement, commands, denyCommands));
                }
                case "string equals ignorecase", "!string equals ignorecase", "sei", "!sei" -> {
                    StringsRequirement requirement = new StringsRequirement(
                            StringsRequirement.Operation.EQUALS_IGNORE_CASE,
                            value.get("input").asString().getOrThrow(),
                            value.get("output").asString().getOrThrow()
                    );
                    if (type.startsWith("!")) requirement = requirement.invert();
                    requirements.add(new LegacyRequirement(requirement, commands, denyCommands));
                }
                case "string contains", "!string contains", "sc", "!sc" -> {
                    StringsRequirement requirement = new StringsRequirement(
                            StringsRequirement.Operation.CONTAINS,
                            value.get("input").asString().getOrThrow(),
                            value.get("output").asString().getOrThrow()
                    );
                    if (type.startsWith("!")) requirement = requirement.invert();
                    requirements.add(new LegacyRequirement(requirement, commands, denyCommands));
                }
                case "regex matches", "!regex matches", "rm", "!rm" -> {
                    RegexRequirement requirement = new RegexRequirement(
                            type.startsWith("!"),
                            value.get("regex").asString().getOrThrow(),
                            value.get("input").asString().getOrThrow()
                    );
                    requirements.add(new LegacyRequirement(requirement, commands, denyCommands));
                }
                case "has permission", "!has permission", "hp", "!hp" -> {
                    PermissionRequirement requirement = new PermissionRequirement(
                            type.startsWith("!") ? PermissionRequirement.Operation.YES : PermissionRequirement.Operation.NO,
                            value.get("permission").asString().getOrThrow()
                    );
                    requirements.add(new LegacyRequirement(requirement, commands, denyCommands));
                }
                default -> {
                    throw new IllegalArgumentException("unknown type " + type);
                }
            }
        }
        return new Requirements(requirements);
    }
}
