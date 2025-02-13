package org.by1337.bmenu.util;

import com.google.common.base.Supplier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ObjectUtil {
    public static <T, E> E mapIfNotNullOrDefault(@Nullable T t, Function<? super T, E> mapper, E def) {
        return t != null ? mapper.apply(t) : def;
    }

    public static <T> T mapIfNotNull(@Nullable T t, Function<? super T, ? extends T> mapper) {
        return t != null ? mapper.apply(t) : null;
    }

    @Nullable
    public static <T> T requireNonNullElseGet(@Nullable T val, Supplier<@Nullable T> supplier){
        return val != null ? val : supplier.get();
    }
}
