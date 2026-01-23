package dev.by1337.bmenu.item.slot;

import dev.by1337.bmenu.click.ClickMap;
import dev.by1337.bmenu.click.MenuClickType;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.item.SlotTicker;
import dev.by1337.bmenu.item.SlotVariantList;
import dev.by1337.bmenu.item.ViewRequirement;
import dev.by1337.bmenu.item.ItemModel;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.placeholder.SimplePlaceholders;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class DynamicSlotContent extends BaseSlotContent {
    private final Data data;
    private final @Nullable ItemModel baseModel;
    private @Nullable SlotVariantList.Entry current;
    private final SlotVariantList.Entry[] variants;
    private final ItemModel[] cachedModels;
    private final boolean ticking;
    private ItemModel activeModel;
    private int ticks;
    private int ticksCount;
    private boolean visible = true;

    public DynamicSlotContent(SimplePlaceholders localArgs, Data data, @Nullable ItemModel baseModel) {
        super(localArgs);
        this.data = data;
        this.baseModel = baseModel;
        variants = data.variants.entries();
        cachedModels = new ItemModel[variants.length];
        ticking = data.ticker != null || data.variants.isTicking();
        if (ticking) {
            setPlaceholder("tick", () -> ticksCount);
        }
    }


    @Override
    public void doClick(Menu menu, Player player, MenuClickType type) {
        var pl = getPlaceholders(menu);
        var ctx = ExecuteContext.of(menu, this);
        if (!data.clickMap.doClick(type, ctx, pl)) {
            if (current != null) {
                current.variant().clicks().doClick(type, ctx, pl);
            }
        }
    }

    @Override
    public boolean isTicking() {
        return ticking;
    }

    @Override
    public void doTick(Menu menu) {
        if (++ticks % data.tickSpeed == 0) {
            ticksCount++;
            if (data.ticker != null) data.ticker.tick(this, menu);
            for (SlotVariantList.Entry variant : variants) {
                var ticker = variant.variant().itemTicker();
                if (ticker != null) {
                    ticker.tick(this, menu);
                }
            }
        }
    }

    @Override
    public ItemModel getItemModel() {
        if (activeModel == null) return ItemModel.AIR;
        return activeModel;
    }

    @Override
    public boolean isVisible(Menu menu) {
        if (isDirty()) {
            visible = data.viewRequirement.isVisible(menu, this);
            if (!visible) return false;
            for (SlotVariantList.Entry variant : variants) {
                visible = variant.variant().viewRequirement().isVisible(menu, this);
                if (visible) {
                    current = variant;
                    activeModel = variant.variant().itemModel();
                    if (baseModel != null) {
                        var v = cachedModels[variant.index()];
                        if (v != null) {
                            activeModel = v;
                        } else {
                            cachedModels[variant.index()] = activeModel = activeModel.and(baseModel);
                        }
                    }
                    return true;
                }
            }
            current = null;
            activeModel = null;
            visible = false;
        }
        return visible;
    }

    public static class Data {
        public static YamlCodec<Data> CODEC = RecordYamlCodecBuilder.mapOf(
                Data::new,
                SlotVariantList.CODEC.fieldOf("oneOf", Data::variants),
                ViewRequirement.CODEC.fieldOf("on_view", Data::viewRequirement, ViewRequirement.EMPTY),
                ClickMap.CODEC.fieldOf(null, Data::clickMap, ClickMap.EMPTY),
                SlotTicker.CODEC.schema(s -> s.asBuilder().remove("tick_speed").build())
                        .fieldOf("on_tick", Data::ticker),
                YamlCodec.INT.fieldOf("tick_speed", Data::tickSpeed, 1)
        );
        private final SlotVariantList variants;
        private final ViewRequirement viewRequirement;
        private final ClickMap clickMap;
        private final @Nullable SlotTicker ticker;
        private final int tickSpeed;

        public Data(SlotVariantList variants, ViewRequirement viewRequirement, ClickMap clickMap, @Nullable SlotTicker ticker, int tickSpeed) {
            this.variants = variants;
            this.viewRequirement = viewRequirement;
            this.clickMap = clickMap;
            this.ticker = ticker;
            this.tickSpeed = tickSpeed;
        }

        public @Nullable SlotTicker ticker() {
            return ticker;
        }

        public ClickMap clickMap() {
            return clickMap;
        }

        public ViewRequirement viewRequirement() {
            return viewRequirement;
        }

        public SlotVariantList variants() {
            return variants;
        }

        public int tickSpeed() {
            return tickSpeed;
        }
    }
}
