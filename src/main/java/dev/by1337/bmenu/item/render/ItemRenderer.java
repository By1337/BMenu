package dev.by1337.bmenu.item.render;

import dev.by1337.bmenu.item.item.ItemModel;
import dev.by1337.plc.Placeholderable;
import dev.by1337.bmenu.menu.Menu;

public interface ItemRenderer<C> {
    void render(C ctx, int slot, ItemModel item, Menu menu, Placeholderable placeholders);
}
