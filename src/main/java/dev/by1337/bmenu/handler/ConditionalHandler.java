package dev.by1337.bmenu.handler;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.yaml.dfu.KeyRenamer;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConditionalHandler implements MenuEventHandler {
    public static final YamlCodec<ConditionalHandler> CODEC = PipelineYamlCodecBuilder.of(ConditionalHandler::new)
            .field(Requirement.Codec.allOf(), "if", m -> m.req, (m, v) -> m.req = v)
            .field(Requirement.Codec.oneOf(), "if-one", m -> null, (m, v) -> m.req = v)
            .field(Requirement.Codec.oneOf(), "if-all", m -> null, (m, v) -> m.req = v)
            .field(Commands.CODEC, "do", m -> m.doCmds, (m, v) -> m.doCmds = v)
            .field(Commands.CODEC, "else", m -> m.elseCmds, (m, v) -> m.elseCmds = v)
            .build()
            .preDecode(new KeyRenamer(Map.of(
                    "check", "if",
                    "commands", "do",
                    "deny_commands", "else"
            )))
            .whenPrimitive(Requirement.CODEC.map(
                    r -> new ConditionalHandler(r, Commands.EMPTY, Commands.EMPTY),
                    r -> r.req
            ));


    private Requirement req;
    private Commands doCmds;
    private Commands elseCmds;

    public ConditionalHandler() {
        req = Requirement.TRUE;
        doCmds = Commands.EMPTY;
        elseCmds = Commands.EMPTY;
    }

    public ConditionalHandler(Requirement req, Commands doCmds, Commands elseCmds) {
        this.req = req;
        this.doCmds = doCmds;
        this.elseCmds = elseCmds;
    }

    @Override
    public boolean test(ExecuteContext ctx, PlaceholderApplier placeholders) {
        if (req.test(ctx.menu, placeholders)) {
            doCmds.test(ctx, placeholders);
            return true;
        }
        elseCmds.test(ctx, placeholders);
        return false;
    }

    @Override
    public YamlValue encode() {
        return CODEC.encode(this);
    }

    public Requirement req() {
        return req;
    }

    public Commands doCommands() {
        return doCmds;
    }

    public Commands elseCommands() {
        return elseCmds;
    }

    private static <T> T unsupported(String msg) {
        throw new UnsupportedOperationException(msg);
    }
}
