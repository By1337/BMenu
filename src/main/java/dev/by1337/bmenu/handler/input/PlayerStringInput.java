package dev.by1337.bmenu.handler.input;

import dev.by1337.bmenu.BMenu;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.by1337.yaml.codec.YamlCodec;

public class PlayerStringInput implements MenuEventHandler {
    public static final YamlDecoder<PlayerStringInput> DECODER = RecordYamlDecoder.mapOf(
            PlayerStringInput::new,
            YamlDecoder.STRING.fieldOf("param"),
            Commands.DECODER.fieldOf("on_pass"),
            Commands.DECODER.fieldOf("on_failed")
    );
    @Deprecated
    public static final YamlCodec<PlayerStringInput> CODEC = YamlCodec.of(DECODER);
    private final String param;
    private final Commands on_pass;
    private final Commands on_failed;

    public PlayerStringInput(String param, Commands onPass, Commands onFailed) {
        this.param = param;
        on_pass = onPass;
        on_failed = onFailed;
    }

    @Override
    public boolean test(PlayerContext ctx0, PlaceholderApplier placeholders) {
        var ctx = (ExecuteContext) ctx0;
        try (var enter = ctx.tracer().enter("input_chat_str {", "} -> %s")) {
            if (ctx.menu.loader().plugin() instanceof BMenu bMenu) {
                enter.result(true);
                var menu = ctx.menu;
                bMenu.playerInputListener().register(ctx.menu.viewer().getUniqueId(), s -> {
                    try (ExecuteContext newCtx = ExecuteContext.of(menu);
                        var on = newCtx.tracer.enter("input_chat_str(%s) {", s, "} -> %s")) {
                        on.result(false);
                        if (s == null) {
                            on_failed.test(newCtx, menu);
                        } else {
                            if (s.startsWith("/")) s = s.substring(1);
                            menu.addArgument(param, s);
                            on_pass.test(newCtx, menu);
                            on.result(true);
                        }
                    }
                });
            } else {
                ctx.tracer.log("The input_chat_str function is unavailable! Install the BMenu plugin!");
                ctx.menu.loader().plugin().getSLF4JLogger().error("The input_chat_str function is unavailable! Install the BMenu plugin!");
                enter.result(false);
            }
        }
        return true;
    }
}
