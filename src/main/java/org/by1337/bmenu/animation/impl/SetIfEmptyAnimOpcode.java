package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.by1337.bmenu.hook.ItemStackCreator;
import org.jetbrains.annotations.Nullable;

public class SetIfEmptyAnimOpcode implements FrameOpcode {
    private final String item;
    private final int[] slots;

    public SetIfEmptyAnimOpcode(YamlValue ctx) {
        String[] args = ctx.getAsString().split(" ");
        item = args[0];
        slots = AnimationUtil.readSlots(args[1]);
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        MenuItemBuilder builder = menu.getConfig().findMenuItem(menu.replace(item), menu);
        if (builder == null) {
            setIfEmpty(new MenuItem(slots, ItemStackCreator.getItem(menu.replace(item))), matrix, slots);
        } else {
            MenuItem menuItem1 = builder.build(menu);
            if (menuItem1 != null) {
                setIfEmpty(menuItem1, matrix, slots);
            }
        }
    }
    public static void setIfEmpty(@Nullable MenuItem who, MenuItem[] to, int[] inSlots) {
        for (int inSlot : inSlots) {
            if (to[inSlot] == null){
                to[inSlot] = who;
            }
        }
    }
}
