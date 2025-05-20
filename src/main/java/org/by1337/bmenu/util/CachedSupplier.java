package org.by1337.bmenu.util;

import java.util.Objects;
import java.util.function.Function;

public class CachedSupplier<T, R> implements Function<T, R> {
    private final Function<T, R> handle;
    private R value;

    public CachedSupplier(Function<T, R> handle) {
        this.handle = handle;
    }

    public CachedSupplier(R value) {
        this.value = Objects.requireNonNull(value);
        handle = null;
    }

    @Override
    public R apply(T t) {
        if (value == null) value = handle.apply(t);
        return value;
    }

    public void invalidateCash(){
        if (handle == null) return;
        value = null;
    }
}
