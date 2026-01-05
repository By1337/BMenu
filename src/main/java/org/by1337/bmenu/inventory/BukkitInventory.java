package org.by1337.bmenu.inventory;

import dev.by1337.core.BCore;
import dev.by1337.core.util.inventory.InventoryUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.by1337.bmenu.item.render.experemental.V1_16_5_ItemRenderer;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.item.render.BukkitItemRenderer;

import java.util.Arrays;
import java.util.Objects;

public class BukkitInventory {
    private static final InventoryUtil INV_UTIL = BCore.getInventoryUtil();
    private static final BukkitItemRenderer RENDERER = new BukkitItemRenderer();

    private final Inventory inventory;
    private final MenuItem[] seenItems;
    private final MenuItem[] items;
    private final int size;
    private final Menu menu;

    public BukkitInventory(Inventory inventory, Menu menu) {
        this.inventory = inventory;
        size = inventory.getSize();
        this.menu = menu;
        seenItems = new MenuItem[size];
        items = new MenuItem[size];
    }

    public void show(Player player) {
        if (!Objects.equals(player.getOpenInventory().getTopInventory(), inventory)) {
            player.openInventory(inventory);
        }
        // Отключаем автоматическую синхронизацию инвентаря с клиентом,
        // так как сервер может периодически отправлять обновления,
        // что может нарушить отображение анимаций.
        INV_UTIL.disableAutoFlush(player);
    }

    public void onClose(Player player) {
        // Восстанавливаем автоматическую синхронизацию инвентаря с клиентом,
        // так как пользователь закрыл кастомное меню, и ответственность за обновление
        // инвентаря теперь возвращается серверу.
        INV_UTIL.enableAutoFlush(player);
    }

    public void setItem(int slot, MenuItem item) {
        items[slot] = item;
    }

    public void sync(Player player) {
        for (int slot = 0; slot < size; slot++) {
            MenuItem old = seenItems[slot];
            MenuItem actual = items[slot];
            if (actual == null) {
                inventory.setItem(slot, null);
                seenItems[slot] = null;
            } else if (old != actual || actual.isDirty()) {
                seenItems[slot] = actual;
                RENDERER.render(
                        inventory,
                        slot,
                        actual.getItemModel(),
                        menu,
                        menu.getPlaceholderResolver().and(actual).bind(menu)
                );
                actual.setDirty(false);
            }
        }
        INV_UTIL.flushInv(player);
    }

    public void clear() {
        Arrays.fill(seenItems, null);
        Arrays.fill(items, null);
        inventory.clear();
    }

    public MenuItem[] getItems() {
        return items;
    }

    public void setTitle(Component newTitle) {
        INV_UTIL.sendFakeTitle(inventory, newTitle);
    }

    @Deprecated //todo скрыть под апи? ну пока пусть так
    public Inventory getInventory() {
        return inventory;
    }
}
