package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;

import java.util.List;

public class CommandsAnimOpcode implements FrameOpcode {
    private final List<String> commands;

    public CommandsAnimOpcode(YamlValue val) {
        commands = val.getAsList(String.class);
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        menu.runCommands(commands.stream().map(menu::replace).toList());
    }
}
