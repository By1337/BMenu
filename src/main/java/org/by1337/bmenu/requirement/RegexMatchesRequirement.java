package org.by1337.bmenu.requirement;

import dev.by1337.plc.Placeholderable;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.by1337.bmenu.command.Commands;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.util.ObjectUtil;
import org.by1337.bmenu.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatchesRequirement implements Requirement {
    private final String input;
    private final String regex;
    private final boolean not;
    private final Pattern pattern;
    private final Commands commands;
    private final Commands denyCommands;

    public RegexMatchesRequirement(YamlMap context) {
        input = context.get("input").decode(YamlCodec.STRING).getOrThrow();
        regex = context.get("regex").decode(YamlCodec.STRING).getOrThrow();
        not = context.get("type").decode(YamlCodec.STRING).getOrThrow().startsWith("!");
        pattern = Pattern.compile(regex);
        commands = context.get("commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
        denyCommands = context.get("deny_commands").decode(Commands.CODEC, Commands.EMPTY).getOrThrow();
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        Matcher m = pattern.matcher(placeholderable.replace(input));
        return not != m.find();
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
        Matcher m = pattern.matcher(input);
        return not != m.find();
    }

    @Override
    public boolean compilable() {
        return StringUtil.hasNoPlaceholders(input);
    }
}
