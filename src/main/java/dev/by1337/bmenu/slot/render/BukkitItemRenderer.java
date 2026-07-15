package dev.by1337.bmenu.slot.render;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.core.BCore;
import dev.by1337.core.bridge.inventory.InventoryUtil;
import dev.by1337.item.ItemModel;
import dev.by1337.item.ItemStackBuilder;
import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.inventory.Inventory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BukkitItemRenderer implements ItemRenderer<Inventory> {
    private static final InventoryUtil INV_UTIL = BCore.getInventoryUtil();
    private static final Executor RENDERER_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        var v = new Thread(r);
        v.setName("bmenu-item-renderer");
        return v;
    });

    @Override
    public void render(Inventory ctx, int slot, ItemModel item, Menu menu, PlaceholderApplier placeholders) {
        RENDERER_EXECUTOR.execute(() -> ctx.setItem(slot, ItemStackBuilder.build(item, placeholders, menu.viewer().locale())));
    }

    @Override
    public void flush(Inventory ctx, Menu menu) {
        RENDERER_EXECUTOR.execute(() -> {
            if (menu.isOpened()) {
                INV_UTIL.flushInv(menu.viewer());
            }
        });
    }
}
