package org.by1337.bmenu.yaml;

import dev.by1337.yaml.YamlMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

@Deprecated
public class RawYamlContext {
    private final YamlMap raw;

    public RawYamlContext(YamlMap raw) {
        this.raw = raw;
    }

    public boolean has(String key) {
        return raw.has(key);
    }

    @NotNull
    public YamlValue get(@NotNull String path, Object def) {
        var obj = get(path);
        return obj.getValue() == null ? YamlValue.wrap(def) : obj;
    }

    @NotNull
    public YamlValue get(@NotNull String path) {
        return YamlValue.wrap(raw.getRaw(path));
    }

    public String saveToString() {
        return raw.saveToString();
    }

    public YamlMap getRaw() {
        return raw;
    }
}
