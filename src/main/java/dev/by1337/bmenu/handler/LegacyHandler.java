package dev.by1337.bmenu.handler;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.legacy.Requirements;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;

public record LegacyHandler(
        Requirements requirements,
        Commands commands,
        Commands denyCommands
) implements MenuEventHandler, RequirementHandler {
    public static YamlCodec<LegacyHandler> CODEC = RecordYamlCodecBuilder.mapOf(
            LegacyHandler::new,
            Requirements.CODEC.fieldOf("requirements", LegacyHandler::requirements, Requirements.EMPTY),
            Commands.CODEC.fieldOf("commands", LegacyHandler::commands, Commands.EMPTY),
            Commands.CODEC.fieldOf("deny_commands", LegacyHandler::denyCommands, Commands.EMPTY)
    );

    @Override
    public boolean run(ExecuteContext ctx, PlaceholderApplier placeholders) {
        if (requirements.test(ctx.menu, placeholders, ctx)) {
            commands.run(ctx, placeholders);
            return true;
        } else {
            denyCommands.run(ctx, placeholders);
            return false;
        }
    }
    @Deprecated
    public boolean test(Menu menu, PlaceholderApplier placeholders) {
        return test(ExecuteContext.of(menu), placeholders);
    }
    public boolean test(ExecuteContext ctx, PlaceholderApplier placeholders) {
        if (requirements.test(ctx.menu, placeholders, ctx)) {
            commands.run(ctx, placeholders);
            return true;
        }
        denyCommands.run(ctx, placeholders);
        return false;
    }

    @Override
    public YamlValue encode() {
        return CODEC.encode(this);
    }
}
