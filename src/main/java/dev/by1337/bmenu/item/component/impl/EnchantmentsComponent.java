package dev.by1337.bmenu.item.component.impl;

import dev.by1337.bmenu.item.component.MergeableComponent;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.codec.InlineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record EnchantmentsComponent(List<Entry> enchantments) implements MergeableComponent<EnchantmentsComponent> {
    public static final YamlCodec<EnchantmentsComponent> CODEC =
            YamlCodec.mapOf(BukkitCodecs.enchantment(), YamlCodec.INT).map(
                            map -> map.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList(),
                            list -> list.stream().collect(Collectors.toMap(
                                    Entry::enchantment,
                                    Entry::lvl
                            )))
                    .whenPrimitive(InlineYamlCodecBuilder.inline(
                            ";",
                            "<enchantment>;<lvl>",
                            Entry::new,
                            BukkitCodecs.enchantment().withGetter(Entry::enchantment),
                            YamlCodec.INT.withGetter(Entry::lvl)
                    ).listOf())
                    .map(EnchantmentsComponent::new, EnchantmentsComponent::enchantments);

    @Override
    public EnchantmentsComponent and(EnchantmentsComponent t1) {
        List<Entry> enchantments = new ArrayList<>(this.enchantments);
        enchantments.addAll(t1.enchantments);
        return new EnchantmentsComponent(enchantments);
    }

    public record Entry(Enchantment enchantment, int lvl) {
        public Entry {
            Objects.requireNonNull(enchantment, "enchantment");
        }
    }
}
