package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;

public class RemoveIfNotEmptyAnimOpcode implements FrameOpcode {
    private final int[] slots;

    public RemoveIfNotEmptyAnimOpcode(YamlValue ctx) {
        slots = AnimationUtil.readSlots(ctx.getAsString());
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        for (int slot : slots) {
            var item = menu.getMatrix()[slot];
            if (item != null && !item.getItemStack().getType().isAir()) {
                matrix[slot] = null;
            }
        }
    }
}
