package dev.by1337.bmenu.animation.impl;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.MenuItem;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import org.jetbrains.annotations.Nullable;

public class GotoAnimOpcode implements FrameOpcode {
    public static YamlCodec<GotoAnimOpcode> CODEC = YamlCodec.INT.map(GotoAnimOpcode::new, v -> v.to);
    private final int to;

    public GotoAnimOpcode(Integer value) {
        to = value;
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        animator.setPos(to);
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.GOTO;
    }
}
