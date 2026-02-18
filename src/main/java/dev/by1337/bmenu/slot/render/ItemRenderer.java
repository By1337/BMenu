package dev.by1337.bmenu.slot.render;

import dev.by1337.item.ItemModel;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.bmenu.menu.Menu;

public interface ItemRenderer<C> {
    void render(C ctx, int slot, ItemModel item, Menu menu, PlaceholderApplier placeholders);
    void flush(C ctx, Menu menu);
}
