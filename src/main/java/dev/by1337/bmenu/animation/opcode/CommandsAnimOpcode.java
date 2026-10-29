package dev.by1337.bmenu.animation.opcode;


import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Nullable;

public class CommandsAnimOpcode implements FrameOpcode {
    public static final dev.by1337.yaml.decoder.YamlDecoder<CommandsAnimOpcode> DECODER = Commands.DECODER.map(CommandsAnimOpcode::new);
    @Deprecated
    public static final YamlCodec<CommandsAnimOpcode> CODEC = YamlCodec.of(DECODER);

    private final Commands commands;

    public CommandsAnimOpcode(Commands commands) {
        this.commands = commands;
    }

    @Override
    public void apply(SlotContent[] matrix, Menu menu, Animator animator) {
        try (var ctx = ExecuteContext.of(menu, "animation")) {
            commands.test(ctx, menu);
        }
    }

    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.COMMANDS;
    }
}
