package org.by1337.bmenu.item;

import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.item.component.EnchantmentData;
import org.by1337.bmenu.text.SourcedComponentLike;
import org.by1337.bmenu.util.DataInt;
import org.by1337.bmenu.util.DataString;
import org.by1337.bmenu.util.ObjectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ItemModel {
    public static final ItemModel AIR = ObjectUtil.make(() -> {
        ItemModel model = new ItemModel();
        model.material = new DataString("AIR");
        return model;
    });
    public static final YamlCodec<ItemModel> CODEC;
    private @Nullable List<SourcedComponentLike> lore;
    private @Nullable SourcedComponentLike name;
    private @NotNull DataInt amount = new DataInt("1");
    private @NotNull DataString material = new DataString("DIRT"); // todo как ItemRenderer реализуем надо будет подумать как кешировать это сразу в Material и в Skull
    private @Nullable CustomModelDataComponent customModelData;
    private @Nullable Set<ItemFlag> flags;
    private @Nullable List<PotionEffect> potionEffects;
    private @Nullable Color color;
    private @Nullable List<EnchantmentData> enchantments;
    private @NotNull DataInt damage = new DataInt("0");
    private boolean hideTooltip;
    private boolean unbreakable;

    public static ItemModel ofMaterial(String material) {
        if (material.equalsIgnoreCase("air")) return AIR;
        ItemModel model = new ItemModel();
        model.material = new DataString(material);
        return model;
    }

    public record CustomModelDataComponent(List<Float> floats, List<Boolean> flags, List<String> strings,
                                           List<Integer> colors) {
        public static YamlCodec<CustomModelDataComponent> CODEC = RecordYamlCodecBuilder.mapOf(
                CustomModelDataComponent::new,
                YamlCodec.FLOAT.listOf().fieldOf("floats", CustomModelDataComponent::floats),
                YamlCodec.BOOL.listOf().fieldOf("flags", CustomModelDataComponent::flags),
                YamlCodec.STRING.listOf().fieldOf("strings", CustomModelDataComponent::strings),
                BukkitCodecs.color().map(Color::asRGB, Color::fromRGB).listOf().fieldOf("colors", CustomModelDataComponent::colors)
        ).whenPrimitive(YamlCodec.INT.map(
                //Deprecated 1.21.5
                i -> new CustomModelDataComponent(List.of(i.floatValue()), List.of(), List.of(), List.of()),
                c -> c.floats.get(0).intValue()
        ));
    }

    static {
        CODEC = PipelineYamlCodecBuilder.of(ItemModel::new)
                .field(SourcedComponentLike.CODEC.listOf(), "lore", m -> m.lore, (m, v) -> m.lore = v)
                .field(SourcedComponentLike.CODEC, "name", m -> m.name, (m, v) -> m.name = v)
                .field(DataInt.CODEC, "amount", m -> m.amount, (m, v) -> m.amount = v)
                .field(MenuCodecs.MATERIAL.map(DataString::new, DataString::src), "material", m -> m.material, (m, v) -> m.material = v)
                .field(CustomModelDataComponent.CODEC, "model_data", m -> m.customModelData, (m, v) -> m.customModelData = v)
                .field(BukkitYamlCodecs.ITEM_FLAG.listOf().asSet(), "item_flags", m -> m.flags, (m, v) -> m.flags = v)
                .field(MenuCodecs.MODERN_POTION_EFFECT_YAML_CODEC, "potion_effects", m -> m.potionEffects, (m, v) -> m.potionEffects = v)
                .field(BukkitCodecs.color(), "color", m -> m.color, (m, v) -> m.color = v)
                .field(MenuCodecs.MODERN_ENCHANTMENT_YAML_CODEC, "enchantments", m -> m.enchantments, (m, v) -> m.enchantments = v)
                .field(DataInt.CODEC, "damage", m -> m.damage, (m, v) -> m.damage = v)
                .bool("hide_tooltip", m -> m.hideTooltip, (m, v) -> m.hideTooltip = v)
                .bool("unbreakable", m -> m.unbreakable, (m, v) -> m.unbreakable = v)
                .build()
        ;
    }

    public @Nullable List<SourcedComponentLike> lore() {
        return lore;
    }

    public @Nullable SourcedComponentLike name() {
        return name;
    }

    public @NotNull DataInt amount() {
        return amount;
    }

    public @NotNull DataString material() {
        return material;
    }

    public @Nullable CustomModelDataComponent customModelData() {
        return customModelData;
    }

    public @Nullable Set<ItemFlag> flags() {
        return flags;
    }

    public @Nullable List<PotionEffect> potionEffects() {
        return potionEffects;
    }

    public @Nullable Color color() {
        return color;
    }

    public @Nullable List<EnchantmentData> enchantments() {
        return enchantments;
    }

    public @NotNull DataInt damage() {
        return damage;
    }

    public boolean hideTooltip() {
        return hideTooltip;
    }

    public boolean unbreakable() {
        return unbreakable;
    }
}
