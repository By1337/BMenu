package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.util.ObjectUtil;

import java.util.Collections;
import java.util.List;

public class StringEqualsIgnoreCaseRequirement implements Requirement {
    private final String input;
    private final String output;
    private final boolean not;
    private final List<String> commands;
    private final List<String> denyCommands;

    public StringEqualsIgnoreCaseRequirement(String input, String output, boolean not, List<String> commands, List<String> denyCommands) {
        this.input = input;
        this.output = output;
        this.not = not;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public StringEqualsIgnoreCaseRequirement(YamlContext context) {
        input = context.getAsString("input");
        output = context.getAsString("output");
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
        boolean b = placeholderable.replace(input).equalsIgnoreCase(placeholderable.replace(output));
        return not ? !b : b;
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
