package org.by1337.bmenu.click;

import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.by1337.bmenu.CommandRunner;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.requirement.Requirements;

import java.util.List;

public class ClickHandlerImpl implements ClickHandler {
    public static YamlCodec<ClickHandlerImpl> CODEC = RecordYamlCodecBuilder.mapOf(
            ClickHandlerImpl::new,
            MenuCodecs.COMMANDS.fieldOf("deny_commands", v -> v.denyCommands, List.of()),
            MenuCodecs.COMMANDS.fieldOf("commands", v -> v.commands, List.of()),
            Requirements.CODEC.fieldOf("requirements", v -> v.requirement, Requirements.EMPTY)
    );

    private final List<String> denyCommands;
    private final List<String> commands;
    private final Requirements requirement;

    public ClickHandlerImpl(List<String> denyCommands, List<String> commands, Requirements requirement) {
        this.denyCommands = denyCommands;
        this.commands = commands;
        this.requirement = requirement;
    }

    @Override
    public void onClick(Menu menu, PlaceholderResolver<Menu> placeholders, Player player, CommandRunner commandRunner) {
        if (requirement.test(menu, placeholders, player, commandRunner)) {
            if (!commands.isEmpty())
                commands.forEach(command -> commandRunner.executeCommand(placeholders.replace(command, menu)));
        } else {
            if (!denyCommands.isEmpty())
                denyCommands.forEach(command -> commandRunner.executeCommand(placeholders.replace(command, menu)));
        }
    }
}
