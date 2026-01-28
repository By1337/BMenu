package dev.by1337.bmenu;

import dev.by1337.bmenu.slot.SlotFactory;
import dev.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public interface MenuItemLookup {
    @Nullable SlotFactory findMenuItem(String name, Menu menu);
}
