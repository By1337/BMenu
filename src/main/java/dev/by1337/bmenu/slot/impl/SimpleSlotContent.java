package dev.by1337.bmenu.slot.impl;

import dev.by1337.bmenu.slot.component.MenuClickType;
import dev.by1337.item.ItemModel;
import dev.by1337.bmenu.menu.Menu;
import org.bukkit.entity.Player;

public class SimpleSlotContent extends BaseSlotContent {
    private final ItemModel itemModel;

    public SimpleSlotContent(ItemModel itemModel) {
        this.itemModel = itemModel;
    }

    @Override
    public void doClick(Menu menu, Player player, MenuClickType type) {
    }

    @Override
    public boolean isTicking() {
        return false;
    }

    @Override
    public void doTick(Menu menu) {
    }

    @Override
    public ItemModel getItemModel() {
        return itemModel;
    }
}
