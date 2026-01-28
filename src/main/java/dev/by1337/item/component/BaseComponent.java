package dev.by1337.item.component;

import dev.by1337.yaml.codec.YamlCodec;

public record BaseComponent<T>(int id, String name, YamlCodec<T> codec) {
}
