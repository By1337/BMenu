package dev.by1337.bmenu.command;

import dev.by1337.cmd.Command;
import dev.by1337.cmd.CompiledCommand;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface CommandRunner<T> {
    Logger log = LoggerFactory.getLogger("BMenu");

    default void runCommands(T ctx, List<String> commands) {
        for (String command : commands) {
            executeCommand(ctx, command);
        }
    }

    default void executeCommand(T ctx, String command) {
        try {
            getCommands().execute(ctx,  command);
        } catch (Exception e) {
            log.error("Failed to run command: {}", command, e);
        }
    }

    @Nullable
    default CompiledCommand<T> compile(String command) {
        try {
            return getCommands().compile(command);
        } catch (Exception e) {
            log.error("Failed to run command: {}", command, e);
        }
        return null;
    }

    default void executeCommand(T ctx, CompiledCommand<T> command) {
        try {
            command.execute(ctx);
        } catch (Exception e) {
            log.error("Failed to run command: {}", command.getSource(), e);
        }
    }

    Command<T> getCommands();
}
