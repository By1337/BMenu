package dev.by1337.bmenu.item.component.impl;

import dev.by1337.bmenu.item.component.MergeableComponent;
import dev.by1337.bmenu.text.SourcedComponentLike;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record ItemLoreComponent(List<ComponentLike> lore) implements MergeableComponent<ItemLoreComponent> {
    public static YamlCodec<ItemLoreComponent> CODEC = SourcedComponentLike.COMPONENT_LIKE_CODEC
            .listOf()
            .map(ItemLoreComponent::new, ItemLoreComponent::lore);

    public void forEachLore(Consumer<ComponentLike> consumer) {
        if (lore == null) return;
        for (int i = 0; i < lore.size(); i++) {
            consumer.accept(lore.get(i));
        }
    }

    @Override
    public ItemLoreComponent and(ItemLoreComponent t1) {
        List<ComponentLike> list = new ArrayList<>(lore);
        list.addAll(t1.lore);
        return new ItemLoreComponent(list);
    }
}
