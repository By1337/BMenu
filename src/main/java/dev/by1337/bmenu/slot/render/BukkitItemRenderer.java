package dev.by1337.bmenu.slot.render;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.core.BCore;
import dev.by1337.core.bridge.inventory.InventoryUtil;
import dev.by1337.item.ItemModel;
import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.inventory.Inventory;

public class BukkitItemRenderer implements ItemRenderer<Inventory> {
    private static final InventoryUtil INV_UTIL = BCore.getInventoryUtil();

    @Override
    public void render(Inventory ctx, int slot, ItemModel item, Menu menu, PlaceholderApplier placeholders) {
        ctx.setItem(slot, item.build(placeholders));
    }

    @Override
    public void flush(Inventory ctx, Menu menu) {
        INV_UTIL.flushInv(menu.viewer());
    }
}
