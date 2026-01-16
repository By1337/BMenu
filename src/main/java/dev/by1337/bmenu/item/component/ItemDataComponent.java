package dev.by1337.bmenu.item.component;

import dev.by1337.yaml.codec.YamlCodec;

public record ItemDataComponent<T>(int id, String name, YamlCodec<T> codec) {
}
