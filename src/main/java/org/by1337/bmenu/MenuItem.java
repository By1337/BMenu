package org.by1337.bmenu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.bmenu.click.ClickHandler;
import org.by1337.bmenu.click.MenuClickType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MenuItem {
    private int[] slots;
    private ItemStack itemStack;
    private Map<MenuClickType, ClickHandler> clicks = new HashMap<>();
    private @Nullable Placeholderable customPlaceholderable;
    private @Nullable Object data;

    public MenuItem(int[] slots, ItemStack itemStack, Map<MenuClickType, ClickHandler> clicks) {
        this.slots = slots;
        this.itemStack = itemStack;
        this.clicks = clicks;
    }

    public MenuItem(int[] slots, ItemStack itemStack) {
        this.slots = slots;
        this.itemStack = itemStack;
    }

    public void doClick(Menu menu, Player player, MenuClickType type) {
        Placeholderable placeholderable;
        if (customPlaceholderable == null) {
            placeholderable = menu;
        } else {
            placeholderable = new BiPlaceholder(menu, customPlaceholderable);
        }

        ClickHandler handler = clicks.get(type);
        if (handler != null) {
            handler.onClick(menu, placeholderable, player);
        } else {
            handler = clicks.get(MenuClickType.ANY_CLICK);
            if (handler != null)
                handler.onClick(menu, placeholderable, player);
        }
    }

    public int[] getSlots() {
        return slots;
    }

    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Map<MenuClickType, ClickHandler> getClicks() {
        return clicks;
    }

    public void setClicks(Map<MenuClickType, ClickHandler> clicks) {
        this.clicks = clicks;
    }

    public Placeholderable getCustomPlaceholderable() {
        return customPlaceholderable;
    }

    public @Nullable Object getData() {
        return data;
    }

    public void setData(@Nullable Object data) {
        this.data = data;
    }

    public void setCustomPlaceholderable(@Nullable Placeholderable customPlaceholderable) {
        this.customPlaceholderable = customPlaceholderable;
    }
}
