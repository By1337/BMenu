package dev.by1337.bmenu.command;

import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.menu.command.MenuTextCommands;
import dev.by1337.edsl.command.LocalizedTextCommands;
import dev.by1337.cmd.CompiledCommand;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuCommand implements MenuEventHandler {
    private static final Logger log = LoggerFactory.getLogger(MenuCommand.class);
    private final String source;
    private @Nullable CompiledCommand<ExecuteContext> compiled;
    private boolean canBeCompiled;
    private final boolean hasPlaceholders;
    private final boolean deferred;

    public MenuCommand(String source) {
        source = LocalizedTextCommands.preprocess(source);
        this.source = source;
        hasPlaceholders = source.contains("%") || source.contains("{");
        deferred = MenuTextCommands.defersPlaceholders(source);
        canBeCompiled = !hasPlaceholders || deferred;
    }

    public boolean test(PlayerContext ctx0, PlaceholderApplier placeholders) {
        var ctx = (ExecuteContext) ctx0;
        try (var scope = ctx.withPlaceholders(placeholders)) {
            if (compiled != null) {
                try (var ignored = ctx.tracer().timing("run: %s", source)) {
                    ctx.menu.executeCommand(ctx, compiled);
                }
            } else {
                if (canBeCompiled) {
                    compiled = ctx.menu.compile(source);
                    canBeCompiled = false;
                    test(ctx, placeholders);
                } else {
                    try (var ignored = ctx.tracer().timing("run: %s", source)) {
                        if (hasPlaceholders && !deferred) {
                            ctx.menu.executeCommand(ctx, placeholders.setPlaceholders(source));
                        } else {
                            ctx.menu.executeCommand(ctx, source);
                        }
                    }
                }
            }
            return true;
        }
    }

    public String source() {
        return source;
    }

    public @Nullable CompiledCommand<ExecuteContext> compiled() {
        return compiled;
    }

    public boolean canBeCompiled() {
        return canBeCompiled;
    }

    public boolean hasPlaceholders() {
        return hasPlaceholders;
    }

    @Override
    public YamlValue encode() {
        return YamlValue.wrap(source);
    }

    @Override
    public String toString() {
        return "MenuCommand{" +
                "source='" + source + '\'' +
                ", compiled=" + compiled +
                ", canBeCompiled=" + canBeCompiled +
                ", hasPlaceholders=" + hasPlaceholders +
                '}';
    }
}
