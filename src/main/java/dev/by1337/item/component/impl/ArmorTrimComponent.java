package dev.by1337.item.component.impl;

import dev.by1337.bmenu.util.holder.Holder;
import dev.by1337.core.ServerVersion;
import dev.by1337.yaml.KeyedYamlCodec;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.Nullable;

public record ArmorTrimComponent(Holder<ArmorTrim> armorTrim) {
    public static final @Nullable YamlCodec<ArmorTrimComponent> CODEC;

    public ArmorTrimComponent(Holder<TrimMaterial> material, Holder<TrimPattern> pattern) {
        this(new Holder<>(new ArmorTrim(material.get(), pattern.get())));
    }
    public boolean has(){
        return armorTrim.has();
    }

    static {
        if (ServerVersion.is1_19_4orNewer()) {
            CODEC = RecordYamlCodecBuilder.mapOf(
                    ArmorTrimComponent::new,
                    new KeyedYamlCodec<>(Registry.TRIM_MATERIAL, "trim_material")
                            .map(Holder::new, Holder::get)
                            .fieldOf("material",
                                    v -> new Holder<>(v.armorTrim.get().getMaterial())
                            ),
                    new KeyedYamlCodec<>(Registry.TRIM_PATTERN, "trim_pattern")
                            .map(Holder::new, Holder::get)
                            .fieldOf("pattern",
                                    v -> new Holder<>(v.armorTrim.get().getPattern())
                            )
            );
        } else {
            CODEC = null;
        }
    }
}
