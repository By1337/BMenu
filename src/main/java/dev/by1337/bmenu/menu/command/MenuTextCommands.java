package dev.by1337.bmenu.menu.command;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.cmd.Command;
import dev.by1337.edsl.command.TextCommands;
import org.bukkit.Bukkit;

/** Text output is implemented once in EventDSL; this is the BMenu context adapter. */
public final class MenuTextCommands {
    private MenuTextCommands() {
    }

    public static boolean defersPlaceholders(String source) {
        return TextCommands.defersPlaceholders(source);
    }

    public static Command<ExecuteContext> create() {
        return TextCommands.create(ctx -> ctx.eventContext().target(), ExecuteContext::placeholders,
                (ctx, output) -> {
                    for (var player : Bukkit.getOnlinePlayers()) {
                        try (var ignored = ctx.eventContext().target(player)) {
                            output.accept(player);
                        }
                    }
                });
    }
}
