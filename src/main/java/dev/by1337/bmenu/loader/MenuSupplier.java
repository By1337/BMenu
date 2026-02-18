package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.menu.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface MenuSupplier {
    Menu createMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu);
}
