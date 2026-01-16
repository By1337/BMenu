/*
package dev.by1337.bmenu.item.item;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.item.ItemModel;
import dev.by1337.bmenu.item.component.impl.ArmorTrimComponent;
import dev.by1337.bmenu.item.component.impl.CustomModelDataComponent;
import dev.by1337.bmenu.item.component.EnchantmentData;
import dev.by1337.bmenu.text.SourcedComponentLike;
import dev.by1337.bmenu.util.ColorHolder;
import dev.by1337.bmenu.util.DataInt;
import dev.by1337.bmenu.util.DataString;
import dev.by1337.bmenu.util.ObjectUtil;
import dev.by1337.core.ServerVersion;
import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ItemModelImpl  {
    public static final ItemModelImpl AIR = ObjectUtil.make(() -> {
        ItemModelImpl model = new ItemModelImpl();
        model.material = new DataString("AIR");
        return model;
    });
    public static final YamlCodec<ItemModelImpl> CODEC;
    private @Nullable List<SourcedComponentLike> lore;
    private @Nullable SourcedComponentLike name;
    private @NotNull DataInt amount = new DataInt("1");
    private @NotNull DataString material = new DataString("DIRT"); // todo как ItemRenderer реализуем надо будет подумать как кешировать это сразу в Material и в Skull
    private @Nullable CustomModelDataComponent customModelData;
    private @Nullable List<ItemFlag> flags;
    private @Nullable List<PotionEffect> potionEffects;
    private @Nullable ColorHolder color;
    private @Nullable List<EnchantmentData> enchantments;
    private @NotNull DataInt damage = new DataInt("0");
    private @Nullable Integer maxStackSize;
    private @Nullable ArmorTrimComponent trim;
    private boolean hideTooltip;
    private boolean unbreakable;
    private Object cache;

    public ItemModelImpl() {
    }

    public ItemModelImpl(@NotNull DataString material) {
        this.material = material;
    }

    public static ItemModel ofMaterial(String material) {
        return ItemModel.ofMaterial(material);
    }

    @Override
    public @Nullable ComponentLike name() {
        return name;
    }

    @Override
    public boolean hasLore() {
        return lore != null;
    }

    @Override
    public void forEachLore(Consumer<ComponentLike> consumer) {
        if (lore == null) return;
        for (int i = 0; i < lore.size(); i++) {
            consumer.accept(lore.get(i));
        }
    }

    @Override
    public @NotNull DataInt amount() {
        return amount;
    }

    @Override
    public @NotNull DataString material() {
        return material;
    }

    @Override
    public @Nullable CustomModelDataComponent customModelData() {
        return customModelData;
    }

    @Override
    public boolean hasItemFlags() {
        return flags != null;
    }

    @Override
    public void forEachItemFlags(Consumer<ItemFlag> consumer) {
        if (flags == null) return;
        for (int i = 0; i < flags.size(); i++) {
            consumer.accept(flags.get(i));
        }
    }

    @Override
    public boolean hasPotionEffects() {
        return potionEffects != null;
    }

    @Override
    public void forEachPotionEffects(Consumer<PotionEffect> consumer) {
        if (potionEffects == null) return;
        for (int i = 0; i < potionEffects.size(); i++) {
            consumer.accept(potionEffects.get(i));
        }
    }

    @Override
    public @Nullable ColorHolder color() {
        return color;
    }

    @Override
    public boolean hasEnchantments() {
        return enchantments != null;
    }

    @Override
    public void forEachEnchantments(Consumer<EnchantmentData> consumer) {
        if (enchantments == null) return;
        for (int i = 0; i < enchantments.size(); i++) {
            consumer.accept(enchantments.get(i));
        }
    }

    @Override
    public @NotNull DataInt damage() {
        return damage;
    }

    @Override
    public boolean hideTooltip() {
        return hideTooltip;
    }

    @Override
    public boolean unbreakable() {
        return unbreakable;
    }

    @Override
    public @Nullable Object getCache() {
        return cache;
    }

    @Override
    public void setCache(Object cache) {
        this.cache = cache;
    }

    public @Nullable Integer getMaxStackSize() {
        return maxStackSize;
    }

    public @Nullable ArmorTrimComponent getTrim() {
        return trim;
    }

    static {
        PipelineYamlCodecBuilder<ItemModelImpl> builder = PipelineYamlCodecBuilder.of(ItemModelImpl::new)
                .field(SourcedComponentLike.CODEC.listOf(), "lore", m -> m.lore, (m, v) -> m.lore = v)
                .field(SourcedComponentLike.CODEC, "name", m -> m.name, (m, v) -> m.name = v)
                .field(DataInt.CODEC, "amount", m -> m.amount, (m, v) -> m.amount = v)
                .field(MenuCodecs.MATERIAL.map(DataString::new, DataString::src), "material", m -> m.material, (m, v) -> m.material = v)
                .field(CustomModelDataComponent.CODEC, "model_data", m -> m.customModelData, (m, v) -> m.customModelData = v)
                .field(BukkitYamlCodecs.ITEM_FLAG.listOf(), "item_flags", m -> m.flags, (m, v) -> m.flags = v)
                .field(MenuCodecs.POTION_EFFECT_LIST_CODEC, "potion_effects", m -> m.potionEffects, (m, v) -> m.potionEffects = v)
                .field(ColorHolder.CODEC, "color", m -> m.color, (m, v) -> m.color = v)
                .field(MenuCodecs.ENCHANTMENT_LIST_CODEC, "enchantments", m -> m.enchantments, (m, v) -> m.enchantments = v)
                .field(DataInt.CODEC, "damage", m -> m.damage, (m, v) -> m.damage = v)
                .bool("unbreakable", m -> m.unbreakable, (m, v) -> m.unbreakable = v)
                ;
        if (ArmorTrimComponent.CODEC != null){
            builder.field(ArmorTrimComponent.CODEC, "trim", m -> m.trim, (m, v) -> m.trim = v);
        }
        if (ServerVersion.is1_20_5orNewer()){
            builder.bool("hide_tooltip", m -> m.hideTooltip, (m, v) -> m.hideTooltip = v);
            builder.integer("max_stack_size", m -> m.maxStackSize, (m, v) -> m.maxStackSize = v);
        }
        CODEC = builder.build();
    }
}
*/
