package dev.by1337.bmenu.command;

import dev.by1337.cmd.CompiledCommand;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExecuteContext {
    public Menu menu;
    public @Nullable SlotContent item;

    public ExecuteContext(Menu menu) {
        this.menu = menu;
    }

    public ExecuteContext(Menu menu, @Nullable SlotContent item) {
        this.menu = menu;
        this.item = item;
    }

    public static ExecuteContext of(Menu menu) {
        return new ExecuteContext(menu);
    }
    public static ExecuteContext of(Menu menu, @Nullable SlotContent item) {
        return new ExecuteContext(menu, item);
    }
}
