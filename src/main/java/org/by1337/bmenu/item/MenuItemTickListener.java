package org.by1337.bmenu.item;

import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.command.Commands;
import org.by1337.bmenu.command.ExecuteContext;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.requirement.Requirements;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuItemTickListener {
    public static final MenuItemTickListener DEFAULT = new MenuItemTickListener(Requirements.EMPTY, new Commands(List.of("[rebuild]")), Commands.EMPTY);
    public static final YamlCodec<MenuItemTickListener> CODEC = RecordYamlCodecBuilder.mapOf(
            MenuItemTickListener::new,
            Requirements.CODEC.fieldOf("requirements", MenuItemTickListener::getRequirements, Requirements.EMPTY),
            Commands.CODEC.fieldOf("commands", MenuItemTickListener::getCommands, Commands.EMPTY),
            Commands.CODEC.fieldOf("deny_commands", MenuItemTickListener::getDenyCommands, Commands.EMPTY)
    );

    private final @NotNull Requirements requirements;
    private final Commands commands;
    private final Commands denyCommands;

    public MenuItemTickListener(@NotNull Requirements requirements, Commands commands, Commands denyCommands) {
        this.requirements = requirements;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public void tick(MenuItem menuItem, Menu menu) {
        var placeholder = menu.getPlaceholderResolver().and(menuItem).bind(menu);
        ExecuteContext ctx = ExecuteContext.of(menu, menuItem);
        if (requirements.test(menu, placeholder, menu.getViewer(), ctx)) {
            commands.run(ctx, placeholder);
        } else {
            denyCommands.run(ctx, placeholder);
        }
    }


    public @NotNull Requirements getRequirements() {
        return requirements;
    }

    public Commands getCommands() {
        return commands;
    }

    public Commands getDenyCommands() {
        return denyCommands;
    }

}
