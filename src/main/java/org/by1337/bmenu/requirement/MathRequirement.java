package org.by1337.bmenu.requirement;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.util.ObjectUtil;
import org.by1337.bmenu.util.StringUtil;
import org.by1337.bmenu.util.math.FastExpressionParser;

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

    public MathRequirement(YamlMap context) {
        expression = context.get("expression").decode(YamlCodec.STRING).getOrThrow();
        not = context.get("type").decode(YamlCodec.STRING).getOrThrow().startsWith("!");
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
        String s = placeholderable.replace(expression);
        try {
            var b = FastExpressionParser.parse(s) == 1D;
            return not != b;
        } catch (FastExpressionParser.MathFormatException e) {
            menu.getLoader().getLogger().error(
                    "Failed to parse math requirement. expression: '{}' replaced expression: '{}'\n{}",
                    expression, s,
                    e.getMessage()
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

    @Override
    public boolean state() {
        try {
            var b = FastExpressionParser.parse(expression) == 1D;
            return not != b;
        } catch (FastExpressionParser.MathFormatException e) {
            e.printStackTrace();//todo
            return false;
        }
    }

    @Override
    public boolean compilable() {
        return StringUtil.hasNoPlaceholders(expression);
    }

}
