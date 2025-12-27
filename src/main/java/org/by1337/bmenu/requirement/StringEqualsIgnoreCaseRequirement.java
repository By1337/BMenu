package org.by1337.bmenu.requirement;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.util.ObjectUtil;
import org.by1337.bmenu.util.StringUtil;

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

    public StringEqualsIgnoreCaseRequirement(YamlMap context) {
        input = context.get("input").decode(YamlCodec.STRING).getOrThrow();
        output = context.get("output").decode(YamlCodec.STRING).getOrThrow();
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
        boolean b = placeholderable.replace(input).equalsIgnoreCase(placeholderable.replace(output));
        return not != b;
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
        return not != input.equalsIgnoreCase(output);
    }

    @Override
    public boolean compilable() {
        return StringUtil.hasNoPlaceholders(input) && StringUtil.hasNoPlaceholders(output);
    }
}
