package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;

public class GotoAnimOpcode implements FrameOpcode {
    private final int to;

    public GotoAnimOpcode(YamlValue value) {
        to = value.getAsInteger();
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        animator.setPos(to);
    }
}
