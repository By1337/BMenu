package dev.by1337.bmenu.yaml.codec;

import dev.by1337.core.util.misc.Pair;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CodecSelector<T> implements YamlCodec<T> {
    private final Function<T, YamlValue> encoder;
    private final List<Pair<YamlTester, YamlCodec<? extends T>>> codecs = new ArrayList<>();

    public CodecSelector(Function<T, YamlValue> encoder) {
        this.encoder = encoder;
    }

    @Override
    public DataResult<T> decode(YamlValue yaml) {
        for (Pair<YamlTester, YamlCodec<? extends T>> codec : codecs) {
            if (codec.getLeft().test(yaml)) {
                return codec.getRight().decode(yaml).mapValue(v -> v);
            }
        }
        return DataResult.error("has no encoder for " + yaml.getRaw());
    }

    @Override
    public YamlValue encode(T t) {
        return encoder.apply(t);
    }

    @Override
    public @NotNull SchemaType schema() {
        return SchemaTypes.ANY;
    }

    public CodecSelector<T> add(YamlTester tester, YamlCodec<? extends T> codec) {
        codecs.add(Pair.of(tester, codec));
        return this;
    }
    public CodecSelector<T> add(YamlTester tester, Function<YamlValue, DataResult<? extends T>> decoder) {
        codecs.add(Pair.of(tester, new YamlCodec<T>() {
            @Override
            public DataResult<T> decode(YamlValue yamlValue) {
                return decoder.apply(yamlValue).mapValue(v -> v);
            }

            @Override
            public YamlValue encode(T t) {
                throw new UnsupportedOperationException("decode only");
            }

            @Override
            public @NotNull SchemaType schema() {
                return SchemaTypes.ANY;
            }
        }));
        return this;
    }
}
