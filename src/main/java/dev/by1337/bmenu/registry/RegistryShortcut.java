package dev.by1337.bmenu.registry;

import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

public abstract class RegistryShortcut<T> {
    private final Map<NamespacedKey, T> key2value = new HashMap<>();
    private final Map<String, T> string2value = new HashMap<>();

    protected abstract T find(String key);

    protected abstract T find(NamespacedKey key);

    public T get(String s) {
        return string2value.computeIfAbsent(s, this::find);
    }

    public T get(NamespacedKey s) {
        return key2value.computeIfAbsent(s, this::find);
    }
    public void clear(){
        key2value.clear();
        string2value.clear();
    }
}
