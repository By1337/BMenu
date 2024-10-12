package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;

public class SetTitleOpcode implements FrameOpcode {
    private final String title;

    public SetTitleOpcode(YamlValue value) {
        title = value.getAsString();
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        menu.setTitle(menu.replace(title));
    }
}
