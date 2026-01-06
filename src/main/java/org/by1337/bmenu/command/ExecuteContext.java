package org.by1337.bmenu.command;

import dev.by1337.cmd.CompiledCommand;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExecuteContext {
    public final Menu menu;
    public @Nullable MenuItem item;

    public ExecuteContext(Menu menu) {
        this.menu = menu;
    }

    public ExecuteContext(Menu menu, @Nullable MenuItem item) {
        this.menu = menu;
        this.item = item;
    }

    public static ExecuteContext of(Menu menu) {
        return new ExecuteContext(menu);
    }
    public static ExecuteContext of(Menu menu, @Nullable MenuItem item) {
        return new ExecuteContext(menu, item);
    }


    public void runCommands(List<String> commands) {
        menu.runCommands(this, commands);
    }


    public void executeCommand(String command) {
        menu.executeCommand(this, command);
    }


    public @Nullable CompiledCommand<ExecuteContext> executeAndTryCompile(String command) {
        return menu.executeAndTryCompile(this, command);
    }


    public void executeCommand(CompiledCommand<ExecuteContext> command) {
        menu.executeCommand(this, command);
    }
}
