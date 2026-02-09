package dev.by1337.bmenu.slot;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.factory.fixer.ItemFixer;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.placeholder.SimplePlaceholders;
import dev.by1337.bmenu.slot.impl.BaseSlotContent;
import dev.by1337.bmenu.slot.impl.SlotContentImpl;
import dev.by1337.item.ItemModel;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class SlotFactory implements Comparable<SlotFactory> {

    public static final YamlCodec<SlotFactory> CODEC;
    private Map<String, String> args;
    private int[] slots = new int[]{-1};
    private SimplePlaceholders localArgs = new SimplePlaceholders();
    private SlotVariant variant;
    private int priority;


    public SlotFactory() {

    }

    @Nullable
    public SlotContent buildIfVisible(Menu menu) {
        return buildIfVisible(menu, null, null);
    }

    @Nullable
    public SlotContent buildIfVisible(Menu menu, @Nullable final ItemModel base, PlaceholderResolver<Menu> custom) {
        var v = build(base, custom);
        if (!v.isVisible(menu)) return null;
        return v;
    }

    public SlotContent build() {
        return build(null, null);
    }

    public SlotContent build(@Nullable final ItemModel base, PlaceholderResolver<Menu> custom) {
        BaseSlotContent result = new SlotContentImpl(
                localArgs.copy(),
                variant,
                base
        );
        result.addCustomResolver(custom);
        return result;
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

    public boolean hasSlot() {
        for (int slot : slots) {
            if (slot != -1) return true;
        }
        return false;
    }

    public SimplePlaceholders getLocalArgs() {
        return localArgs;
    }

    public void setLocalArgs(SimplePlaceholders localArgs) {
        this.localArgs = localArgs;
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, String> args() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    static {
        PipelineYamlCodecBuilder<SlotFactory> builder = PipelineYamlCodecBuilder.of(SlotFactory::new)
                .field(MenuCodecs.ARGS_CODEC, "args", SlotFactory::args, SlotFactory::setArgs)
                .field(SimplePlaceholders.CODEC, "local_args", SlotFactory::getLocalArgs, SlotFactory::setLocalArgs)
                .integer("priority", SlotFactory::priority, SlotFactory::setPriority, 0)
                .field(SlotVariant.CODEC, null, s -> s.variant, (f, s) -> f.variant = s)
                .field(MenuCodecs.SLOTS_CODEC, "slot", SlotFactory::slots, SlotFactory::setSlots, new int[]{-1});

        CODEC = builder
                .build()
                .preDecode(v -> {
                    var res = v.asYamlMap();
                    if (res.hasResult()) {
                        var map = res.result();
                        ItemFixer.fixItem(map);
                        return YamlValue.wrap(map);
                    }
                    return v;
                })
        ;
    }
}
