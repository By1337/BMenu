package dev.by1337.bmenu.yaml.codec;

import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.jetbrains.annotations.NotNull;

public class CodecHolder<T> implements YamlCodec<T> {
    private final YamlCodec<T> codec;

    public CodecHolder(YamlCodec<T> codec) {
        this.codec = codec;
    }

    @Override
    public DataResult<T> decode(YamlValue yamlValue) {
        return codec.decode(yamlValue);
    }

    @Override
    public YamlValue encode(T t) {
        return codec.encode(t);
    }

    @Override
    public @NotNull SchemaType schema() {
        return codec.schema();
    }
}
