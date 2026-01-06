package org.by1337.bmenu.command;

import dev.by1337.cmd.CompiledCommand;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface CommandRunner<T> {
    default void runCommands(T ctx, List<String> commands) {
        for (String command : commands) {
            executeCommand(ctx, command);
        }
    }

    void executeCommand(T ctx, String command);

    @Nullable
    default CompiledCommand<T> executeAndTryCompile(T ctx, String command) {
        executeCommand(ctx, command);
        return null;
    }

    default void executeCommand(T ctx, CompiledCommand<T> command) {
        executeCommand(ctx, command.getSource());
    }

}
