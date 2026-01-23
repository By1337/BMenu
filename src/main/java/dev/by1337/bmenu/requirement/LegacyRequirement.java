package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.CommandLike;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.event.MenuEventHandler;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;

public record LegacyRequirement(Requirement requirement, Commands commands,
                                Commands denyCommands) implements Requirement, CommandLike, MenuEventHandler {
    public static final YamlCodec<LegacyRequirement> CODEC = RecordYamlCodecBuilder.mapOf(
            LegacyRequirement::new,
            Requirement.CODEC.fieldOf("check", LegacyRequirement::requirement, Requirement.TRUE),
            Commands.CODEC.fieldOf("commands", LegacyRequirement::commands, Commands.EMPTY),
            Commands.CODEC.fieldOf("deny_commands", LegacyRequirement::denyCommands, Commands.EMPTY)
    ).whenPrimitive(Requirement.CODEC.map(
            r -> new LegacyRequirement(r, Commands.EMPTY, Commands.EMPTY),
            LegacyRequirement::requirement
    ));

    @Override
    public boolean test(Menu menu, PlaceholderApplier placeholders) {
        return false;
    }

    @Override
    public YamlValue encode() {
        return CODEC.encode(this);
    }

    @Override
    public void run(ExecuteContext ctx, PlaceholderApplier placeholders) {
        if (requirement.test(ctx.menu, placeholders)) {
            commands.run(ctx, placeholders);
        } else {
            denyCommands.run(ctx, placeholders);
        }
    }
}
