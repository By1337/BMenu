package org.by1337.bmenu.yaml;

import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class RawYamlContext {
    private Map<String, Object> raw;

    public RawYamlContext(Map<String, Object> raw) {
        this.raw = raw;
    }
    public boolean has(String key) {
        return raw.containsKey(key);
    }

    @NotNull
    public YamlValue get(@NotNull String path, Object def) {
        var obj = get(path);
        return obj.getValue() == null ? new YamlValue(def) : obj;
    }

    @NotNull
    public YamlValue get(@NotNull String path) {
        Objects.requireNonNull(path, "path is null!");
        if (path.isBlank()) throw new IllegalArgumentException("path is empty!");
        String[] path0 = path.split("\\.");

        Object last = null;
        for (String s : path0) {
            if (last == null) {
                Object o = raw.get(s);
                if (o == null) new YamlValue(null);
                last = o;
            } else if (last instanceof Map<?, ?> sub) {
                Object o = sub.get(s);
                if (o == null) return new YamlValue(null);
                last = o;
            } else {
                throw new ClassCastException(last.getClass().getName() + " to Map<String, Object>");
            }
        }
        return new YamlValue(last);
    }

    public String saveToString() {
        YamlConfiguration root = new YamlConfiguration();
        YamlContext.convertMapsToSections(raw, root);
        return root.saveToString();
    }

    public Map<String, Object> getRaw() {
        return raw;
    }
}
