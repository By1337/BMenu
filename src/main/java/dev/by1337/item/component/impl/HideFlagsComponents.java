package dev.by1337.item.component.impl;

import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.inventory.ItemFlag;

import java.util.HashSet;
import java.util.Set;

public record HideFlagsComponents(Set<ItemFlag> flags) implements MergeableComponent<HideFlagsComponents> {
    public static final YamlCodec<HideFlagsComponents> CODEC = BukkitCodecs.item_flag()
            .listOf().asSet()
            .map(HideFlagsComponents::new, HideFlagsComponents::flags);

    @Override
    public HideFlagsComponents and(HideFlagsComponents t1) {
        var set = new HashSet<>(flags);
        set.addAll(t1.flags);
        return new HideFlagsComponents(set);
    }
}
