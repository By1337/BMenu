package org.by1337.bmenu.item;

import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.requirement.Requirements;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuItemTickListener {
    public static final MenuItemTickListener DEFAULT = new MenuItemTickListener(Requirements.EMPTY, List.of("[rebuild]"), List.of());
    public static final YamlCodec<MenuItemTickListener> CODEC = RecordYamlCodecBuilder.mapOf(
            Requirements.CODEC.fieldOf("requirements", MenuItemTickListener::getRequirements, Requirements.EMPTY),
            YamlCodec.STRINGS.fieldOf("commands", MenuItemTickListener::getCommands, List.of()),
            YamlCodec.STRINGS.fieldOf("deny_commands", MenuItemTickListener::getDenyCommands, List.of()),
            MenuItemTickListener::new
    );

    private final @NotNull Requirements requirements;
    private final List<String> commands;
    private final List<String> denyCommands;

    public MenuItemTickListener(@NotNull Requirements requirements, List<String> commands, List<String> denyCommands) {
        this.requirements = requirements;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public void tick(MenuItem menuItem, Menu menu, int tick) {
        Placeholderable placeholder = new BiPlaceholder(menu, s -> s.replace("{tick}", String.valueOf(tick)));
        if (requirements.test(menu, placeholder, menu.getViewer(), s -> executeCommand(s, menuItem, menu))) {
            commands.forEach(command -> executeCommand(placeholder.replace(command), menuItem, menu));
        } else {
            denyCommands.forEach(command -> executeCommand(placeholder.replace(command), menuItem, menu));
        }
    }

    public void executeCommand(String command, MenuItem menuItem, Menu menu) {
        if (command.equalsIgnoreCase("[rebuild]")) {
            menuItem.doRebuild();
        } else if (command.equalsIgnoreCase("[die]")) {
            menuItem.die();
        } else if (command.equalsIgnoreCase("[update]")) {
            menuItem.invalidateCash();
        } else {
            menu.executeCommand(command);
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
