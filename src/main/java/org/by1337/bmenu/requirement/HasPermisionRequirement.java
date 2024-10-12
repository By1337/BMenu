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
import java.util.Objects;

public class HasPermisionRequirement implements Requirement {
    private final String permission;
    private final boolean not;
    private final List<String> commands;
    private final List<String> denyCommands;

    public HasPermisionRequirement(YamlContext context, Placeholder argsReplacer) {
        permission = argsReplacer.replace(context.getAsString("permission"));
        not = context.getAsString("type").startsWith("!");
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
        return not ? !clicker.hasPermission(placeholderable.replace(permission)) : clicker.hasPermission(placeholderable.replace(permission));
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
