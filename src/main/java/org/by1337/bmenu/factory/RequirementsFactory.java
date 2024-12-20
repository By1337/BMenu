package org.by1337.bmenu.factory;

import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.requirement.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementsFactory {
    public static Requirements read(YamlValue ctx, MenuLoader loader) {
        if (ctx.getValue() == null) return Requirements.EMPTY;
        List<Requirement> requirements = new ArrayList<>();
        if (ctx.isMap()) {
            for (YamlContext context : ctx.getAsMap(YamlValue::getAsYamlContext).values()) {
                String type = context.getAsString("type");
                RequirementType requirementType = RequirementType.byName(type);
                if (requirementType == null) {
                    throw new IllegalArgumentException("unknown requirement type: " + type);
                }
                requirements.add(requirementType.fromYaml.createRequirement(context));
            }
        } else {
            List<YamlContext> list = ctx.getAsList(YamlContext.class);
            for (YamlContext context : list) {
                String check = context.getAsString("check");
                List<String> commands = context.getList("commands", String.class, Collections.emptyList());
                List<String> denyCommands = context.getList("deny_commands", String.class, Collections.emptyList());
                if (check.startsWith("has") || check.startsWith("!has")) {
                    requirements.add(new HasPermisionRequirement(
                            check.split(" ")[1],
                            check.startsWith("!"),
                            commands,
                            denyCommands
                    ));
                } else {
                    String[] args = check.split(" ");
                    if (args.length == 3) {
                        String operator = args[1];
                        switch (operator) {
                            case "has", "!has" -> requirements.add(new StringContainsRequirement(
                                    args[0],
                                    args[2],
                                    operator.startsWith("!"),
                                    commands,
                                    denyCommands
                            ));
                            case "HAS", "!HAS" -> requirements.add(new StringEqualsIgnoreCaseRequirement(
                                    args[0],
                                    args[2],
                                    operator.startsWith("!"),
                                    commands,
                                    denyCommands
                            ));
                            case "==", "!=" -> requirements.add(new StringEqualsRequirement(
                                    args[0],
                                    args[2],
                                    operator.startsWith("!"),
                                    commands,
                                    denyCommands
                            ));
                            default -> requirements.add(new MathRequirement(
                                    check,
                                    commands,
                                    denyCommands
                            ));
                        }
                    } else {
                        requirements.add(new MathRequirement(
                                check,
                                commands,
                                denyCommands
                        ));
                    }
                }
            }
        }
        return new Requirements(requirements);
    }
}
