package org.by1337.bmenu.util;

import java.util.Objects;
import java.util.function.Consumer;

public class ObjectWatcher<T> {
    private T value;
    private final Consumer<T> onUpdate;

    public ObjectWatcher(T value, Consumer<T> onUpdate) {
        this.value = value;
        this.onUpdate = onUpdate;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (Objects.equals(this.value, value)) return;
        this.value = value;
        onUpdate.accept(value);
    }
}
