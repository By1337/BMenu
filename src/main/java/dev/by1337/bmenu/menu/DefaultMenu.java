package dev.by1337.bmenu.menu;

import dev.by1337.bmenu.loader.MenuConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DefaultMenu extends AbstractMenu {
    public DefaultMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        super(config, viewer, previousMenu);
    }

    @Override
    protected void generate() {
    }
}
