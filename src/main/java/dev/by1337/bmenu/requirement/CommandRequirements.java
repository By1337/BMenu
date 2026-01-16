package dev.by1337.bmenu.requirement;

import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;

public class CommandRequirements {
    public static final YamlCodec<CommandRequirements> CODEC = RecordYamlCodecBuilder.mapOf(
            CommandRequirements::new,
            Requirements.CODEC.fieldOf("requirements", v -> v.requirements, Requirements.EMPTY),
            Commands.CODEC.fieldOf("deny_commands", v -> v.denyCommands, Commands.EMPTY),
            Commands.CODEC.fieldOf("commands", v -> v.commands, Commands.EMPTY)
    );
    private final Requirements requirements;
    private final Commands denyCommands;
    private final Commands commands;

    public CommandRequirements(Requirements requirements, Commands denyCommands, Commands commands) {
        this.requirements = requirements;
        this.denyCommands = denyCommands;
        this.commands = commands;
    }

    public void run(Menu menu, PlaceholderApplier placeholder, Player clicker) {
        if (!test(menu, placeholder, clicker)) {
            denyCommands.run(ExecuteContext.of(menu), placeholder);
        } else {
            commands.run(ExecuteContext.of(menu), placeholder);
        }
    }

    public boolean test(Menu menu, PlaceholderApplier PlaceholderApplier, Player clicker) {
        return requirements.test(menu, PlaceholderApplier, clicker, ExecuteContext.of(menu));
    }

    public Commands getCommands() {
        return commands;
    }

    public Commands getDenyCommands() {
        return denyCommands;
    }
}
