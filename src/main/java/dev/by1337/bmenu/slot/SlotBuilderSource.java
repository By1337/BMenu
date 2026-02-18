package dev.by1337.bmenu.slot;

import dev.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public interface SlotBuilderSource {
    @Nullable SlotFactory resolveSlotBuilder(String name, Menu menu);
}
