package org.by1337.bmenu;

import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public interface MenuItemLookup {
    @Nullable MenuItemBuilder findMenuItem(String name, Menu menu);
}
