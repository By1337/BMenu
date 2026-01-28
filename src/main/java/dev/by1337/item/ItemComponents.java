package dev.by1337.item;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.item.component.impl.*;
import dev.by1337.bmenu.text.SourcedComponentLike;
import dev.by1337.bmenu.util.holder.ColorHolder;
import dev.by1337.bmenu.util.holder.IntHolder;
import dev.by1337.bmenu.util.holder.StringHolder;
import dev.by1337.core.ServerVersion;
import dev.by1337.item.component.BaseComponent;
import dev.by1337.item.component.ComponentsHolder;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemComponents {
    private static final List<BaseComponent<?>> COMPONENTS = new ArrayList<>();

    public static final BaseComponent<ItemLoreComponent> LORE = register("lore", ItemLoreComponent.CODEC);
    public static final BaseComponent<ComponentLike> NAME = register("name", SourcedComponentLike.COMPONENT_LIKE_CODEC);
    public static final BaseComponent<IntHolder> AMOUNT = register("amount", IntHolder.CODEC);
    public static final BaseComponent<IntHolder> DAMAGE = register("damage", IntHolder.CODEC);
    public static final BaseComponent<StringHolder> MATERIAL = register("material", MenuCodecs.MATERIAL.map(StringHolder::new, StringHolder::src));
    public static final BaseComponent<CustomModelDataComponent> MODEL_DATA = register("model_data", CustomModelDataComponent.CODEC);
    public static final BaseComponent<PotionContentsComponent> POTION_CONTENTS = register("potion_contents", PotionContentsComponent.CODEC);
    public static final BaseComponent<ColorHolder> COLOR = register("color", ColorHolder.CODEC);
    public static final BaseComponent<EnchantmentsComponent> ENCHANTMENTS = register("enchantments", EnchantmentsComponent.CODEC);
    public static final BaseComponent<Boolean> UNBREAKABLE = register("unbreakable", YamlCodec.BOOL);
    public static final BaseComponent<ContainerComponent> CONTAINER = register("container", ContainerComponent.CODEC);
    public static final BaseComponent<HideFlagsComponents> HIDE_FLAGS = register("item_flags", HideFlagsComponents.CODEC);
    //1.19.4+
    @Nullable
    public static final BaseComponent<ArmorTrimComponent> TRIM = register("trim", ArmorTrimComponent.CODEC, ArmorTrimComponent.CODEC != null);
    //1.20.5+
    @Nullable
    public static final BaseComponent<Boolean> HIDE_TOOLTIP = register("hide_tooltip", YamlCodec.BOOL, ServerVersion.is1_20_5orNewer());
    @Nullable
    public static final BaseComponent<Integer> MAX_STACK_SIZE = register("max_stack_size", YamlCodec.INT, ServerVersion.is1_20_5orNewer());
    @Nullable
    public static final BaseComponent<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", YamlCodec.BOOL, ServerVersion.is1_20_5orNewer());
    //1.21.3+
    @Nullable
    public static final BaseComponent<Boolean> GLIDER = register("glider", YamlCodec.BOOL, ServerVersion.is1_21_3orNewer());

    public static final YamlCodec<ComponentsHolder> COMPONENTS_CODEC;

    @Nullable
    private static <T> BaseComponent<T> register(String name, YamlCodec<T> codec, boolean supplier) {
        if (supplier) {
            return register(name, codec);
        }
        return null;
    }

    private static <T> BaseComponent<T> register(String name, YamlCodec<T> codec) {
        int id = COMPONENTS.size();
        var component = new BaseComponent<>(id, name, codec);
        COMPONENTS.add(component);
        return component;
    }

    public static int count() {
        return COMPONENTS.size();
    }

    public static List<BaseComponent<?>> list() {
        return Collections.unmodifiableList(COMPONENTS);
    }

    static {
        var builder = PipelineYamlCodecBuilder.of(ComponentsHolder::new);
        for (BaseComponent component : COMPONENTS) {
            builder.field(component.codec(), component.name(),
                    v -> v.get(component),
                    (v, c) -> v.set(component, c)
            );
        }
        COMPONENTS_CODEC = builder.build();
    }
}
