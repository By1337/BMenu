package org.by1337.bmenu.util;

import java.util.function.Supplier;

public class LazyLoad<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T value;

    public LazyLoad(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        return this.value == null ? (this.value = this.supplier.get()) : this.value;
    }
}
