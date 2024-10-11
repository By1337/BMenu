package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.math.MathParser;
import org.by1337.bmenu.Menu;

public class MathRequirement implements Requirement {
    private final String expression;
    private final boolean not;

    public MathRequirement(YamlContext context, Placeholder argsReplacer) {
        expression = argsReplacer.replace(context.getAsString("expression"));
        not = context.getAsString("type").startsWith("!");
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        try {
            var b = MathParser.math("math[" + placeholderable.replace(expression) + "]").equals("1");
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
}
