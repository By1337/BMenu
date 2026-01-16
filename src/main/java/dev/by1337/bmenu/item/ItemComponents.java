package dev.by1337.bmenu.item;

import dev.by1337.bmenu.item.component.ItemDataComponent;
import dev.by1337.bmenu.item.component.ItemDataComponents;
import dev.by1337.bmenu.item.component.MergeableComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ItemComponents {
    private static final int INIT_SIZE = ItemDataComponents.count();
    private final Object[] components;

    public ItemComponents() {
        components = new Object[INIT_SIZE];
    }

    private ItemComponents(Object[] components) {
        this.components = components;
    }

    public boolean getBool(ItemDataComponent<Boolean> type) {
        return get(type, false);
    }

    @Nullable
    public <T> T get(ItemDataComponent<T> type) {
        if (type == null) return null;
        return (T) components[type.id()];
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(ItemDataComponent<T> type, T def) {
        if (type == null) return def;
        var o = components[type.id()];
        return o == null ? def : (T) o;
    }

    public <T> void set(ItemDataComponent<T> type, T value) {
        if (type == null) return;
        components[type.id()] = value;
    }

    public ItemComponents copy() {
        return new ItemComponents(Arrays.copyOf(components, INIT_SIZE));
    }

    public ItemComponents merge(ItemComponents other) {
        Object[] result = Arrays.copyOf(components, INIT_SIZE);
        for (int i = 0; i < other.components.length; i++) {
            Object o = other.components[i];
            if (o != null) {
                Object current = components[i];
                if (current == null) {
                    result[i] = o;
                } else if (o instanceof MergeableComponent<?>) {
                    result[i] = merge((MergeableComponent) current, (MergeableComponent) o);
                }
            }
        }
        return new ItemComponents(result);
    }

    private <T extends MergeableComponent<T>> T merge(T t, T t2) {
        return t.merge(t2);
    }
}
