package dev.by1337.bmenu.item;

import dev.by1337.bmenu.factory.ItemFactory;
import dev.by1337.bmenu.factory.fixer.ItemFixer;
import dev.by1337.bmenu.item.slot.DynamicSlotContent;
import dev.by1337.bmenu.item.slot.SingleSlotContent;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.placeholder.SlotPlaceholders;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SlotFactory implements Comparable<SlotFactory> {

    public static final YamlCodec<SlotFactory> YAML_CODEC;

    private int[] slots = new int[]{-1};
    private SlotPlaceholders localArgs = new SlotPlaceholders();
    private SlotVariant variant;
    private DynamicSlotContent.Data variants;
    private int priority;


    public SlotFactory() {

    }

    public static SlotFactory read(YamlMap yamlMap) {
        return ItemFactory.readItem(yamlMap);
    }

    @Nullable
    public SlotContent build(Menu menu) {
        return build(menu, null, null);
    }

    @Nullable
    public SlotContent build(Menu menu, @Nullable final ItemModel base, PlaceholderResolver<Menu> resolver1) {
        if (variants != null) {
            return new DynamicSlotContent(
                    localArgs.copy(),
                    variants,
                    base
            );
        } else {
            return new SingleSlotContent(
                    localArgs.copy(),
                    variant,
                    base
            );
        }
    }

    @Override
    public int compareTo(@NotNull SlotFactory o) {
        return Integer.compare(priority, o.priority);
    }

    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    public int[] getSlots() {
        return slots;
    }

    public int[] slots() {
        return slots;
    }

    public SlotPlaceholders getLocalArgs() {
        return localArgs;
    }

    public void setLocalArgs(SlotPlaceholders localArgs) {
        this.localArgs = localArgs;
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    static {
        PipelineYamlCodecBuilder<SlotFactory> builder = PipelineYamlCodecBuilder.of(SlotFactory::new)
                .field(SlotPlaceholders.CODEC, "local_args", SlotFactory::getLocalArgs, SlotFactory::setLocalArgs)
                .integer("priority", SlotFactory::priority, SlotFactory::setPriority, 0)
                .field(ItemFactory.SLOTS_YAML_CODEC, "slot", SlotFactory::slots, SlotFactory::setSlots, new int[]{-1});
        ;

        var oneOf = builder
                .copy()
                .field(DynamicSlotContent.Data.CODEC, null, s -> s.variants, (f, s) -> f.variants = s)
                .build();
        var single = builder
                .copy()
                .field(SlotVariant.CODEC, null, s -> s.variant, (f, s) -> f.variant = s)
                .build();

        YAML_CODEC = new YamlCodec<SlotFactory>() {
            private final SchemaType schemaType = single.schema().or(oneOf.schema());
            private final YamlCodec<SlotFactory> baseCodec = builder.build();

            @Override
            public DataResult<SlotFactory> decode(YamlValue yaml) {
                return yaml.decode(YAML_MAP).flatMap(map -> {
                    ItemFixer.fixItem(map);
                    return baseCodec.decode(map).flatMap(factory -> {
                        if (map.has("oneOf")) {
                            return DynamicSlotContent.Data.CODEC.decode(map).flatMap(d -> {
                                factory.variants = d;
                                return DataResult.success(factory);
                            });
                        } else {
                            return SlotVariant.CODEC.decode(map).flatMap(d -> {
                                factory.variant = d;
                                return DataResult.success(factory);
                            });
                        }
                    });
                });
            }

            @Override
            public YamlValue encode(SlotFactory factory) {
                //  YamlValue yamlValue = baseCodec.encode(factory);
                //  if (yamlValue.isMap()){
                //      YamlMap map = yamlValue.asYamlMap().getOrThrow();
                //      if (factory.variants != null){
                //         var encoded = DynamicSlotContent.Data.CODEC.encode(factory.variants);
                //          if (encoded.isMap()){
                //              YamlMap map2 = yamlValue.asYamlMap().getOrThrow();
                //          }
                //      }
                //  }
                return null;
            }

            @Override
            public @NotNull SchemaType schema() {
                return schemaType;
            }
        };
    }
}
