package org.by1337.bmenu.item.render;

import dev.by1337.plc.Placeholderable;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.item.ItemModel;

public interface ItemRenderer<C> {
    void render(C ctx, int slot, ItemModel item, Menu menu, Placeholderable placeholders);
}
