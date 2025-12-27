package org.by1337.bmenu.yaml;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CashedYamlMap {
    private final YamlMap source;
    private final Map<String, DataResult<?>> cashed = new HashMap<>();

    public CashedYamlMap(YamlMap source) {
        this.source = source;
    }

    @SuppressWarnings("unchecked")
    public <T> DataResult<@Nullable T> get(@NotNull String path, @Nullable T def, YamlCodec<T> codec) {
        var v = cashed.get(path);
        if (v != null) {
            return (DataResult<T>) v;
        } else {
            var raw = source.get(path);
            if (raw.isNull()) {
                DataResult<T> res = DataResult.success(def);
                cashed.put(path, res);
                return res;
            }
            DataResult<T> decoded = codec.decode(raw);
            cashed.put(path, decoded);
            return decoded;
        }
    }
}
