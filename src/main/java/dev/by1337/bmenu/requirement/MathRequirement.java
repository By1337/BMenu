package dev.by1337.bmenu.requirement;

import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.StringUtil;
import dev.by1337.bmenu.util.math.FastExpressionParser;

public class MathRequirement implements Requirement {
    private final String expression;
    private final boolean not;
    private final Commands commands;
    private final Commands denyCommands;

    public MathRequirement(String expression, Commands commands, Commands denyCommands) {
        this.expression = expression;
        this.commands = commands;
        not = false;
        this.denyCommands = denyCommands;
    }

    public MathRequirement(YamlMap context) {
        expression = context.get("expression").decode(YamlCodec.STRING).getOrThrow();
        not = context.get("type").decode(YamlCodec.STRING).getOrThrow().startsWith("!");
        commands = context.get("commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
        denyCommands = context.get("deny_commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
    }

    @Override
    public boolean test(Menu menu, PlaceholderApplier placeholder, Player clicker) {
        String s = placeholder.setPlaceholders(expression);
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
    public Commands getCommands() {
        return commands;
    }

    @Override
    public Commands getDenyCommands() {
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
