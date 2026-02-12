package dev.by1337.bmenu.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class RegistryLike<T extends Keyed> implements Iterable<T> {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    private final Map<NamespacedKey, T> key2value = new HashMap<>();
    private final Map<String, List<T>> path2Value = new HashMap<>();
    private final Map<T, NamespacedKey> value2Key = new IdentityHashMap<>();
    private long lastMutatedTimestamp;

    public void register(T value) {
        NamespacedKey key = value.getKey();
        if (key2value.containsKey(key)) {
            log.error("key already exists {}", key);
            return;
        }
        if (value2Key.containsKey(value)) {
            log.error("{} already registered with key {}", value, value2Key.get(value));
            return;
        }
        key2value.put(key, value);
        value2Key.put(value, key);
        path2Value.computeIfAbsent(key.getKey(), k -> new ArrayList<>()).add(value);
        lastMutatedTimestamp = System.currentTimeMillis();
    }

    public @Nullable T get(String s) {
        if (s.contains(":")) {
            var key = NamespacedKey.fromString(s);
            if (key != null)
                return key2value.get(key);
        }
        var list = path2Value.get(s);
        if (list == null || list.size() != 1) return null;
        return path2Value.get(s).get(0);
    }
    public void clear(){
        key2value.clear();
        value2Key.clear();
        path2Value.clear();
        lastMutatedTimestamp = System.currentTimeMillis();
    }
    public Stream<T> stream() {
        return key2value.values().stream();
    }
    public int size(){
        return  key2value.size();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return key2value.values().iterator();
    }

   // @Override
    public @Nullable T get(@NotNull NamespacedKey namespacedKey) {
        return key2value.get(namespacedKey);
    }

    public long lastMutatedTimestamp() {
        return lastMutatedTimestamp;
    }
}
