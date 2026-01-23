package dev.by1337.bmenu.event;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.item.ViewRequirement;
import dev.by1337.bmenu.requirement.Requirements;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;

public record LegacyHandler(
        Requirements requirements,
        Commands commands,
        Commands denyCommands
) implements MenuEventHandler {
    public static YamlCodec<LegacyHandler> CODEC = RecordYamlCodecBuilder.mapOf(
            LegacyHandler::new,
            Requirements.CODEC.fieldOf("requirements", LegacyHandler::requirements, Requirements.EMPTY),
            Commands.CODEC.fieldOf("commands", LegacyHandler::commands, Commands.EMPTY),
            Commands.CODEC.fieldOf("deny_commands", LegacyHandler::denyCommands, Commands.EMPTY)
    );

    @Override
    public void run(ExecuteContext ctx, PlaceholderApplier placeholders) {
        if (requirements.test(ctx.menu, placeholders, ctx)) {
            commands.run(ctx, placeholders);
        } else {
            denyCommands.run(ctx, placeholders);
        }
    }
}
