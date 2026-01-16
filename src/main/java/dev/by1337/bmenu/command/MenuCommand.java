package dev.by1337.bmenu.command;

import dev.by1337.cmd.CompiledCommand;
import dev.by1337.plc.PlaceholderApplier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuCommand {
    private static final Logger log = LoggerFactory.getLogger(MenuCommand.class);
    private final String source;
    private @Nullable CompiledCommand<ExecuteContext> compiled;
    private boolean canBeCompiled;
    private final boolean hasPlaceholders;

    public MenuCommand(String source) {
        this.source = source;
        hasPlaceholders = source.contains("%") || source.contains("{");
        canBeCompiled = !hasPlaceholders;
    }

    public void run(ExecuteContext ctx, PlaceholderApplier placeholders) {
        if (compiled != null) {
            ctx.executeCommand(compiled);
        } else {
            if (canBeCompiled) {
                compiled = ctx.executeAndTryCompile(source);
                canBeCompiled = false;
                //log.info("Command {} compiled {}", source, compiled);
            } else {
                if (hasPlaceholders) {
                    ctx.executeCommand(placeholders.setPlaceholders(source));
                } else {
                    ctx.executeCommand(source);
                }
            }
        }
    }
}
