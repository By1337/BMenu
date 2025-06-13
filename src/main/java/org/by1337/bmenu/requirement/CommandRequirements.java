package org.by1337.bmenu.requirement;

import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.factory.MenuCodecs;

import java.util.List;

public class CommandRequirements {
    public static final YamlCodec<CommandRequirements> CODEC = RecordYamlCodecBuilder.mapOf(
            CommandRequirements::new,
            Requirements.CODEC.fieldOf("requirements", v -> v.requirements, Requirements.EMPTY),
            MenuCodecs.COMMANDS.fieldOf("deny_commands", v -> v.denyCommands, List.of()),
            MenuCodecs.COMMANDS.fieldOf("commands", v -> v.commands, List.of())
    );
    private final Requirements requirements;
    private final List<String> denyCommands;
    private final List<String> commands;

    public CommandRequirements(Requirements requirements, List<String> denyCommands, List<String> commands) {
        this.requirements = requirements;
        this.denyCommands = denyCommands;
        this.commands = commands;
    }

    public void run(Menu menu, Placeholderable placeholderable, Player clicker) {
        if (!test(menu, placeholderable, clicker)) {
            runCommands(denyCommands, menu, placeholderable);
        } else {
            runCommands(commands, menu, placeholderable);
        }
    }

    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        return requirements.test(menu, placeholderable, clicker);
    }

    private void runCommands(List<String> commands, Menu menu, Placeholderable placeholderable) {
        for (String command : commands) {
            if (command.equalsIgnoreCase("[break]")) return;
            menu.executeCommand(placeholderable.replace(command));
        }
        return;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getDenyCommands() {
        return denyCommands;
    }
}
