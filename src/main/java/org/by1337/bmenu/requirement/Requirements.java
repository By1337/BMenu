package org.by1337.bmenu.requirement;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.factory.RequirementsFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Requirements {
    public static final YamlCodec<Requirement> REQUIREMENT_CODEC = new YamlCodec<Requirement>() {
        private final SchemaType schemaType = SchemaTypes.OBJECT
                .asBuilder()
                .properties("check", SchemaTypes.STRING)
                .properties("commands", SchemaTypes.STRINGS)
                .properties("deny_commands", SchemaTypes.STRINGS)
                .additionalProperties(false)
                .required("check")
                .build();

        @Override
        public Requirement decode(YamlValue yamlValue) {
            YamlMap yaml = yamlValue.getAsYamlMap();
            String check = yaml.get("check").getAsString();
            List<String> commands = yaml.get("commands").decode(YamlCodec.STRINGS, List.of());
            List<String> denyCommands = yaml.get("deny_commands").decode(YamlCodec.STRINGS, List.of());
            if (check.startsWith("has") || check.startsWith("!has")) {
                yaml.setRaw("$check-type", "has permission");
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
                yaml.setRaw("$check-type", "nearby");
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
                            yaml.setRaw("$check-type", "string contains");
                            return new StringContainsRequirement(
                                    args[0],
                                    args[2],
                                    operator.startsWith("!"),
                                    commands,
                                    denyCommands
                            );
                        }
                        case "HAS", "!HAS" -> {
                            yaml.setRaw("$check-type", "string equals ignorecase");
                            return new StringEqualsIgnoreCaseRequirement(
                                    args[0],
                                    args[2],
                                    operator.startsWith("!"),
                                    commands,
                                    denyCommands
                            );
                        }
                        case "==", "!=" -> {
                            yaml.setRaw("$check-type", "string equals");
                            return new StringEqualsRequirement(
                                    args[0],
                                    args[2],
                                    operator.startsWith("!"),
                                    commands,
                                    denyCommands
                            );
                        }
                        default -> {
                            yaml.setRaw("$check-type", "math");
                            return new MathRequirement(
                                    check,
                                    commands,
                                    denyCommands
                            );
                        }
                    }
                } else {
                    yaml.setRaw("$check-type", "math");
                    return new MathRequirement(
                            check,
                            commands,
                            denyCommands
                    );
                }
            }
        }

        @Override
        public YamlValue encode(Requirement requirement) {
            return YamlValue.wrap("unsupported"); //todo
        }

        @Override
        public @NotNull SchemaType schema() {
            return schemaType;
        }
    };

    public static final YamlCodec<Requirements> CODEC = new YamlCodec<>() {
        final SchemaType schemaType = REQUIREMENT_CODEC.schema().listOf();
        final YamlCodec<List<Requirement>> LIST_CODEC = REQUIREMENT_CODEC.listOf();

        @Override
        public Requirements decode(YamlValue yamlValue) {
            if (yamlValue.isMap()) return RequirementsFactory.readLegacy(yamlValue);
            return new Requirements(yamlValue.decode(LIST_CODEC));
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
        this.requirements = Collections.unmodifiableList(requirements);
    }


    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        boolean result = true;
        for (Requirement requirement : requirements) {
            try {
                if (!requirement.test(menu, placeholderable, clicker)) {
                    if (runCommands(requirement.getDenyCommands(), menu, placeholderable)){
                        return false;
                    }
                    result = false;
                } else {
                    if (runCommands(requirement.getCommands(), menu, placeholderable)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                menu.getLoader().getLogger().error("Failed to check requirement: {}", requirement, e);
            }
        }
        return result;
    }

    private boolean runCommands(List<String> commands, Menu menu, Placeholderable placeholderable) {
        for (String command : commands) {
            if (command.equals("[BREAK]") || command.equals("[break]")) return true;
            menu.runCommands(List.of(placeholderable.replace(command)));
        }
        return false;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }


    public static Requirements empty() {
        return EMPTY;
    }

    public boolean isEmpty(){
        return requirements.isEmpty();
    }

}