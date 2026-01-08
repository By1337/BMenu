package dev.by1337.bmenu.util;

public class Holder<T> {
    private final T item;

    public Holder(T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }

    public boolean has(){
        return item != null;
    }
}
