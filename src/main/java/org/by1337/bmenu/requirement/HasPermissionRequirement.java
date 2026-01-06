package org.by1337.bmenu.requirement;

import dev.by1337.plc.Placeholderable;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.by1337.bmenu.command.Commands;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.util.ObjectUtil;

import java.util.Collections;
import java.util.List;

public class HasPermissionRequirement implements Requirement {

    private final String permission;
    private final boolean not;
    private final Commands commands;
    private final Commands denyCommands;

    public HasPermissionRequirement(String permission, boolean not, Commands commands, Commands denyCommands) {
        this.permission = permission;
        this.not = not;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public HasPermissionRequirement(YamlMap context) {
        permission = context.get("permission").decode(YamlCodec.STRING).getOrThrow();
        not = context.get("type").decode(YamlCodec.STRING).getOrThrow().startsWith("!");
        commands = context.get("commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
        denyCommands = context.get("deny_commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        return not ? !clicker.hasPermission(placeholderable.replace(permission)) : clicker.hasPermission(placeholderable.replace(permission));
    }

    @Override
    public Commands getCommands() {
        return commands;
    }

    @Override
    public Commands getDenyCommands() {
        return denyCommands;
    }
}
