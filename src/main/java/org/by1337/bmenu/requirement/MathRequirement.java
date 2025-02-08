package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.util.ObjectUtil;
import org.by1337.bmenu.util.math.MathParserType;

import java.util.Collections;
import java.util.List;

public class MathRequirement implements Requirement {

    private final String expression;
    private final boolean not;
    private final List<String> commands;
    private final List<String> denyCommands;

    public MathRequirement(String expression, List<String> commands, List<String> denyCommands) {
        this.expression = expression;
        this.commands = commands;
        not = false;
        this.denyCommands = denyCommands;
    }

    public MathRequirement(YamlContext context) {
        expression = context.getAsString("expression");
        not = context.getAsString("type").startsWith("!");
        commands = ObjectUtil.mapIfNotNullOrDefault(context.get("commands").getValue(),
                value -> ((List<?>) value).stream()
                        .map(v -> YamlValue.wrap(v).getAsString()).toList(),
                Collections.emptyList()
        );
        denyCommands = ObjectUtil.mapIfNotNullOrDefault(context.get("deny_commands").getValue(),
                value -> ((List<?>) value).stream()
                        .map(v -> YamlValue.wrap(v).getAsString()).toList(),
                Collections.emptyList()
        );
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        try {
            String s = placeholderable.replace(expression);
            MathParserType parserType = s.contains(".") ? MathParserType.DOUBLE : MathParserType.DEFAULT;
            var b = parserType.processor.apply(s).replace(".0", "").equals("1");
            return not ? !b : b;
        } catch (Throwable t) {
            menu.getLoader().getLogger().error(
                    "Failed to parse math requirement. expression: '{}' replaced expression: '{}'",
                    expression, placeholderable.replace(expression),
                    t
            );
            return false;
        }
    }

    @Override
    public List<String> getCommands() {
        return commands;
    }

    @Override
    public List<String> getDenyCommands() {
        return denyCommands;
    }

}
