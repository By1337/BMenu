package org.by1337.bmenu.animation;

import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;

@FunctionalInterface
public interface FrameOpcode {
    void apply(MenuItem[] matrix, Menu menu, Animator animator);
}
