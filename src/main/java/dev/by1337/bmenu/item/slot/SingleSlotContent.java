package dev.by1337.bmenu.item.slot;

import dev.by1337.bmenu.click.MenuClickType;
import dev.by1337.bmenu.item.SlotTicker;
import dev.by1337.bmenu.item.SlotVariant;
import dev.by1337.bmenu.item.ViewRequirement;
import dev.by1337.bmenu.item.item.ItemModel;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.placeholder.SlotPlaceholders;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class SingleSlotContent extends BaseSlotContent {
    private final SlotVariant variant;
    private final ItemModel itemModel;
    private final @Nullable SlotTicker ticker;
    private final ViewRequirement viewRequirement;
    private final boolean ticking;
    private int ticks;
    private int ticksCount;
    private boolean visible = true;

    public SingleSlotContent(SlotPlaceholders localArgs, SlotVariant variant) {
        this(localArgs, variant, null);
    }

    public SingleSlotContent(SlotPlaceholders localArgs, SlotVariant variant, @Nullable ItemModel itemModel) {
        super(localArgs);
        this.itemModel = itemModel == null ? variant.itemModel() : variant.itemModel().and(itemModel);
        this.variant = variant;
        ticker = variant.itemTicker();
        viewRequirement = variant.viewRequirement();
        ticking = ticker != null;
        if (ticking) {
            setPlaceholder("tick", () -> ticksCount);
        }
    }

    @Override
    public void doClick(Menu menu, Player player, MenuClickType type) {
        variant.doClick(menu, player, type, this);
    }

    @Override
    public boolean isTicking() {
        return ticking;
    }

    @Override
    public void doTick(Menu menu) {
        if (ticker != null && ticker.shouldTick(++ticks)) {
            ticksCount++;
            ticker.tick(this, menu);
        }
    }

    @Override
    public ItemModel getItemModel() {
        return itemModel;
    }

    @Override
    public boolean isVisible(Menu menu) {
        if (isDirty()) {
            visible = viewRequirement.isVisible(menu, this);
        }
        return visible;
    }
}
