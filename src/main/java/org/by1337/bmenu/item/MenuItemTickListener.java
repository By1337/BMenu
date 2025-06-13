package org.by1337.bmenu.item;

import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.requirement.Requirements;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuItemTickListener {
    public static final MenuItemTickListener DEFAULT = new MenuItemTickListener(Requirements.EMPTY, List.of("[rebuild]"), List.of());
    public static final YamlCodec<MenuItemTickListener> CODEC = RecordYamlCodecBuilder.mapOf(
            MenuItemTickListener::new,
            Requirements.CODEC.fieldOf("requirements", MenuItemTickListener::getRequirements, Requirements.EMPTY),
            MenuCodecs.COMMANDS.fieldOf("commands",MenuItemTickListener::getCommands, List.of()),
            MenuCodecs.COMMANDS.fieldOf("deny_commands", MenuItemTickListener::getDenyCommands, List.of())
    );

    private final @NotNull Requirements requirements;
    private final List<String> commands;
    private final List<String> denyCommands;

    public MenuItemTickListener(@NotNull Requirements requirements, List<String> commands, List<String> denyCommands) {
        this.requirements = requirements;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public void tick(MenuItem menuItem, Menu menu) {
        Placeholderable placeholder = new BiPlaceholder(menu, menuItem);
        if (requirements.test(menu, placeholder, menu.getViewer(), s -> menuItem.executeCommand(s, menu))) {
            commands.forEach(command -> menuItem.executeCommand(placeholder.replace(command), menu));
        } else {
            denyCommands.forEach(command -> menuItem.executeCommand(placeholder.replace(command), menu));
        }
    }


    public @NotNull Requirements getRequirements() {
        return requirements;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getDenyCommands() {
        return denyCommands;
    }

}
