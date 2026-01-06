package org.by1337.bmenu.animation.impl;


import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.FrameOpcodes;
import org.by1337.bmenu.command.ExecuteContext;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandsAnimOpcode implements FrameOpcode {
    public static final YamlCodec<CommandsAnimOpcode> CODEC = MenuCodecs.COMMANDS.map(CommandsAnimOpcode::new, v -> v.commands);

    private final List<String> commands;

    public CommandsAnimOpcode(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        menu.runCommands(ExecuteContext.of(menu), commands.stream().map(menu::replace).toList());
    }

    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.COMMANDS;
    }
}
