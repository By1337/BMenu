package dev.by1337.bmenu.loader;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MenuCodecRegistry {
    private final Map<NamespacedKey, Registration> key2value = new HashMap<>();
    private final Map<String, List<Registration>> path2Value = new HashMap<>();

    @Deprecated
    public void register(String key, MenuSupplier s) {
        register(NamespacedKey.fromString(key), s);
    }

    @Deprecated
    public void register(NamespacedKey key, MenuSupplier s) {
        register(key, MenuConfig.createDecoderFor(s));
    }

    public void register(String key, YamlDecoder<? extends MenuConfig> value) {
        register(NamespacedKey.fromString(key), value);
    }

    public void register(NamespacedKey key, YamlDecoder<? extends MenuConfig> value) {
        register(key, new Registration(value, null));
    }

    @Deprecated
    public void register(String key, YamlCodec<? extends MenuConfig> value) {
        register(NamespacedKey.fromString(key), value);
    }

    @Deprecated
    public void register(NamespacedKey key, YamlCodec<? extends MenuConfig> value) {
        register(key, new Registration(value.asDecoder(), value));
    }

    private void register(NamespacedKey key, Registration value) {
        var previous = key2value.put(key, value);
        var list = path2Value.computeIfAbsent(key.getKey(), k -> new ArrayList<>());
        if (previous != null) list.remove(previous);
        list.add(value);
    }

    public void unregister(YamlDecoder<? extends MenuConfig> value) {
        unregisterMatching(entry -> entry.decoder == value);
    }

    @Deprecated
    public void unregister(YamlCodec<? extends MenuConfig> value) {
        unregisterMatching(entry -> entry.codec == value);
    }

    private void unregisterMatching(Predicate<Registration> predicate) {
        key2value.values().removeIf(predicate);
        path2Value.values().removeIf(list -> {
            list.removeIf(predicate);
            return list.isEmpty();
        });
    }

    public @Nullable YamlDecoder<? extends MenuConfig> getDecoder(String key) {
        var entry = getRegistration(key);
        return entry == null ? null : entry.decoder;
    }

    public @Nullable YamlDecoder<? extends MenuConfig> getDecoder(@NotNull NamespacedKey key) {
        var entry = getRegistration(key);
        return entry == null ? null : entry.decoder;
    }

    @Deprecated
    public @Nullable YamlCodec<? extends MenuConfig> get(String key) {
        var entry = getRegistration(key);
        return entry == null ? null : entry.codec();
    }

    @Deprecated
    public @Nullable YamlCodec<? extends MenuConfig> get(@NotNull NamespacedKey key) {
        var entry = getRegistration(key);
        return entry == null ? null : entry.codec();
    }

    @Nullable
    Registration getRegistration(String key) {
        if (key.contains(":")) {
            var namespacedKey = NamespacedKey.fromString(key);
            if (namespacedKey != null) return getRegistration(namespacedKey);
        }
        var list = path2Value.get(key);
        return list == null || list.size() != 1 ? null : list.get(0);
    }

    @Nullable
    Registration getRegistration(NamespacedKey key) {
        return key2value.get(key);
    }

    public int size() {
        return key2value.size();
    }

    static final class Registration {
        final YamlDecoder<? extends MenuConfig> decoder;
        private YamlCodec<? extends MenuConfig> codec;

        Registration(YamlDecoder<? extends MenuConfig> decoder, YamlCodec<? extends MenuConfig> codec) {
            this.decoder = decoder;
            this.codec = codec;
        }

        YamlCodec<? extends MenuConfig> codec() {
            if (codec == null) codec = YamlCodec.of(decoder);
            return codec;
        }
    }
}
