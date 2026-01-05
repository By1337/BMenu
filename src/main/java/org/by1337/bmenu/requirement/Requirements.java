package org.by1337.bmenu.requirement;

import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.entity.Player;
import org.by1337.bmenu.CommandRunner;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.factory.RequirementsFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Requirements {
    public static final YamlCodec<Requirement> REQUIREMENT_CODEC = new YamlCodec<Requirement>() {
        private final SchemaType schemaType = SchemaTypes.OBJECT
                .asBuilder()
                .properties("check", SchemaTypes.STRING)
                .properties("commands", MenuCodecs.COMMANDS.schema())
                .properties("deny_commands", MenuCodecs.COMMANDS.schema())
                .additionalProperties(false)
                .required("check")
                .build();

        @Override
        public DataResult<Requirement> decode(YamlValue yamlValue) {
            YamlMap yaml = yamlValue.asYamlMap().getOrThrow();
            try {
                Requirement requirement = readRequirement(yaml);
                if (requirement.compilable()) {
                    requirement = requirement.compile();
                    boolean state = requirement.state();
                    if (requirement.isNOP()) {
                        yaml.getRaw().clear();
                        yaml.set("check", "NOP");
                    } else {
                        yaml.set("check", state);
                        if (state) {
                            yaml.set("deny_commands", null);
                        } else {
                            yaml.set("commands", null);
                        }
                    }
                }
                return DataResult.success(requirement);
            } catch (Exception e) {
                return DataResult.error(e.getMessage());
            }

        }

        @Override
        public YamlValue encode(Requirement requirement) {
            return YamlValue.wrap("unsupported");//todo
        }

        @Override
        public @NotNull SchemaType schema() {
            return schemaType;
        }
    };

    private static Requirement readRequirement(YamlMap yaml) {
        String check = yaml.get("check").decode(YamlCodec.STRING).getOrThrow();
        List<String> commands = yaml.get("commands").decode(MenuCodecs.COMMANDS, List.of()).getOrThrow();
        List<String> denyCommands = yaml.get("deny_commands").decode(MenuCodecs.COMMANDS, List.of()).getOrThrow();
        if (check.equalsIgnoreCase("true") || check.equalsIgnoreCase("false")) {
            yaml.set("$check-type", "flag");
            return new FlagRequirements(Boolean.parseBoolean(check), commands, denyCommands);
        }
        if (check.startsWith("has") || check.startsWith("!has")) {
            yaml.set("$check-type", "has permission");
            return new HasPermissionRequirement(
                    check.split(" ")[1],
                    check.startsWith("!"),
                    commands,
                    denyCommands
            );
        } else if (check.startsWith("nearby") || check.startsWith("!nearby")) {
            String[] args = check.split(" ");
            if (args.length != 6) {
                throw new IllegalArgumentException("The condition expected 'nearby <world> <x> <y> <z> <radius>' but got '" + check + "'.");
            }
            yaml.set("$check-type", "nearby");
            return new NearbyRequirement(
                    args[1],
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]),
                    check.startsWith("!"),
                    commands,
                    denyCommands
            );
        } else {
            String[] args = check.split(" ");
            if (args.length == 3) {
                String operator = args[1];
                switch (operator) {
                    case "has", "!has" -> {
                        yaml.set("$check-type", "string contains");
                        return new StringContainsRequirement(
                                args[0],
                                args[2],
                                operator.startsWith("!"),
                                commands,
                                denyCommands
                        );
                    }
                    case "HAS", "!HAS" -> {
                        yaml.set("$check-type", "string equals ignorecase");
                        return new StringEqualsIgnoreCaseRequirement(
                                args[0],
                                args[2],
                                operator.startsWith("!"),
                                commands,
                                denyCommands
                        );
                    }
                    case "==", "!=" -> {
                        yaml.set("$check-type", "string equals");
                        return new StringEqualsRequirement(
                                args[0],
                                args[2],
                                operator.startsWith("!"),
                                commands,
                                denyCommands
                        );
                    }
                    default -> {
                        yaml.set("$check-type", "math");
                        return new MathRequirement(
                                check,
                                commands,
                                denyCommands
                        );
                    }
                }
            } else {
                yaml.set("$check-type", "math");
                return new MathRequirement(
                        check,
                        commands,
                        denyCommands
                );
            }
        }
    }

    public static final YamlCodec<Requirements> CODEC = new YamlCodec<>() {
        final SchemaType schemaType = REQUIREMENT_CODEC.schema().listOf();
        final YamlCodec<List<Requirement>> LIST_CODEC = REQUIREMENT_CODEC.listOf();

        @Override
        public DataResult<Requirements> decode(YamlValue yamlValue) {
            if (yamlValue.isMap()) return DataResult.success(RequirementsFactory.readLegacy(yamlValue));
            return yamlValue.decode(LIST_CODEC).mapValue(Requirements::new);
        }

        @Override
        public YamlValue encode(Requirements requirements) {
            return LIST_CODEC.encode(requirements.getRequirements());
        }

        @Override
        public @NotNull SchemaType schema() {
            return schemaType;
        }
    };

    public static final Requirements EMPTY = new Requirements(Collections.emptyList());
    private final List<Requirement> requirements;

    public Requirements(List<Requirement> requirements) {
        this.requirements = requirements.stream().filter(r -> !r.isNOP()).toList();
    }


    public boolean test(Menu menu, PlaceholderResolver<Menu> placeholders, Player clicker) {
        return test(menu, placeholders, clicker, menu);
    }

    public boolean test(Menu menu, PlaceholderResolver<Menu> placeholders, Player clicker, CommandRunner commandRunner) {
        boolean result = true;
        for (Requirement requirement : requirements) {
            try {
                if (!requirement.test(menu, placeholders, clicker)) {
                    if (runCommands(requirement.getDenyCommands(), placeholders, commandRunner, menu)) {
                        return false;
                    }
                    result = false;
                } else {
                    if (runCommands(requirement.getCommands(), placeholders, commandRunner, menu)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                menu.getLoader().getLogger().error("Failed to check requirement: {}", requirement, e);
            }
        }
        return result;
    }

    private boolean runCommands(List<String> commands, PlaceholderResolver<Menu> placeholders, CommandRunner commandRunner, Menu menu) {
        for (String command : commands) {
            if (command.equals("[BREAK]") || command.equals("[break]")) return true;
            commandRunner.executeCommand(placeholders.replace(command, menu));
        }
        return false;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }


    public static Requirements empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return requirements.isEmpty();
    }

}