package dev.by1337.bmenu.handler.input;

import dev.by1337.bmenu.BMenu;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.util.math.FastExpressionParser;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;

import java.text.DecimalFormat;

public class PlayerNumberInput implements MenuEventHandler {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    public static final YamlCodec<PlayerNumberInput> CODEC = RecordYamlCodecBuilder.mapOf(
            PlayerNumberInput::new,
            YamlCodec.STRING.fieldOf("param", v -> v.param),
            Commands.CODEC.fieldOf("on_pass", v -> v.on_pass),
            Commands.CODEC.fieldOf("on_failed", v -> v.on_failed)
    );
    private final String param;
    private final Commands on_pass;
    private final Commands on_failed;

    public PlayerNumberInput(String param, Commands onPass, Commands onFailed) {
        this.param = param;
        on_pass = onPass;
        on_failed = onFailed;
    }

    @Override
    public boolean test(ExecuteContext ctx, PlaceholderApplier placeholders) {
        try (var enter = ctx.tracer.enter("input_chat {", "} -> %s")) {
            if (ctx.menu.loader().plugin() instanceof BMenu bMenu) {
                enter.result(true);
                var menu = ctx.menu;
                bMenu.playerInputListener().register(ctx.menu.viewer().getUniqueId(), s -> {
                    try (ExecuteContext newCtx = ExecuteContext.of(menu);
                         var on = newCtx.tracer.enter("input_chat(%s) {", s, "} -> %s")) {
                        on.result(false);
                        if (s == null) {
                            on_failed.test(newCtx, menu);
                        } else {
                            try {
                                double d = FastExpressionParser.parse(s);
                                menu.addArgument(param, df.format(d));
                                on_pass.test(newCtx, menu);
                                on.result(true);
                            } catch (FastExpressionParser.MathFormatException e) {
                                on_failed.test(newCtx, menu);
                            }
                        }
                    }
                });
            } else {
                ctx.tracer.log("The input_chat function is unavailable! Install the BMenu plugin!");
                ctx.menu.loader().plugin().getSLF4JLogger().error("The input_chat function is unavailable! Install the BMenu plugin!");
                enter.result(false);
            }
        }
        return true;
    }
}
