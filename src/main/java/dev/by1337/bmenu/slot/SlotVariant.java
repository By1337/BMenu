package dev.by1337.bmenu.slot;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.component.ClickMapComponent;
import dev.by1337.bmenu.slot.component.MenuClickType;
import dev.by1337.bmenu.slot.component.OnTickComponent;
import dev.by1337.bmenu.slot.component.OnViewComponent;
import dev.by1337.item.ItemModel;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.decoder.PipelineYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlotVariant {

    public static final YamlDecoder<SlotVariant> DECODER = YamlDecoder.<SlotVariant>recursive(self -> PipelineYamlDecoder.of(SlotVariant::new)
            .field(ItemModel.DECODER, null, (h, v) -> h.itemModel = v)
            .field(OnViewComponent.DECODER, "on_view", (h, v) -> h.onViewComponent = v)
            .field(ClickMapComponent.DECODER, null, (h, v) -> h.clicks = v)
            .field(OnTickComponent.DECODER, "on_tick", (h, v) -> h.ticker = v)
            .field(YamlDecoder.INT, "tick_speed", (h, v) -> h.tickSpeed = v)
            .field(self.listOf().whenMap(YamlDecoder.mapOf(YamlDecoder.STRING, self).map(m -> new java.util.ArrayList<>(m.values()))), "oneOf", (h, v) -> h.variants = v)
            .build()
    ).map(SlotVariant::postDecode);
    @Deprecated
    public static final YamlCodec<SlotVariant> CODEC = YamlCodec.of(DECODER);

    private ItemModel itemModel = ItemModel.AIR;
    private @Nullable OnViewComponent onViewComponent;
    private @Nullable ClickMapComponent clicks;
    private @Nullable OnTickComponent ticker;
    private @Nullable List<SlotVariant> variants;
    private int tickSpeed = 20;

    public SlotVariant() {
    }

    @ApiStatus.Internal
    private SlotVariant postDecode() {
        if (variants != null) {
            for (SlotVariant variant : variants) {
                variant.itemModel = variant.itemModel.setDefaults(itemModel);
            }
        }
        return this;
    }

    private SlotVariant copy() {
        SlotVariant o = new SlotVariant();
        o.itemModel = itemModel;
        o.onViewComponent = onViewComponent;
        o.clicks = clicks;
        o.ticker = ticker;
        o.variants = variants;
        o.tickSpeed = tickSpeed;
        return o;
    }

    public SlotVariant and(ItemModel itemModel) {
        var v = copy();
        v.itemModel = v.itemModel.and(itemModel);
        return v;
    }

    public static SlotVariant of(ItemModel itemModel) {
        SlotVariant v = new SlotVariant();
        v.itemModel = itemModel;
        return v;
    }

    public static SlotVariant of(ItemModel itemModel, ClickMapComponent clicks) {
        SlotVariant v = new SlotVariant();
        v.itemModel = itemModel;
        v.clicks = clicks;
        return v;
    }


    public boolean doClick(MenuClickType type, ExecuteContext ctx, PlaceholderApplier placeholders) {
        return clicks != null && clicks.doClick(type, ctx, placeholders);
    }

    public ItemModel model() {
        return itemModel;
    }

    public @Nullable OnViewComponent onView() {
        return onViewComponent;
    }

    public @Nullable ClickMapComponent clicks() {
        return clicks;
    }

    public @Nullable OnTickComponent ticker() {
        return ticker;
    }

    public @Nullable List<SlotVariant> variants() {
        return variants;
    }

    public int tickSpeed() {
        return tickSpeed;
    }

    public @Nullable SlotVariant getView(Menu menu, SlotContent slot) {
        if (onViewComponent != null && !onViewComponent.isVisible(menu, slot)) return null;
        if (variants != null) {
            for (SlotVariant variant : variants) {
                var v = variant.getView(menu, slot);
                if (v != null) return v;
            }
        }
        return this;
    }
}
