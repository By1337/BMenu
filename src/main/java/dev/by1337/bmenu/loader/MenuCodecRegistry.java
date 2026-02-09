package dev.by1337.bmenu.loader;

import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuCodecRegistry {

    private final Map<NamespacedKey, YamlCodec<? extends MenuConfig>> key2value = new HashMap<>();
    private final Map<String, List<YamlCodec<? extends MenuConfig>>> path2Value = new HashMap<>();


    public void register(String key, MenuSupplier s) {
        register(NamespacedKey.fromString(key), s);
    }

    public void register(NamespacedKey key, MenuSupplier s) {
        register(key, MenuConfig.CODEC.map(
                v -> {
                    v.setDefaultMenuCreator(s);
                    return v;
                },
                v -> v
        ));
    }


    public void register(String key, YamlCodec<? extends MenuConfig> value) {
        register(NamespacedKey.fromString(key), value);
    }

    public void register(NamespacedKey key, YamlCodec<? extends MenuConfig> value) {
        key2value.put(key, value);
        path2Value.computeIfAbsent(key.getKey(), k -> new ArrayList<>()).add(value);
    }

    public @Nullable YamlCodec<? extends MenuConfig> get(String s) {
        if (s.contains(":")) {
            var key = NamespacedKey.fromString(s);
            if (key != null)
                return key2value.get(key);
        }
        var list = path2Value.get(s);
        if (list == null || list.size() != 1) return null;
        return path2Value.get(s).get(0);
    }

    public int size() {
        return key2value.size();
    }

    public @Nullable YamlCodec<? extends MenuConfig> get(@NotNull NamespacedKey namespacedKey) {
        return key2value.get(namespacedKey);
    }
}
