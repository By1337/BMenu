package org.by1337.bmenu.serialization;

import blib.com.mojang.serialization.Codec;
import blib.com.mojang.serialization.DataResult;
import blib.com.mojang.serialization.DynamicOps;
import blib.com.mojang.serialization.MapLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ValueCodecSet<T> {
    private final List<ValueCodec<T, ?>> codecs = new ArrayList<>();

    public void add(ValueCodec<T, ?> codec) {
        codecs.add(codec);
    }

    public <V> void add(Codec<V> valueCodec, Function<T, V> getter, BiConsumer<T, V> setter, String field) {
        add(valueCodec, getter, setter, field, null);
    }

    public <V> void add(Codec<V> valueCodec, Function<T, V> getter, BiConsumer<T, V> setter, String field, @Nullable V def) {
        codecs.add(new ValueCodec<>(valueCodec, getter, setter, field, def));
    }

    public <R> DataResult<T> decode(T source, DynamicOps<R> ops, R t) {
        DataResult<MapLike<R>> rawMap = ops.getMap(t);
        if (rawMap.isError()) return DataResult.error(rawMap.error().get().messageSupplier(), source);
        MapLike<R> map = ops.getMap(t).getOrThrow();
        int errors = 0;
        StringBuilder sb = new StringBuilder();
        for (ValueCodec<T, ?> codec : codecs) {
            R r = map.get(codec.field());
            if (r != null) {
                DataResult<R> res = codec.decode(source, ops, r);
                if (res.isError()) {
                    sb.append(++errors).append(". ").append(res.error().get().message()).append("\n");
                }
            }
        }
        if (sb.isEmpty()) return DataResult.success(source);
        sb.setLength(sb.length() -1);
        return DataResult.error(sb::toString, source);
    }

    public <R> DataResult<R> encode(T source, DynamicOps<R> ops, R t) {
        Map<R, R> map = new HashMap<>();

        for (ValueCodec<T, ?> codec : codecs) {
            DataResult<R> result = codec.encode(source, ops, t);
            if (result.isSuccess()) {
                map.put(ops.createString(codec.field()), result.getOrThrow());
            } else {
                return result;
            }
        }
        R res = ops.createMap(map);
        return DataResult.success(res);
    }
}
