package dev.by1337.bmenu.animation;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface FrameOpcode {
    void apply(SlotContent[] matrix, Menu menu, Animator animator);

    default @Nullable FrameOpcodes type() {
        return null;
    }
}
