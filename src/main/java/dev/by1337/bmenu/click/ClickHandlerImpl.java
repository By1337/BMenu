package dev.by1337.bmenu.click;

import dev.by1337.plc.Placeholderable;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.Requirements;

public class ClickHandlerImpl implements ClickHandler {
    public static YamlCodec<ClickHandlerImpl> CODEC = RecordYamlCodecBuilder.mapOf(
            ClickHandlerImpl::new,
            Commands.CODEC.fieldOf("deny_commands", v -> v.denyCommands, Commands.EMPTY),
            Commands.CODEC.fieldOf("commands", v -> v.commands, Commands.EMPTY),
            Requirements.CODEC.fieldOf("requirements", v -> v.requirement, Requirements.EMPTY)
    );

    private final Commands denyCommands;
    private final Commands commands;
    private final Requirements requirement;

    public ClickHandlerImpl(Commands denyCommands, Commands commands, Requirements requirement) {
        this.denyCommands = denyCommands;
        this.commands = commands;
        this.requirement = requirement;
    }

    @Override
    public void onClick(Menu menu, Placeholderable placeholders, Player player, ExecuteContext ctx) {
        if (requirement.test(menu, placeholders, player, ctx)) {
            commands.run(ctx, placeholders);
        } else {
            denyCommands.run(ctx, placeholders);
        }
    }
}
