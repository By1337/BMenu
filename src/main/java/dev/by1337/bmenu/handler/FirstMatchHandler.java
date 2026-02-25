package dev.by1337.bmenu.handler;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;

import java.util.List;

public class FirstMatchHandler implements MenuEventHandler {
    public static final YamlCodec<FirstMatchHandler> CODEC = ConditionalHandler.CODEC.listOf()
            .map(FirstMatchHandler::new, FirstMatchHandler::blocks);
    private final List<ConditionalHandler> blocks;

    private FirstMatchHandler(List<ConditionalHandler> blocks) {
        this.blocks = blocks;
    }

    @Override
    public boolean test(ExecuteContext ctx, PlaceholderApplier placeholders) {
        try (var enter = ctx.tracer.enter("first-math [", "] -> %s")){
            enter.result(true);
            var iterator = blocks.iterator();
            while (iterator.hasNext()){
                var block = iterator.next();
                if (block.test(ctx, placeholders)) return true;
                if (iterator.hasNext()){
                    ctx.tracer.log("");
                }
            }
            enter.result(false);
            return false;
        }
    }

    @Override
    public YamlValue encode() {
        return CODEC.encode(this);
    }

    public List<ConditionalHandler> blocks() {
        return blocks;
    }
}
