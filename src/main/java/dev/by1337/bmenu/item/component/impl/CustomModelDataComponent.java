package dev.by1337.bmenu.item.component.impl;

import dev.by1337.bmenu.util.ColorHolder;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Color;

import java.util.List;

public record CustomModelDataComponent(List<Float> floats, List<Boolean> flags, List<String> strings,
                                       List<Color> colors) {
    public static YamlCodec<CustomModelDataComponent> CODEC = RecordYamlCodecBuilder.mapOf(
            CustomModelDataComponent::new,
            YamlCodec.FLOAT.listOf().fieldOf("floats", CustomModelDataComponent::floats, List.of()),
            YamlCodec.BOOL.listOf().fieldOf("flags", CustomModelDataComponent::flags, List.of()),
            YamlCodec.STRING.listOf().fieldOf("strings", CustomModelDataComponent::strings, List.of()),
            ColorHolder.CODEC.map(ColorHolder::toBukkit, ColorHolder::fromBukkit).listOf()
                    .fieldOf("colors",CustomModelDataComponent::colors, List.of())
    ).whenPrimitive(YamlCodec.INT.map(
            //Deprecated 1.21.5
            i -> new CustomModelDataComponent(List.of(i.floatValue()), List.of(), List.of(), List.of()),
            c -> c.floats.get(0).intValue()
    ));
}
