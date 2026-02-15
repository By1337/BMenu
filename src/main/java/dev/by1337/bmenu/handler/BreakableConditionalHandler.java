package dev.by1337.bmenu.handler;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.yaml.codec.CodecSelector;
import dev.by1337.bmenu.yaml.codec.YamlTester;
import dev.by1337.bmenu.yaml.dfu.BMenuDFU;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;

import java.util.ArrayList;
import java.util.List;

public class BreakableConditionalHandler implements MenuEventHandler {
    private static final YamlCodec<List<Requirement>> LIST_CODEC = Requirement.CODEC.listOf()
            .whenMap(YamlCodec.mapOf(YamlCodec.STRING, Requirement.CODEC)
                    .map(
                            map -> new ArrayList<>(map.values()),
                            v -> unsupported("no impl")
                    )
            );
    public static final YamlCodec<BreakableConditionalHandler> CODEC = RecordYamlCodecBuilder.mapOf(
            BreakableConditionalHandler::new,
            LIST_CODEC.fieldOf("requirements", BreakableConditionalHandler::handlers),
            Commands.CODEC.fieldOf("do", BreakableConditionalHandler::doCmds, Commands.EMPTY),
            Commands.CODEC.fieldOf("else", BreakableConditionalHandler::elseCmds, Commands.EMPTY)
    ).preDecode(BMenuDFU.COMMANDS_KEY_RENAMER);

    private final List<Requirement> handlers;
    private final Commands doCmds;
    private final Commands elseCmds;

    public BreakableConditionalHandler(List<Requirement> handlers, Commands doCmds, Commands elseCmds) {
        this.handlers = handlers;
        this.doCmds = doCmds;
        this.elseCmds = elseCmds;
    }

    @Override
    public boolean test(ExecuteContext ctx, PlaceholderApplier placeholders) {
        boolean result = true;
        for (Requirement requirement : handlers) {
            try {
                if (!requirement.test(ctx, placeholders)) {
                    result = false;
                    if (requirement instanceof ConditionalHandler c1){
                        Commands c = c1.elseCommands();
                        if (c.isHasBreak()) {
                            break;
                        }
                    }
                } else {
                    if (requirement instanceof ConditionalHandler c1){
                        Commands c = c1.doCommands();
                        if (c.isHasBreak()) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                ctx.menu.loader().logger().error("Failed to check requirement: {}", requirement, e);
            }
        }
        if (result) {
            doCmds.test(ctx, placeholders);
        } else {
            elseCmds.test(ctx, placeholders);
        }
        return result;
    }

    @Override
    public YamlValue encode() {
        return CODEC.encode(this);
    }

    public Commands doCmds() {
        return doCmds;
    }

    public Commands elseCmds() {
        return elseCmds;
    }

    public List<Requirement> handlers() {
        return handlers;
    }

    private static <T> T unsupported(String msg) {
        throw new UnsupportedOperationException(msg);
    }
}
