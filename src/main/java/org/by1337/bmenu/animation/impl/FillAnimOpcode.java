package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.hook.ItemStackCreator;

import java.util.Arrays;

public class FillAnimOpcode implements FrameOpcode {
    private static final int[] EMPTY_ARRAY = new int[0];
    private final String item;

    public FillAnimOpcode(YamlValue ctx) {
        item = ctx.getAsString();
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        MenuItemBuilder builder = menu.getConfig().findMenuItem(menu.replace(item), menu);
        MenuItem menuItem;
        if (builder == null) {
            menuItem = new MenuItem(EMPTY_ARRAY, ItemStackCreator.getItem(menu.replace(item)));
        } else {
            menuItem = builder.build(menu);
        }
        Arrays.fill(matrix, menuItem);
    }
}
