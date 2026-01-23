package dev.by1337.bmenu.item.component;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.item.ItemComponents;
import dev.by1337.bmenu.item.component.impl.*;
import dev.by1337.bmenu.text.SourcedComponentLike;
import dev.by1337.bmenu.util.ColorHolder;
import dev.by1337.bmenu.util.DataInt;
import dev.by1337.bmenu.util.DataString;
import dev.by1337.core.ServerVersion;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.ComponentLike;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemDataComponents {
    private static final List<ItemDataComponent<?>> COMPONENTS = new ArrayList<>();

    public static final ItemDataComponent<ItemLoreComponent> LORE = register("lore", ItemLoreComponent.CODEC);
    public static final ItemDataComponent<ComponentLike> NAME = register("name", SourcedComponentLike.COMPONENT_LIKE_CODEC);
    public static final ItemDataComponent<DataInt> AMOUNT = register("amount", DataInt.CODEC);
    public static final ItemDataComponent<DataInt> DAMAGE = register("damage", DataInt.CODEC);
    public static final ItemDataComponent<DataString> MATERIAL = register("material", MenuCodecs.MATERIAL.map(DataString::new, DataString::src));
    public static final ItemDataComponent<CustomModelDataComponent> MODEL_DATA = register("model_data", CustomModelDataComponent.CODEC);
    public static final ItemDataComponent<PotionContentsComponent> POTION_CONTENTS = register("potion_contents", PotionContentsComponent.CODEC);
    public static final ItemDataComponent<ColorHolder> COLOR = register("color", ColorHolder.CODEC);
    public static final ItemDataComponent<EnchantmentsComponent> ENCHANTMENTS = register("enchantments", EnchantmentsComponent.CODEC);
    public static final ItemDataComponent<Boolean> UNBREAKABLE = register("unbreakable", YamlCodec.BOOL);
    public static final ItemDataComponent<ContainerComponent> CONTAINER = register("container", ContainerComponent.CODEC);
    public static final ItemDataComponent<HideFlagsComponents> HIDE_FLAGS = register("item_flags", HideFlagsComponents.CODEC);
    //1.19.4+
    @Nullable
    public static final ItemDataComponent<ArmorTrimComponent> TRIM = register("trim", ArmorTrimComponent.CODEC, ArmorTrimComponent.CODEC != null);
    //1.20.5+
    @Nullable
    public static final ItemDataComponent<Boolean> HIDE_TOOLTIP = register("hide_tooltip", YamlCodec.BOOL, ServerVersion.is1_20_5orNewer());
    @Nullable
    public static final ItemDataComponent<Integer> MAX_STACK_SIZE = register("max_stack_size", YamlCodec.INT, ServerVersion.is1_20_5orNewer());
    @Nullable
    public static final ItemDataComponent<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", YamlCodec.BOOL, ServerVersion.is1_20_5orNewer());
    //1.21.3+
    @Nullable
    public static final ItemDataComponent<Boolean> GLIDER = register("glider", YamlCodec.BOOL, ServerVersion.is1_21_3orNewer());

    public static final YamlCodec<ItemComponents> COMPONENTS_CODEC;

    @Nullable
    private static <T> ItemDataComponent<T> register(String name, YamlCodec<T> codec, boolean supplier) {
        if (supplier) {
            return register(name, codec);
        }
        return null;
    }

    private static <T> ItemDataComponent<T> register(String name, YamlCodec<T> codec) {
        int id = COMPONENTS.size();
        var component = new ItemDataComponent<>(id, name, codec);
        COMPONENTS.add(component);
        return component;
    }

    public static int count() {
        return COMPONENTS.size();
    }

    public static List<ItemDataComponent<?>> list() {
        return Collections.unmodifiableList(COMPONENTS);
    }

    static {
        var builder = PipelineYamlCodecBuilder.of(ItemComponents::new);
        for (ItemDataComponent component : COMPONENTS) {
            builder.field(component.codec(), component.name(),
                    v -> v.get(component),
                    (v, c) -> v.set(component, c)
            );
        }
        COMPONENTS_CODEC = builder.build();
    }
}
