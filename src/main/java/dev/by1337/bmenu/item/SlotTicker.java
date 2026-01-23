package dev.by1337.bmenu.item;

import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.Requirements;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SlotTicker {
    public static final SlotTicker DEFAULT = new SlotTicker(1, Requirements.EMPTY, new Commands(List.of("[rebuild]")), Commands.EMPTY);
    public static final YamlCodec<SlotTicker> CODEC = RecordYamlCodecBuilder.mapOf(
            SlotTicker::new,
            YamlCodec.INT.fieldOf("tick_speed", SlotTicker::tickSpeed, 1),
            Requirements.CODEC.fieldOf("requirements", SlotTicker::getRequirements, Requirements.EMPTY),
            Commands.CODEC.fieldOf("commands", SlotTicker::getCommands, Commands.EMPTY),
            Commands.CODEC.fieldOf("deny_commands", SlotTicker::getDenyCommands, Commands.EMPTY)
    );

    private final int tickSpeed;
    private final @NotNull Requirements requirements;
    private final Commands commands;
    private final Commands denyCommands;

    public SlotTicker(int tickSpeed, @NotNull Requirements requirements, Commands commands, Commands denyCommands) {
        this.tickSpeed = tickSpeed;
        this.requirements = requirements;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public void tick(SlotContent slotContent, Menu menu) {
        var placeholder = menu.getPlaceholderResolver().and(slotContent).bind(menu);
        ExecuteContext ctx = ExecuteContext.of(menu, slotContent);
        if (requirements.test(menu, placeholder, ctx)) {
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

    public int tickSpeed() {
        return tickSpeed;
    }
    public boolean shouldTick(int ticks) {
        return ticks % tickSpeed == 0;
    }
}
