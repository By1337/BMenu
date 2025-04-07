package org.by1337.bmenu.impl;

import org.bukkit.entity.Player;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuConfig;
import org.jetbrains.annotations.Nullable;

public class DefaultMenu extends Menu {
    public DefaultMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        super(config, viewer, previousMenu);
    }

    @Override
    protected void generate() {

    }

    @Override
    protected boolean runCommand(String cmd) throws CommandException {
        return false;
    }

    @Override
    public boolean isSupportsHotReload() {
        return true;
    }
}
