package dev.by1337.bmenu.slot.render;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.core.BCore;
import dev.by1337.core.bridge.inventory.InventoryUtil;
import dev.by1337.item.ItemModel;
import dev.by1337.item.ItemStackBuilder;
import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.entity.Player;
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
    private static final ThreadLocal<Player> PLAYER_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public void render(Inventory ctx, int slot, ItemModel item, Menu menu, PlaceholderApplier placeholders) {
        Player viewer = menu.viewer();
        RENDERER_EXECUTOR.execute(() -> {
            try (var ignored = setPlayer(viewer)) {
                ctx.setItem(slot, ItemStackBuilder.build(item, placeholders, viewer.locale()));
            }
        });
    }

    @Override
    public void flush(Inventory ctx, Menu menu) {
        RENDERER_EXECUTOR.execute(() -> {
            if (menu.isOpened()) {
                INV_UTIL.flushInv(menu.viewer());
            }
        });
    }

    public static Player getPlayer() {
        return PLAYER_THREAD_LOCAL.get();
    }

    public static Scope setPlayer(Player player) {
        var old = PLAYER_THREAD_LOCAL.get();
        PLAYER_THREAD_LOCAL.set(player);
        return () -> PLAYER_THREAD_LOCAL.set(old);
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
