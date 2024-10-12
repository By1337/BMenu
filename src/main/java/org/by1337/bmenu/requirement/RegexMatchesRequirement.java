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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatchesRequirement implements Requirement {
    private final String input;
    private final String regex;
    private final boolean not;
    private final Pattern pattern;
    private final List<String> commands;
    private final List<String> denyCommands;

    public RegexMatchesRequirement(YamlContext context, Placeholder argsReplacer) {
        input = argsReplacer.replace(context.getAsString("input"));
        regex = argsReplacer.replace(context.getAsString("regex"));
        not = context.getAsString("type").startsWith("!");
        pattern = Pattern.compile(regex);
        commands = ObjectUtil.mapIfNotNullOrDefault(context.get("commands").getValue(),
                value -> ((List<?>) value).stream()
                        .map(v -> argsReplacer.replace(new YamlValue(v).getAsString())).toList(),
                Collections.emptyList()
        );
        denyCommands = ObjectUtil.mapIfNotNullOrDefault(context.get("deny_commands").getValue(),
                value -> ((List<?>) value).stream()
                        .map(v -> argsReplacer.replace(new YamlValue(v).getAsString())).toList(),
                Collections.emptyList()
        );
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        Matcher m = pattern.matcher(placeholderable.replace(input));
        return not ? !m.find() : m.find();
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
