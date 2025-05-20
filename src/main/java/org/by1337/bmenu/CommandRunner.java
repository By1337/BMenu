package org.by1337.bmenu;

import java.util.List;
@FunctionalInterface
public interface CommandRunner {
    default void runCommands(List<String> commands) {
        for (String command : commands) {
            executeCommand(command);
        }
    }

    void executeCommand(String command);
}
