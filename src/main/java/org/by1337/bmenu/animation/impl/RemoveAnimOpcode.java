package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;

public class RemoveAnimOpcode implements FrameOpcode {
    private final int[] slots;

    public RemoveAnimOpcode(YamlValue ctx) {
        slots = AnimationUtil.readSlots(ctx.getAsString());
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        AnimationUtil.set(null, matrix, slots);
    }
}
