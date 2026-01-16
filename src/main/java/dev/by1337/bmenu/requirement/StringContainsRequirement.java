package dev.by1337.bmenu.requirement;

import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.ObjectUtil;
import dev.by1337.bmenu.util.StringUtil;

import java.util.Collections;
import java.util.List;

public class StringContainsRequirement implements Requirement {
    private final String input;
    private final String output;
    private final boolean not;
    private final Commands commands;
    private final Commands denyCommands;


    public StringContainsRequirement(String input, String output, boolean not, Commands commands, Commands denyCommands) {
        this.input = input;
        this.output = output;
        this.not = not;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public StringContainsRequirement(YamlMap context) {
        input = context.get("input").decode(YamlCodec.STRING).getOrThrow();
        output = context.get("output").decode(YamlCodec.STRING).getOrThrow();
        not = context.get("type").decode(YamlCodec.STRING).getOrThrow().startsWith("!");
        commands = context.get("commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
        denyCommands = context.get("deny_commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
    }


    @Override
    public boolean test(Menu menu, PlaceholderApplier placeholder, Player clicker) {
        boolean b = placeholder.setPlaceholders(input).contains(placeholder.setPlaceholders(output));
        return not != b;
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
        return not != input.contains(output);
    }

    @Override
    public boolean compilable() {
        return StringUtil.hasNoPlaceholders(input) && StringUtil.hasNoPlaceholders(output);
    }
}
