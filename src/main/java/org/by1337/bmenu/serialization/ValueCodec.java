package org.by1337.bmenu.serialization;

import blib.com.mojang.datafixers.util.Pair;
import blib.com.mojang.serialization.Codec;
import blib.com.mojang.serialization.DataResult;
import blib.com.mojang.serialization.DynamicOps;
import blib.com.mojang.serialization.codecs.PrimitiveCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ValueCodec<T, V> {
    private final Codec<V> valueCodec;
    private final Function<T, V> getter;
    private final BiConsumer<T, V> setter;
    private final @Nullable V def;
    private final String field;

    public ValueCodec(Codec<V> valueCodec, Function<T, V> getter, BiConsumer<T, V> setter, String field, @Nullable V def) {
        this.valueCodec = valueCodec;
        this.getter = getter;
        this.setter = setter;
        this.def = def;
        this.field = field;
    }

    public <R> DataResult<R> decode(T source, DynamicOps<R> ops, R t) {
        DataResult<Pair<V, R>> data = valueCodec.decode(ops, t);
        if (data.isSuccess()) {
            setter.accept(source, data.getOrThrow().getFirst());
        } else if (def != null) {
            setter.accept(source, def);
        }
        return data.map(Pair::getSecond).mapError(s -> "failed to read param '" + field + "' cause: '" + s + "'");
    }


    public <R> DataResult<R> encode(T source, DynamicOps<R> ops, R t) {
        V val = getter.apply(source);
        if (val != null) {
            return valueCodec.encode(val, ops, t);
        } else if (def != null) {
            return valueCodec.encode(def, ops, t);
        }
        return DataResult.success(t);
    }

    public Codec<V> valueCodec() {
        return valueCodec;
    }

    public Function<T, V> getter() {
        return getter;
    }

    public BiConsumer<T, V> setter() {
        return setter;
    }

    @Nullable
    public V def() {
        return def;
    }

    public String field() {
        return field;
    }
}
