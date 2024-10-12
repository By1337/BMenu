package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.by1337.bmenu.hook.ItemStackCreator;

public class SetAnimOpcode implements FrameOpcode {
    private final String item;
    private final int[] slots;

    public SetAnimOpcode(YamlValue ctx) {
        String[] args = ctx.getAsString().split(" ");
        item = args[0];
        slots = AnimationUtil.readSlots(args[1]);
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        MenuItemBuilder builder = menu.getConfig().findMenuItem(menu.replace(item), menu);
        if (builder == null) {
            AnimationUtil.set(new MenuItem(slots, ItemStackCreator.getItem(menu.replace(item))), matrix, slots);
        } else {
            MenuItem menuItem1 = builder.build(menu);
            if (menuItem1 != null) {
                AnimationUtil.set(menuItem1, matrix, slots);
            }
        }
    }
}
