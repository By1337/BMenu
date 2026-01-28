package dev.by1337.bmenu.util.function;

import java.util.function.BooleanSupplier;

public class LazyBool implements BooleanSupplier {
    private final BooleanSupplier supplier;
    private boolean has;
    private boolean value;

    public LazyBool(BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    public boolean get() {
        if (has) return value;
        has = true;
        return value = supplier.getAsBoolean();
    }

    public boolean getAsBoolean() {
        return get();
    }

}
