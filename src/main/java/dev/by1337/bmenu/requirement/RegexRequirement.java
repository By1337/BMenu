package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexRequirement implements Requirement {
    private final boolean not;
    private final String regex;
    private final String input;
    private final Pattern pattern;

    public RegexRequirement(boolean not, String regex, String input) {
        this.not = not;
        this.regex = regex;
        this.input = input;
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean test(ExecuteContext ignored, PlaceholderApplier placeholders) {
        Matcher m = pattern.matcher(placeholders.setPlaceholders(input));
        return not != m.find();
    }

    @Override
    public @Nullable Requirement compile() {
        if (input.contains("{") || input.contains("%")) return null;
        return test((ExecuteContext)null, s -> s) ? TRUE : FALSE;
    }

    @Override
    public YamlValue encode() {
        return YamlValue.wrap(toString());
    }

    @Override
    public @NotNull String toString() {
        String s = regex + " " + input;
        return not ? "!regex " + s : "regex " + s;
    }
}
