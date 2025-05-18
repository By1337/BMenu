package org.by1337.bmenu.yaml;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CashedYamlMap {
    private final YamlMap source;
    private final Map<String, Object> cashed = new HashMap<>();

    public CashedYamlMap(YamlMap source) {
        this.source = source;
    }

    @Contract("_, !null, _ -> !null")
    @SuppressWarnings("unchecked")
    public @Nullable <T> T get(@NotNull String path, @Nullable T def, YamlCodec<T> codec) {
        var v = cashed.get(path);
        if (v != null) {
            return (T) v;
        } else {
            var raw = source.get(path);
            if (raw.isNull()) {
                cashed.put(path, def);
                return def;
            }
            var decoded = codec.decode(raw);
            cashed.put(path, decoded);
            return decoded;
        }
    }
}
