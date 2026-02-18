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
        for (ConditionalHandler block : blocks) {
            if (block.test(ctx, placeholders)) return true;
        }
        return false;
    }

    @Override
    public YamlValue encode() {
        return CODEC.encode(this);
    }

    public List<ConditionalHandler> blocks() {
        return blocks;
    }
}
