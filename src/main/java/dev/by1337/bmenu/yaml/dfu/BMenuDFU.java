package dev.by1337.bmenu.yaml.dfu;

import dev.by1337.yaml.YamlValue;

import java.util.Map;
import java.util.function.Function;

public class BMenuDFU {
    public static final Function<YamlValue, YamlValue> COMMANDS_KEY_RENAMER = new KeyRenamer(Map.of(
            "commands", "do",
            "deny_commands", "else"
    ));
}
