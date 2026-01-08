package dev.by1337.bmenu.util;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectUtil {
    @Contract(value = "_, _, !null -> !null")
    @Nullable
    public static <T, E> E mapIfNotNullOrDefault(@Nullable T t, Function<? super @NotNull T, E> mapper, @Nullable E def) {
        return t != null ? mapper.apply(t) : def;
    }

    @Contract(value = "null, _ -> null")
    public static <T> T mapIfNotNull(@Nullable T t, Function<? super T, ? extends T> mapper) {
        return t != null ? mapper.apply(t) : null;
    }

    public static <T> void applyIfNotNull(@Nullable T t, Consumer<@NotNull T> consumer) {
        if (t != null) {
            consumer.accept(t);
        }
    }

    @Nullable
    @Contract(value = "!null, _ -> !null")
    public static <T> T requireNonNullElseGet(@Nullable T val, Supplier<@Nullable T> supplier) {
        return val != null ? val : supplier.get();
    }

    public static <T> T make(Supplier<T> maker) {
        return maker.get();
    }
}
