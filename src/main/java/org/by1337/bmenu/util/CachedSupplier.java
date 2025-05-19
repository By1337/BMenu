package org.by1337.bmenu.util;

import java.util.Objects;
import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<T> {
    private final Supplier<T> handle;
    private T value;

    public CachedSupplier(Supplier<T> handle) {
        this.handle = handle;
    }

    public CachedSupplier(T value) {
        this.value = Objects.requireNonNull(value);
        handle = null;
    }

    @Override
    public T get() {
        if (value == null) value = handle.get();
        return value;
    }
}
