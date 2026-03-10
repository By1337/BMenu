package dev.by1337.bmenu.slot.impl;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.placeholder.SimplePlaceholders;
import dev.by1337.bmenu.slot.SlotVariant;
import dev.by1337.bmenu.slot.component.MenuClickType;
import dev.by1337.bmenu.slot.component.OnTickComponent;
import dev.by1337.item.ItemModel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class SlotContentImpl extends BaseSlotContent {
    private final SlotVariant head;

    private @Nullable ItemModel base;
    private @Nullable SlotVariant view;

    private @Nullable ItemModel last;
    private final @Nullable OnTickComponent ticker;
    private final boolean ticking;
    private int ticks;
    private int ticksCount;
    private boolean visible = true;

    public SlotContentImpl(SimplePlaceholders localArgs, SlotVariant variant) {
        this(localArgs, variant, null);
    }

    public SlotContentImpl(SimplePlaceholders localArgs, SlotVariant variant, @Nullable ItemModel base) {
        super(localArgs);
        this.base = base;
        head = variant;
        ticker = variant.ticker();
        ticking = ticker != null;
        if (ticking) {
            setPlaceholder("tick", () -> ticksCount);
        }
    }

    @Override
    public void doClick(Menu menu, Player player, MenuClickType type) {
        var pl = getPlaceholders(menu);
        try (var ctx = ExecuteContext.of(menu, this, type.getConfigKeyClick())){
            if (!head.doClick(type, ctx, pl)) {
                if (view != null) {
                    view.doClick(type, ctx, pl);
                }
            }
        }
    }

    @Override
    public boolean isTicking() {
        return ticking;
    }

    @Override
    public void doTick(Menu menu) {
        if (ticker != null && ticker.shouldTick(++ticks, head.tickSpeed())) {
            ticksCount++;
            ticker.tick(this, menu);
        }
    }

    @Override
    public ItemModel getItemModel() {
        return last == null ? ItemModel.AIR : last;
    }

    @Override
    public boolean isVisible(Menu menu) {
        if (isDirty()) {
            view = head.getView(menu, this);
            visible = view != null;
            if (visible){
                var n = view.model();
                if (n != last){
                    last = n;
                    if (base != null){
                        last = last.and(base);
                    }
                }
            }
        }
        return visible;
    }
}
