package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.math.MathParser;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuLoader;

public class MathRequirement implements Requirement {
    private final String expression;
    private final boolean not;

    public MathRequirement(YamlContext context) {
        expression = context.getAsString("expression");
        not = context.getAsString("type").startsWith("!");
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        var b = MathParser.mathSave("math[" + placeholderable.replace(expression) + "]").equals("1");
        return not ? !b : b;
    }
}
