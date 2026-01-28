package dev.by1337.item.component.impl;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public record PotionContentsComponent(
        List<PotionEffect> contents) implements MergeableComponent<PotionContentsComponent> {

    public static final YamlCodec<PotionContentsComponent> CODEC =
            MenuCodecs.POTION_EFFECT_LIST_CODEC.map(
                    PotionContentsComponent::new,
                    PotionContentsComponent::contents
            );

    @Override
    public PotionContentsComponent and(PotionContentsComponent t1) {
        List<PotionEffect> list = new ArrayList<>(contents);
        list.addAll(t1.contents);
        return new PotionContentsComponent(list);
    }
}
